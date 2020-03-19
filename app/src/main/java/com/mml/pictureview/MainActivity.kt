package com.mml.pictureview

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnPreDrawListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.app.SharedElementCallback
import androidx.core.util.Pair
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_pic.view.*


class MainActivity : AppCompatActivity() {

    private var shareElementsBundle: Bundle? = null
    private var position= 0
    val list = mutableListOf<Int>().apply {
        add(R.drawable.a)
        add(R.drawable.b)
        add(R.drawable.c)
        add(R.drawable.b)
        add(R.drawable.a)
        add(R.drawable.c)
        add(R.drawable.b)
        add(R.drawable.a)
 
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initRecyclerView()
      /*  setExitSharedElementCallback(object : SharedElementCallback() {
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
        })*/
    }
    private fun initRecyclerView(){
        recyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity,2)
            adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): RecyclerView.ViewHolder {
                  val view =LayoutInflater.from(parent.context).inflate(R.layout.item_pic,parent,false)
                    return object :RecyclerView.ViewHolder(view){}
                }

                override fun getItemCount(): Int =list.size

                override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                    this@MainActivity.position= position
                      holder.itemView.image.setImageResource(list[position])
                    holder.itemView.image.transitionName= "photoView-$position"
                    holder.itemView.image.setOnClickListener {
                        it.tag=position
                        this@MainActivity.position= position
                        Toast.makeText(this@MainActivity,"click:$position",Toast.LENGTH_SHORT).show()
                        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                this@MainActivity, Pair(it, it.transitionName)
                        )
                        startActivity(Intent(this@MainActivity,PictureViewActivity::class.java).apply {
                            putExtra("position",position)
                        }
                            ,options.toBundle())
                    }
                }
               
            }
        }
    }

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
}
