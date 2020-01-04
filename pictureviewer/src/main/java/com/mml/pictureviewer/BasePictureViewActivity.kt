package com.mml.pictureviewer

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.github.chrisbanes.photoview.PhotoView
import kotlinx.android.synthetic.main.activity_base_picture_view.*
import net.lucode.hackware.magicindicator.ViewPagerHelper

/**
 * 使用自定义view 重写 [getCustomLayoutId]和[customLayoutConvert]
 * 使用默认view 重写 [onPictureShow]即可
 * 重写[setIPagerNavigator]即可设置自定义的导航器样式
 */
abstract class BasePictureViewActivity<T> : AppCompatActivity() {
    //live data 数据
    protected val mDataList = MutableLiveData<MutableList<T>>()
    protected var mViewPager: ViewPager? = null
    private var iPagerNavigator:IPagerNavigator<MutableList<T>>? = null

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
    open fun setIPagerNavigator():IPagerNavigator<MutableList<T>>{
        return getScaleCircleNavigator()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_picture_view)
        mDataList.value = mutableListOf()
        initData(mDataList.value)
        initViewPager()
        initMagicIndicator()
        mDataList.observe(this, Observer {
            mViewPager?.adapter?.notifyDataSetChanged()
            magic_indicator.navigator.apply {
                iPagerNavigator?.setDataSet(mDataList.value!!)
                notifyDataSetChanged()
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
                    view
                } else {
                    val photoView = PhotoView(container.context)
                    photoView.id = position
                    onPictureShow(photoView, mDataList.value!![position], position)
                    container.addView(photoView)
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
        iPagerNavigator=setIPagerNavigator()
        iPagerNavigator?.setDataSet(mDataList.value!!)
        iPagerNavigator?.setItemClickListener(object :OnItemClickListener{
            override fun onClick(index: Int) {
                mViewPager!!.currentItem = index
            }
        })
        magic_indicator.navigator = iPagerNavigator
        ViewPagerHelper.bind(magic_indicator, mViewPager)
    }
}
