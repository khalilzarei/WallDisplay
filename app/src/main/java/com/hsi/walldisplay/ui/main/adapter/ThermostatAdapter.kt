package com.hsi.walldisplay.ui.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.hsi.walldisplay.R
import com.hsi.walldisplay.databinding.ThermostatItem
import com.hsi.walldisplay.model.BuildingService
import com.hsi.walldisplay.ui.main.MainActivity
import com.hsi.walldisplay.ui.main.viewmodel.ThermostatViewModel


class ThermostatAdapter(
    val activity: MainActivity,
    private var items: ArrayList<BuildingService>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {

        val itemRow = DataBindingUtil.inflate<ThermostatItem>(
            LayoutInflater.from(parent.context), R.layout.item_row_thermostat, parent, false
        )
        activity.setFontAndFontSize(itemRow.root)
        return ViewHolder(itemRow)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val item = items[position]
        val viewHolder = holder as ViewHolder
        val viewModel = ThermostatViewModel(activity, item)
        viewHolder.item.viewModel = viewModel

        val topic = "${activity.sessionManager.user.projectId}/Thermo/In"


    }

    override fun getItemCount(): Int = items.size

    fun setItems(items: ArrayList<BuildingService>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    fun getItems() = items

    fun addItem(item: BuildingService) {
        items.add(item)
        notifyDataSetChanged()
    }

    fun removeItem(item: BuildingService) {
        items.remove(item)
        notifyDataSetChanged()
    }

    fun thermostatItem(item: BuildingService): Boolean {
        return items.contains(item)
    }

    class ViewHolder internal constructor(var item: ThermostatItem) : RecyclerView.ViewHolder(item.root)


}