package com.mml.pictureviewer

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.github.chrisbanes.photoview.PhotoView
import kotlinx.android.synthetic.main.activity_base_picture_set_view.*


/**
 * 此采用共享元素,使用请时请注意
 * 使用自定义view 重写 [getCustomLayoutId]和[customLayoutConvert]
 * 使用默认view 重写 [onPictureShow]即可
 * 重写[getIPagerNavigator]即可设置自定义的导航器样式
 *
 * 支持设置中间页 [currentPosition]
 */
abstract class BasePictureSetViewActivity<T> : AppCompatActivity() {
    //live data 数据
    protected val mDataList = MutableLiveData<MutableList<T>>()
    protected lateinit var mViewPager: ViewPager2
    private var iPagerNavigator:IPagerNavigator<MutableList<T>>? = null
    protected var enterPosition=  MutableLiveData<Int>()
    protected var currentPosition=  MutableLiveData<Int>()
    fun log(msg:String){
        Log.i("BasePictureSetView",msg)
    }
    /**
     * 初始化数据类型
     */
    abstract fun initData(data: MutableLiveData<MutableList<T>>)

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
        setContentView(R.layout.activity_base_picture_set_view)
        supportPostponeEnterTransition() //延缓执行
        mDataList.value = mutableListOf()
        currentPosition.value=0
        initData(mDataList)
        initMagicIndicator()
        initViewPager()
        currentPosition.observe(this, Observer {
            tv_indicator.text="${currentPosition.value!!+1}/${mDataList.value!!.size}"
        })
        mDataList.observe(this, Observer {
            mViewPager.adapter?.notifyDataSetChanged()
          if (it.size<10){
              magic_indicator.visibility= View.VISIBLE
              magic_indicator.navigator.apply {
                  iPagerNavigator?.setDataSet(mDataList.value!!)
                  notifyDataSetChanged()
              }
              tv_indicator.visibility = View.VISIBLE
              mViewPager.currentItem=currentPosition.value!!
          }else {
              tv_indicator.visibility = View.VISIBLE

              magic_indicator.visibility= View.GONE
          }
        })
    }

    private fun initViewPager() {
        mViewPager = findViewById(R.id.mViewPager)
        mViewPager.adapter =object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                return if (getCustomLayoutId() != -1) {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(getCustomLayoutId(), parent, false)
                  object : RecyclerView.ViewHolder(view) {}
                } else {
                    val photoView = PhotoView(parent.context)
                    photoView.layoutParams=LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT)
                    photoView.scaleType = ImageView.ScaleType.FIT_CENTER
                    object : RecyclerView.ViewHolder(photoView) {}
                }
            }

            override fun getItemCount(): Int = mDataList.value!!.size

            override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
                super.onViewRecycled(holder)
                val name=holder.itemView.tag
                log("onViewRecycled: $name")
            }
            //显示时调用
            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                log("onBindViewHolder: $position")
                log("onBindViewHolder: currentPosition.value:${currentPosition.value}")
                holder.itemView.tag= position
                customLayoutConvert(holder.itemView,mDataList.value!![position],position)
                if (position==currentPosition.value){
                    if (getCustomLayoutId()==-1)
                        holder.itemView.transitionName="photoView-${enterPosition.value}"
                }else{
                    if (getCustomLayoutId()==-1)
                        holder.itemView.transitionName=null
                }
            }
        }
        mViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
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
                mViewPager.tag = position
                log("onPageSelected: currentPosition.value:${currentPosition.value}")
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
                mViewPager.currentItem = index
            }
        })
        magic_indicator.navigator = iPagerNavigator
        //ViewPagerHelper.bind(magic_indicator, mViewPager)
    }

    override fun supportFinishAfterTransition() {
        val data = Intent()
        data.putExtra("position", enterPosition.value)
        setResult(RESULT_OK, data)
        super.supportFinishAfterTransition()
    }


    override fun onBackPressed() {
        val data = Intent()
        data.putExtra("position", enterPosition.value)
        setResult(RESULT_OK, data)
        super.supportFinishAfterTransition()
    }
}