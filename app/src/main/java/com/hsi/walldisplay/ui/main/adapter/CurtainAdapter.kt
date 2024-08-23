package com.hsi.walldisplay.ui.main.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.hsi.walldisplay.R
import com.hsi.walldisplay.databinding.CurtainItem
import com.hsi.walldisplay.helper.Constants
import com.hsi.walldisplay.model.Curtain
import com.hsi.walldisplay.ui.main.MainActivity
import com.hsi.walldisplay.ui.main.viewmodel.CurtainViewModel


class CurtainAdapter(
    val activity: MainActivity,
    private var curtains: ArrayList<Curtain>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {

        val itemRow = DataBindingUtil.inflate<CurtainItem>(
            LayoutInflater.from(parent.context), R.layout.item_row_curtain, parent, false
        )
        return ViewHolder(itemRow)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val item = curtains[position]
        val viewHolder = holder as ViewHolder
        val viewModel = CurtainViewModel(activity, item)
        viewHolder.item.viewModel = viewModel

        viewHolder.item.layoutSeekBar.visibility = View.GONE
        val seekBar = viewHolder.item.seekBar
        val tvSeekResult = viewHolder.item.tvSeekResult

        val topic = "${activity.sessionManager.user.projectId}/${Constants.CURTAIN_IN}"
//        seekBar.thumb = if (item.value!!.toInt() > 0) activity.getDrawable(R.drawable.seekbar_thumb) else null
        seekBar.progress = item.value!!
        viewModel.curtainValue = item.value!!
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                tvSeekResult.text = "$progress%"
                viewModel.curtainValue = progress
                if (progress > 0) {
                    seekBar.thumb = activity.getDrawable(R.drawable.seekbar_thumb)
                    viewModel.curtain.value = progress
                } else {
                    seekBar.thumb = null
                }

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                seekBar.thumb = activity.getDrawable(R.drawable.seekbar_thumb)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val dim: Int = seekBar.progress
                activity.logD("onStopTrackingTouch $dim")
                if (dim in 0..100) {
                    val message = "{\"id\":\"${item.serviceId}\",\"command\":\"setposition_$dim\"}"
                    viewModel.curtainValue = dim
                    viewModel.updateCurtain(dim)
                    activity.publishMessage(topic, message)
                }
            }

        })

    }

    override fun getItemCount(): Int = curtains.size

    fun setItems(items: ArrayList<Curtain>) {
        this.curtains.clear()
        this.curtains.addAll(items)
        notifyDataSetChanged()
    }

    fun getCurtains() = curtains

    fun addItem(item: Curtain) {
        curtains.add(item)
        notifyDataSetChanged()
    }

    fun removeItem(item: Curtain) {
        curtains.remove(item)
        notifyDataSetChanged()
    }

    fun containsItem(item: Curtain): Boolean {
        return curtains.contains(item)
    }

    class ViewHolder internal constructor(var item: CurtainItem) : RecyclerView.ViewHolder(item.root)


}