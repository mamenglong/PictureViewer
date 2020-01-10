# PictureViewer
> 基于 [PhotoView](https://github.com/chrisbanes/PhotoView) 和[MagicIndicator](https://github.com/hackware1993/MagicIndicator)
封装的图片浏览框架,支持本地数据以及网络数据,用户自己决定采用什么图片家在框架

## 使用 查看图集并返回原来
* 继承 BasePictureSetViewActivity ,根据是否自定义实现部分方法即可
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
        
        
        class PictureViewActivity : BasePictureSetViewActivity<SkinInfo>() {
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

## 正常图片查看
* 继承 BasePictureViewActivity ,根据是否自定义实现部分方法即可

    ``` kotlin
      package com.mml.pictureview
    
    import androidx.lifecycle.MutableLiveData
    import com.github.chrisbanes.photoview.PhotoView
    
    
    class PictureViewActivity : com.mml.pictureviewer.BasePictureViewActivity() {
        val list = mutableListOf<Any>().apply {
            add(R.drawable.a)
            add(R.drawable.b)
            add(R.drawable.c)
            add(R.drawable.b)
            add(R.drawable.a)
    
        }
        override fun initData( mDataList: MutableLiveData<MutableList<Any>>) {
            intent?.let {
              val po=  it.getIntExtra("position",0)
                log("position:$po")
                viewModel.currentPosition.value=po
            }
            mDataList.value= list
    
        }
    
        override fun getOnPictureShow():((photoView: PhotoView, data: Any, position: Int)->Unit)?  {
           return { photoView, data, position ->
    
    
            }
        }
    
    }
    
    
    ```
* 在浏览页 重写
``` kotlin
    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        super.onActivityReenter(resultCode, data)
        data?.extras?.let {
            shareElementsBundle=it
        }
        shareElementsBundle?.let {
            val pos = it.getInt("position", 0)
            recyclerView.smoothScrollToPosition(pos)
            postponeEnterTransition()
            recyclerView.viewTreeObserver.addOnPreDrawListener(object : OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    recyclerView.viewTreeObserver.removeOnPreDrawListener(this)
                    // TODO: figure out why it is necessary to request layout here in order to get a smooth transition.
                    recyclerView.requestLayout()
                    startPostponedEnterTransition()
                    return true
                }
            })
        }
    }
   override fun onCreate(savedInstanceState: Bundle?) {
  ......
  setExitSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: MutableList<String>?,
                sharedElements: MutableMap<String, View>?
            ) {
                super.onMapSharedElements(names, sharedElements)
                shareElementsBundle?.let {
                    val pos=it.getInt("position",0)
                        sharedElements?.clear()
                        names?.clear()
                        val view=recyclerView.layoutManager!!.findViewByPosition(pos)
                        view?.let {
                            view.transitionName ="photoView-$pos"
                            sharedElements!!["photoView-$pos"]= view
                        }

                        shareElementsBundle=null
                    }
            }
        })
  }
```

