package com.mml.pictureviewer

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.github.chrisbanes.photoview.PhotoView
import kotlinx.android.synthetic.main.activity_base_picture_view.*
import net.lucode.hackware.magicindicator.ViewPagerHelper

abstract class BasePictureViewActivity<T> : AppCompatActivity() {

    /**
     * 初始化数据类型
     */
    abstract fun initData(data:MutableList<T>)
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
    val mDataList= mutableListOf<T>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_picture_view)
        initData(mDataList)
        initViewPager()
        initMagicIndicator()
    }


    private fun initViewPager(){
        mViewPager.adapter=object : PagerAdapter() {
            override fun isViewFromObject(view: View, any: Any): Boolean =view==any

            override fun getCount(): Int =mDataList.size
            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                val photoView = PhotoView(container.context)
                photoView.tag=mDataList[position]
                onPictureShow(photoView,mDataList[position],position)
                container.addView(photoView)
                return photoView
            }

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                container.removeView(`object` as View)
            }

            override fun getItemPosition(any: Any): Int {
                val photoView =any as PhotoView

                val index: Int = mDataList.indexOf(photoView.tag as T)
                return if (index >= 0) {
                    index
                } else POSITION_NONE
            }
        }
    }
    private fun initMagicIndicator(){
        val scaleCircleNavigator =
            ScaleCircleNavigator(this)
        scaleCircleNavigator.setCircleCount(mDataList.size)
        scaleCircleNavigator.setNormalCircleColor(Color.LTGRAY)
        scaleCircleNavigator.setSelectedCircleColor(Color.DKGRAY)
        scaleCircleNavigator.setCircleClickListener {
            mViewPager.currentItem = it
        }
        magic_indicator.navigator = scaleCircleNavigator
        ViewPagerHelper.bind(magic_indicator, mViewPager)
    }
}
