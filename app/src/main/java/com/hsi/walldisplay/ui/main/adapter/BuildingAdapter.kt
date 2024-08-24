package com.hsi.walldisplay.ui.main.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.hsi.walldisplay.R
import com.hsi.walldisplay.application.BaseActivity
import com.hsi.walldisplay.databinding.RoomMoodItem
import com.hsi.walldisplay.helper.BuildingClickListener
import com.hsi.walldisplay.helper.ItemAnimation
import com.hsi.walldisplay.model.Building
import com.hsi.walldisplay.ui.main.viewmodel.RoomViewModel

class BuildingAdapter(
    val activity: BaseActivity,
    private var buildingClickListener: BuildingClickListener?,
    private val rooms: ArrayList<Building>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private var lastPosition = -1
    private val onAttach = true

    private var layoutInflater: LayoutInflater? = null

    private val animationType = ItemAnimation.RIGHT_LEFT

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.context)
        }
        val itemRow = DataBindingUtil
            .inflate<RoomMoodItem>(layoutInflater!!, R.layout.item_row_floor, parent, false)

        activity.setFontAndFontSize(itemRow.root)

        return ViewHolder(itemRow)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val room = rooms[position]
        val viewHolder = holder as ViewHolder
        val viewModel = RoomViewModel(room)
        viewHolder.roomItem.viewModel = viewModel
        viewHolder.roomItem.layoutEnd.visibility = if (position % 2 == 0) View.VISIBLE else View.GONE
        viewHolder.roomItem.rootLayout.setOnClickListener {
            if (buildingClickListener != null)
                buildingClickListener!!.onBuildingClickListener(room)
        }

        viewHolder.roomItem.tvRoomTitle.setOnLongClickListener {
            if (buildingClickListener != null)
                buildingClickListener!!.onBuildingTitleClickListener(room)
            true
        }

    }


    fun setData(rooms: List<Building>) {
        this.rooms.clear()
        this.rooms.addAll(rooms)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return rooms.size
    }

    class ViewHolder internal constructor(var roomItem: RoomMoodItem) : RecyclerView.ViewHolder(
        roomItem.root
    )
}