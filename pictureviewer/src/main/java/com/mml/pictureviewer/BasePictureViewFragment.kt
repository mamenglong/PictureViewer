package com.mml.pictureviewer


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.chrisbanes.photoview.PhotoView
import kotlinx.android.synthetic.main.fragment_base_picture_view.view.*


/**
 * A simple [Fragment] subclass.
 * Use the [BasePictureViewFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BasePictureViewFragment : Fragment() {
    lateinit var  viewModel:BasePictureViewViewModel
    private var position: Int=0
    val ARG_PARAM_POSITION = "arg_param_position"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            position = it.getInt(ARG_PARAM_POSITION)
        }
       viewModel=  ViewModelProvider(activity!!).get(BasePictureViewViewModel::class.java)
        lifecycle.addObserver(viewModel)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::viewModel.isInitialized)
            lifecycle.removeObserver(viewModel)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var rootView:View
        if (viewModel.customLayoutId==-1) {
            Log.i("Fragment", "default view ")
            rootView = inflater.inflate(R.layout.fragment_base_picture_view, container, false)
            rootView.photoView.transitionName = "photoView-${position}"
            Log.i("Fragment", "default view transitionName:${rootView.photoView.transitionName} ")
            viewModel.onPictureShow?.invoke(rootView.photoView,viewModel.mDataList.value!![position],position)
            Log.i("Fragment", "default view transitionName:${rootView.photoView.transitionName} ")
            Log.i(
                "Fragment",
                "position:${position} viewModel.currentPosition.value:${viewModel.currentPosition.value} view.photoView.transitionName :${rootView.photoView.transitionName}"
            )
            rootView.photoView.setOnClickListener {
                Log.i(
                    "Fragment",
                    "OnClickListener-> position:${position} setOnClickListener viewModel.currentPosition.value:${viewModel.currentPosition.value} view.photoView.transitionName :${rootView.photoView.transitionName}"
                )
                viewModel.currentPhotoView = it as PhotoView
                viewModel.onClickListener?.invoke(it,viewModel.mDataList.value!![position],position)
                activity?.supportFinishAfterTransition()
            }
            rootView.photoView.setOnLongClickListener {
                Log.i(
                    "Fragment",
                    "OnLongClickListener-> position:${position} setOnClickListener viewModel.currentPosition.value:${viewModel.currentPosition.value} view.photoView.transitionName :${rootView.photoView.transitionName}"
                )
                viewModel.onLongClickListener?.invoke(it,viewModel.mDataList.value!![position],position)
                return@setOnLongClickListener true
            }
            rootView.photoView.viewTreeObserver.addOnPreDrawListener(object :
                ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    viewModel.currentPhotoView = rootView.photoView
                    rootView.photoView.viewTreeObserver.removeOnPreDrawListener(this)
                    activity?.supportStartPostponedEnterTransition()
                    return true
                }
            })
        }else{
            Log.i("Fragment", "custom view ")
            rootView = inflater.inflate(viewModel.customLayoutId, container, false)
            viewModel.customLayoutConvert?.invoke(rootView,viewModel.mDataList.value!![position],position)

        }
        return rootView
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param position Parameter 1.
         * @return A new instance of fragment BasePictureViewFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(position: Int) =
            BasePictureViewFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM_POSITION, position)
                }
            }
    }
}
