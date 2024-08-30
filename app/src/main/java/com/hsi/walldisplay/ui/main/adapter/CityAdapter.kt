package com.hsi.walldisplay.ui.main.adapter

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.hsi.walldisplay.databinding.ItemRowFontBinding
import com.hsi.walldisplay.model.CityItem
import com.hsi.walldisplay.ui.main.MainActivity


class CityAdapter(val context: MainActivity, private var items: ArrayList<CityItem>) : BaseAdapter() {

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val item = items[position]
        val itemRowFontBinding: ItemRowFontBinding by lazy { ItemRowFontBinding.inflate(context.layoutInflater) }
        itemRowFontBinding.tvTitle.gravity = Gravity.CENTER
        itemRowFontBinding.tvTitle.text = "${item.name},${item.country}"
        return itemRowFontBinding.root
    }

    override fun getView(position: Int, view: View?, viewgroup: ViewGroup?): View {
        val item = items[position]
        val itemRowFontBinding: ItemRowFontBinding by lazy { ItemRowFontBinding.inflate(context.layoutInflater) }
        itemRowFontBinding.tvTitle.text = "${item.name},${item.country}"
        itemRowFontBinding.bottomLine.visibility = View.GONE
        return itemRowFontBinding.root
    }

    override fun getItem(position: Int): CityItem {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return items.size
    }

}