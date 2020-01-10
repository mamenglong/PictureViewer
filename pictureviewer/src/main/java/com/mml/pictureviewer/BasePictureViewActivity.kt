package com.mml.pictureviewer

import android.app.SharedElementCallback
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.github.chrisbanes.photoview.PhotoView
import kotlinx.android.synthetic.main.activity_base_picture_view.*

/**
 * 支持自定义布局,需要重写 [getCustomLayoutId] [getCustomLayoutConvert] 方法
 *
 * 默认布局 重写 [getOnClickListener] [getOnLongClickListener] [getOnPictureShow]
 */
abstract class BasePictureViewActivity: AppCompatActivity() {
    protected lateinit var viewModel: BasePictureViewViewModel
    //live data 数据
    protected lateinit var mViewPager: ViewPager2
    private var iPagerNavigator:IPagerNavigator<MutableList<Any>>? = null

    fun log(msg:String){
        Log.i("BasePictureSetView",msg)
    }
    /**
     * 初始化数据类型 ,初始化viewModel里的数据
     */
    abstract fun initData(mDataList: MutableLiveData<MutableList<Any>>)

    /**
     * 使用自己的图片加载框架,用户自己决定采用什么方式显示
     * @param photoView  支持缩放的imageview  [https://github.com/chrisbanes/PhotoView]
     * @param data      T
     * @param position  位置
     */
    open fun getOnPictureShow( ):((photoView: PhotoView, data: Any, position: Int)->Unit) ? {
          return null
    }

    /**
     * 默认布局的点击事件,或者调用 [getOnPictureShow]依据 参数自己实现点击事件,同时注意共享元素的执行
     */
    open fun getOnClickListener( ): ((View)->Unit)?{
        return null
    }
    /**
     * 默认布局的长点击事件
     */
    open fun getOnLongClickListener( ): ((View)->Unit)? {
        return null
    }
    /**
     *此方法调用需要设置 [getCustomLayoutConvert]
     */
    open fun getCustomLayoutId():Int =-1
    /**
     * 进行自定义view的事件绑定等,此生效前提是 [getCustomLayoutId]不为-1
     */
    open fun getCustomLayoutConvert():((view: View, data: Any, position: Int)->Unit)? =null

    /**
     * 使用自定义的导航器样式
     */
    open fun getIPagerNavigator():IPagerNavigator<MutableList<Any>>{
        return getScaleCircleNavigator()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_picture_view)
        initVM()
        supportPostponeEnterTransition() //延缓执行
        initViewModelData()
        initData(viewModel.mDataList)
        initMagicIndicator()
        initViewPager()
        initShareElement()
    }
    private fun initViewModelData(){
        viewModel.mDataList.value = mutableListOf()
        viewModel.currentPosition.value=0
        viewModel.onPictureShow=getOnPictureShow()
        viewModel.onClickListener=getOnClickListener()
        viewModel.onLongClickListener=getOnLongClickListener()
        viewModel.customLayoutId = getCustomLayoutId()
        viewModel.customLayoutConvert =getCustomLayoutConvert()
        viewModel.currentPosition.observe(this, Observer { it ->
            log("observe->viewModel.currentPosition.value:${viewModel.currentPosition.value} it:$it")
            tv_indicator.text=String.format("%d/%d",viewModel.currentPosition.value!!+1,viewModel.mDataList.value!!.size)
            viewModel.mDataList.value?.let {
                if (it.size<10){
                    magic_indicator.visibility= View.VISIBLE
                    magic_indicator.navigator.apply {
                        iPagerNavigator?.setDataSet(viewModel.mDataList.value!!)
                        notifyDataSetChanged()
                    }
                    tv_indicator.visibility = View.VISIBLE
                }else {
                    tv_indicator.visibility = View.VISIBLE
                    magic_indicator.visibility= View.GONE
                }
            }
        })

    }
    private fun initShareElement(){
        //这个可以看做个管道  每次进入和退出的时候都会进行调用  进入的时候获取到前面传来的共享元素的信息
        //退出的时候 把这些信息传递给前面的activity
        //同时向sharedElements里面put view,跟对view添加transitionname作用一样
        setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: List<String>,
                sharedElements: MutableMap<String, View>
            ) {
                sharedElements.clear()
                viewModel.currentPhotoView?.let {
                    sharedElements["photoView-${viewModel.currentPosition.value}"] =  it
                }
            }
        })
    }
    override fun onDestroy() {
        super.onDestroy()
        if (this::viewModel.isInitialized)
            lifecycle.removeObserver(viewModel)
    }
    private fun initVM(){
        viewModel=  ViewModelProvider(this).get(BasePictureViewViewModel::class.java)
        lifecycle.addObserver(viewModel)
    }
    private fun initViewPager() {
        mViewPager = findViewById(R.id.mViewPager)
        mViewPager.adapter=BaseViewPagerAdapter(this,viewModel.mDataList.value!!)
        mViewPager.setCurrentItem(viewModel.currentPosition.value!!,false)
        mViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                log("onPageScrollStateChanged: state:${state}")
                magic_indicator.onPageScrollStateChanged(state)
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int) {
                log("onPageScrolled: position:${position}  positionOffset:$positionOffset positionOffsetPixels:$positionOffsetPixels")
                magic_indicator.onPageScrolled(position, positionOffset, positionOffsetPixels)

            }

            override fun onPageSelected(position: Int) {
                log("onPageSelected: position:${position}")
                magic_indicator.onPageSelected(position)
                viewModel.currentPosition.value=position
                mViewPager.tag = position
            }

        })

    }

    private fun getScaleCircleNavigator(): ScaleCircleNavigator<MutableList<Any>> {
        log( "getScaleCircleNavigator")
        val scaleCircleNavigator =
            ScaleCircleNavigator<MutableList<Any>>(this)
        scaleCircleNavigator.setCircleCount(viewModel.mDataList.value!!.size)
        scaleCircleNavigator.setNormalCircleColor(viewModel.magicIndicatorNormalColor)
        scaleCircleNavigator.setSelectedCircleColor(viewModel.magicIndicatorSelectColor)
        return scaleCircleNavigator
    }
    private fun initMagicIndicator() {
        iPagerNavigator=getIPagerNavigator()
        iPagerNavigator?.setDataSet(viewModel.mDataList.value!!)
        iPagerNavigator?.setItemClickListener(object :OnItemClickListener{
            override fun onClick(index: Int) {
                mViewPager.currentItem = index
            }
        })
        magic_indicator.navigator = iPagerNavigator
        magic_indicator.onPageSelected(viewModel.currentPosition.value!!)
        //ViewPagerHelper.bind(magic_indicator, mViewPager)
    }

    override fun supportFinishAfterTransition() {
        val data = Intent()
        data.putExtra("position", viewModel.currentPosition.value)
        setResult(RESULT_OK, data)
        super.supportFinishAfterTransition()
    }


    override fun onBackPressed() {
        val data = Intent()
        data.putExtra("position", viewModel.currentPosition.value)
        setResult(RESULT_OK, data)
        super.supportFinishAfterTransition()
    }
}

class BaseViewPagerAdapter(fragmentActivity: FragmentActivity,var list:MutableList<Any>):FragmentStateAdapter(fragmentActivity){
    override fun getItemCount(): Int =list.size

    override fun createFragment(position: Int): Fragment {
        return BasePictureViewFragment.newInstance(position)
    }


}