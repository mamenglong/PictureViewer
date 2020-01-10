package com.mml.pictureviewer

import android.graphics.Color
import android.view.View
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.viewpager2.widget.ViewPager2
import com.github.chrisbanes.photoview.PhotoView

/**
 * Author: Menglong Ma
 * Email: mml2015@126.com
 * Date: 20-1-9 下午6:04
 * Description: This is BasePictureViewViewModel
 * Package: com.mml.pictureviewer
 * Project: PictureView
 */
class BasePictureViewViewModel: ViewModel(), LifecycleObserver {
    val mDataList = MutableLiveData<MutableList<Any>>()  // = viewModel.dataSet
    var currentPosition=  MutableLiveData<Int>()
    var currentPhotoView: PhotoView?= null
    var magicIndicatorNormalColor:  Int= Color.LTGRAY
    var magicIndicatorSelectColor:  Int =Color.DKGRAY

    var onPictureShow:((photoView: PhotoView, data: Any, position: Int)->Unit)?=null
    var onClickListener:((View)->Unit)?= null
    var onLongClickListener:((View)->Unit)?= null
    var customLayoutId :Int =-1
    var customLayoutConvert:((view: View, data: Any, position: Int)->Unit)? =null
}