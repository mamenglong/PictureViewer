# PictureViewer
> 基于 [PhotoView](https://github.com/chrisbanes/PhotoView) 和[MagicIndicator](https://github.com/hackware1993/MagicIndicator)
封装的图片浏览框架,支持本地数据以及网络数据,用户自己决定采用什么图片家在框架

## 使用
* 继承 BasePictureViewActivity ,根据是否自定义实现部分方法即可
    ```kotlin
        package com.m.l.hydrogen.wallpaper.activity
        import android.content.Intent
        import android.os.Bundle
        import android.view.View
        import androidx.fragment.app.Fragment
        import com.github.chrisbanes.photoview.PhotoView
        import com.m.l.hydrogen.wallpaper.R
        import com.m.l.hydrogen.wallpaper.hero.HeroItem
        import com.m.l.hydrogen.wallpaper.hero.SkinInfo
        import com.mml.basewheel.util.GlideUtil
        import com.mml.core.log
        import com.mml.kotlinextension.showToast
        import com.mml.kotlinextension.startActivity
        import com.mml.pictureviewer.BasePictureViewActivity
        import com.mml.pictureviewer.IPagerNavigator
        import kotlinx.android.synthetic.main.item_picture_view.view.*
        
        
        class PictureViewActivity : BasePictureViewActivity<SkinInfo>() {
            private  var heroItem:HeroItem?=null
            override fun onNewIntent(intent: Intent?) {
                super.onNewIntent(intent)
                setIntent(intent)
                initData()
            }
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                intent.getBundleExtra("bundle")?.apply {
                    heroItem= getParcelable<HeroItem>("heroItem")
                }
                initData()
            }
            private fun initData(){
                heroItem?.let {
                    mDataList.value!!.clear()
                    mDataList.postValue(it.skin_info)
                }
            }
            override fun initData(data: MutableList<SkinInfo>?) {
                heroItem?.let {
                    data!!.addAll(it.skin_info)
                }
                mViewPager?.adapter?.notifyDataSetChanged()
            }
            companion object{
                fun startActivity(activity: Fragment,bundle: Bundle= Bundle()){
                    activity.startActivity<PictureViewActivity>(bundle)
                }
            }
            override fun getCustomLayoutId(): Int {
                return R.layout.item_picture_view
            }
            override fun customLayoutConvert(view: View, data: SkinInfo, position: Int) {
                view.photoView.apply {
                    GlideUtil.loadDefault(data.url, this)
                    setOnClickListener {
                        finish()
                    }
                    setOnLongClickListener {
                        showToast("长按:${data.name}")
                        true
                    }
                }
                view.tv_name.text=data.name
            }
        }

    
    ```
