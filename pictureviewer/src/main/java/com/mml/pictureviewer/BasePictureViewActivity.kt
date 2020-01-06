package com.mml.pictureviewer

import android.app.SharedElementCallback
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.github.chrisbanes.photoview.PhotoView
import kotlinx.android.synthetic.main.activity_base_picture_view.*

/**
 * 使用自定义view 重写 [getCustomLayoutId]和[customLayoutConvert]
 * 使用默认view 重写 [onPictureShow]即可
 * 重写[getIPagerNavigator]即可设置自定义的导航器样式
 * 支持设置中间页 [currentPosition]
 */
abstract class BasePictureViewActivity<T> : AppCompatActivity() {
    //live data 数据
    protected val mDataList = MutableLiveData<MutableList<T>>()
    protected var mViewPager: ViewPager? = null
    private var iPagerNavigator:IPagerNavigator<MutableList<T>>? = null

    protected var currentPosition=  MutableLiveData<Int>()

    /**
     * 初始化数据类型
     */
    abstract fun initData(data: MutableList<T>?)

    /**
     * 使用自己的图片加载框架,用户自己决定采用什么方式显示
     * @param photoView  支持缩放的imageview  [https://github.com/chrisbanes/PhotoView]
     * @param data      T
     * @param position  位置
     */
    open fun onPictureShow(
        photoView: PhotoView,
        data: T,
        position: Int
    ) {
    }

    /**
     * 自定义view id
     */
    open fun getCustomLayoutId(): Int = -1

    /**
     * 进行自定义view的事件绑定等
     */
    open fun customLayoutConvert(
        view: View,
        data: T,
        position: Int
    ) {}

    /**
     * 使用自定义的导航器样式
     */
    open fun getIPagerNavigator():IPagerNavigator<MutableList<T>>{
        return getScaleCircleNavigator()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_picture_view)
        mDataList.value = mutableListOf()
        currentPosition.value=0
        initData(mDataList.value)
        initMagicIndicator()
        initViewPager()
        currentPosition.observe(this, Observer {
            tv_indicator.text="${currentPosition.value!!+1}/${mDataList.value!!.size}"
        })
        mDataList.observe(this, Observer {
            mViewPager?.adapter?.notifyDataSetChanged()
          if (it.size<10){
              magic_indicator.visibility= View.VISIBLE
              magic_indicator.navigator.apply {
                  iPagerNavigator?.setDataSet(mDataList.value!!)
                  notifyDataSetChanged()
              }
              tv_indicator.visibility = View.VISIBLE
              mViewPager!!.currentItem=currentPosition.value!!
          }else {
              magic_indicator.visibility= View.GONE
          }
        })
        //这个可以看做个管道  每次进入和退出的时候都会进行调用  进入的时候获取到前面传来的共享元素的信息
        //退出的时候 把这些信息传递给前面的activity
        //同时向sharedElements里面put view,跟对view添加transitionname作用一样
        setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: List<String>,
                sharedElements: MutableMap<String, View>) {
                sharedElements.clear()
                sharedElements["photoView"] = mViewPager!!.adapter!!.instantiateItem(mViewPager!!,mViewPager!!.currentItem) as View
            }
        })
    }

    private fun initViewPager() {
        mViewPager = findViewById(R.id.mViewPager)
        mViewPager!!.adapter = object : PagerAdapter() {
            override fun isViewFromObject(view: View, any: Any): Boolean = view == any

            override fun getCount(): Int = mDataList.value!!.size
            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                return if (getCustomLayoutId() != -1) {
                    val view = LayoutInflater.from(container.context)
                        .inflate(getCustomLayoutId(), container, false)
                    view.id = position
                    customLayoutConvert(view, mDataList.value!![position], position)
                    container.addView(view)
                    if (position==currentPosition.value){
                        view.transitionName="photoView"
                        supportStartPostponedEnterTransition()
                    }
                    view
                } else {
                    val photoView = PhotoView(container.context)
                    photoView.id = position
                    onPictureShow(photoView, mDataList.value!![position], position)
                    container.addView(photoView)
                    if(position==currentPosition.value){
                        photoView.transitionName="photoView"
                        supportStartPostponedEnterTransition()
                    }
                    photoView
                }
            }

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                container.removeView(`object` as View)
            }

            override fun getItemPosition(any: Any): Int {
                val view = any as View
                val index: Int = view.id
                return if (index >= 0) {
                    index
                } else POSITION_NONE
            }
        }
        mViewPager!!.addOnPageChangeListener(object :ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {
                magic_indicator.onPageScrollStateChanged(state)
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int) {
                magic_indicator.onPageScrolled(position, positionOffset, positionOffsetPixels)

            }

            override fun onPageSelected(position: Int) {
                magic_indicator.onPageSelected(position)
                currentPosition.value=position
            }

        })
    }

    private fun getScaleCircleNavigator(): ScaleCircleNavigator<MutableList<T>> {
        Log.d("BasePicture","getScaleCircleNavigator")
        val scaleCircleNavigator =
            ScaleCircleNavigator<MutableList<T>>(this)
        scaleCircleNavigator.setCircleCount(mDataList.value!!.size)
        scaleCircleNavigator.setNormalCircleColor(Color.LTGRAY)
        scaleCircleNavigator.setSelectedCircleColor(Color.DKGRAY)
        return scaleCircleNavigator
    }
    private fun initMagicIndicator() {
        iPagerNavigator=getIPagerNavigator()
        iPagerNavigator?.setDataSet(mDataList.value!!)
        iPagerNavigator?.setItemClickListener(object :OnItemClickListener{
            override fun onClick(index: Int) {
                mViewPager!!.currentItem = index
            }
        })
        magic_indicator.navigator = iPagerNavigator
        //ViewPagerHelper.bind(magic_indicator, mViewPager)
    }

    override fun supportFinishAfterTransition() {
        val data = Intent()
        data.putExtra("position", currentPosition.value)
        setResult(RESULT_OK, data)
        super.supportFinishAfterTransition()
    }


    override fun onBackPressed() {
        val data = Intent()
        data.putExtra("position", currentPosition.value)
        setResult(RESULT_OK, data)
        supportFinishAfterTransition()
    }
}
