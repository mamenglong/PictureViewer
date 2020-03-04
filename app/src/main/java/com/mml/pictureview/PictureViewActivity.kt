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
            photoView.setImageResource(viewModel.mDataList.value!![position] as Int)
       }
    }

}
