package com.hsi.walldisplay.ui.main.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.hsi.walldisplay.R
import com.hsi.walldisplay.databinding.SceneItem
import com.hsi.walldisplay.helper.SceneClickListener
import com.hsi.walldisplay.model.HomeScene
import com.hsi.walldisplay.ui.main.MainActivity
import com.hsi.walldisplay.ui.main.viewmodel.SceneViewModel

class SceneAdapter(
    val activity: MainActivity,
    var sceneClickListener: SceneClickListener?,
    val homeScenes: ArrayList<HomeScene>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var layoutInflater: LayoutInflater? = null


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.context)
        }
        val itemRow = DataBindingUtil
            .inflate<SceneItem>(layoutInflater!!, R.layout.item_row_scene, parent, false)

        activity.setFontAndFontSize(itemRow.root)
        return ViewHolder(itemRow)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val homeScene = homeScenes[position]
        Log.e("SceneAdapter", "Scene id: ${homeScene.id}  ${homeScene.sceneId}  ${homeScene.masterId}  ${homeScene.name}")
        val viewHolder = holder as ViewHolder
        val viewModel = SceneViewModel(homeScene)
        viewHolder.sceneItem.viewModel = viewModel

        viewHolder.sceneItem.layoutEnd.visibility = if (position % 2 == 0) View.VISIBLE else View.GONE

        viewHolder.sceneItem.layoutImage.setOnClickListener { view: View? ->
            if (sceneClickListener != null)
                sceneClickListener!!.onSceneClickListener(homeScene)
        }
        viewHolder.sceneItem.layoutTitle.setOnClickListener { view: View? ->
            if (sceneClickListener != null)
                sceneClickListener!!.onSceneClickListener(homeScene)
        }

        viewHolder.sceneItem.layoutImage.setOnLongClickListener {
            if (sceneClickListener != null)
                sceneClickListener!!.onSceneImageLongClickListener(homeScene);

            true
        }

        viewHolder.sceneItem.layoutTitle.setOnLongClickListener {
            if (sceneClickListener != null)
                sceneClickListener!!.onSceneTitleLongClickListener(homeScene);

            true
        }
    }

    fun setData(homeScenes: List<HomeScene>) {
        this.homeScenes.clear()
        this.homeScenes.addAll(homeScenes)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return homeScenes.size
    }


    class ViewHolder internal constructor(var sceneItem: SceneItem) : RecyclerView.ViewHolder(sceneItem.root)
}