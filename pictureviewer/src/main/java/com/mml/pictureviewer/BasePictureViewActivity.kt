package com.mml.pictureviewer

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.github.chrisbanes.photoview.PhotoView
import kotlinx.android.synthetic.main.activity_base_picture_view.*
import net.lucode.hackware.magicindicator.ViewPagerHelper

abstract class BasePictureViewActivity<T> : AppCompatActivity() {

    /**
     * 初始化数据类型
     */
    abstract fun initData(data:MutableList<T>?)
    /**
     * 使用自己的图片加载框架,用户自己决定采用什么方式显示
     * @param photoView  支持缩放的imageview  [https://github.com/chrisbanes/PhotoView]
     * @param data      T
     * @param position  位置
     */
    abstract fun onPictureShow(
        photoView: PhotoView,
        data: T,
        position: Int
    )
    val mDataList= MutableLiveData<MutableList<T>>()
    protected var mViewPager:ViewPager?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_picture_view)
        mDataList.value= mutableListOf()
        initData(mDataList.value)
        initViewPager()
        initMagicIndicator()
        mDataList.observe(this, Observer {
            mViewPager?.adapter?.notifyDataSetChanged()
            magic_indicator.navigator.notifyDataSetChanged()
        })
    }


    private fun initViewPager(){
        mViewPager = findViewById(R.id.mViewPager)
        mViewPager!!.adapter=object : PagerAdapter() {
            override fun isViewFromObject(view: View, any: Any): Boolean =view==any

            override fun getCount(): Int =mDataList.value!!.size
            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                val photoView = PhotoView(container.context)
                onPictureShow(photoView,mDataList.value!![position],position)
                photoView.id = position
                container.addView(photoView)
                return photoView
            }

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                container.removeView(`object` as View)
            }

            override fun getItemPosition(any: Any): Int {
                val photoView =any as PhotoView

                val index: Int = photoView.id
                return if (index >= 0) {
                    index
                } else POSITION_NONE
            }
        }
    }
    protected fun initMagicIndicator(){
        val scaleCircleNavigator =
            ScaleCircleNavigator(this)
        scaleCircleNavigator.setCircleCount(mDataList.value!!.size)
        scaleCircleNavigator.setNormalCircleColor(Color.LTGRAY)
        scaleCircleNavigator.setSelectedCircleColor(Color.DKGRAY)
        scaleCircleNavigator.setCircleClickListener {
            mViewPager!!.currentItem = it
        }
        magic_indicator.navigator = scaleCircleNavigator
        ViewPagerHelper.bind(magic_indicator, mViewPager)
    }
}
