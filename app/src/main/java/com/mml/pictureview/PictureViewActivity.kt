package com.mml.pictureview

import com.github.chrisbanes.photoview.PhotoView


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
