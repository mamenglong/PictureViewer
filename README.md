# PictureViewer
> 基于 [PhotoView](https://github.com/chrisbanes/PhotoView) 和[MagicIndicator](https://github.com/hackware1993/MagicIndicator)
封装的图片浏览框架,支持本地数据以及网络数据,用户自己决定采用什么图片家在框架

## 使用
* 继承 BasePictureViewActivity ,实现 onPictureShow和initData即可
    ```kotlin
    class PictureViewActivity : com.mml.pictureviewer.BasePictureViewActivity<String>() {
        override fun onPictureShow(
            photoView: PhotoView,
            position: String,
            position1: Int
        ) {
    
        }
    
        override fun initData(data: MutableList<String>) {
            ('a'..'z').forEach {
                data.add(it.toString())
            }
        }                   
    
    }
    
    ```
