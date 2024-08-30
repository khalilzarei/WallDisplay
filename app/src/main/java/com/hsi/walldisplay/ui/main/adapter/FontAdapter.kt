package com.hsi.walldisplay.ui.main.adapter

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.hsi.walldisplay.databinding.ItemRowFontBinding
import com.hsi.walldisplay.model.FontItem
import com.hsi.walldisplay.ui.main.MainActivity


class FontAdapter(val context: MainActivity, private var items: ArrayList<FontItem>) : BaseAdapter() {


    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    //    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
//        val view: View
//        val vh: ItemRowHolder
//        if (convertView == null) {
//            view = mInflater.inflate(com.hsi.walldisplay.R.layout.item_row_font, parent, false)
//            vh = ItemRowHolder(view)
//            view?.tag = vh
//        } else {
//            view = convertView
//            vh = view.tag as ItemRowHolder
//        }
//
//        vh.label.text = items[position].name
//        return view
//    }
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val itemRowFontBinding: ItemRowFontBinding by lazy { ItemRowFontBinding.inflate(context.layoutInflater) }
        itemRowFontBinding.tvTitle.gravity = Gravity.CENTER
        itemRowFontBinding.tvTitle.text = items[position].name
        return itemRowFontBinding.root
    }

    override fun getView(position: Int, view: View?, viewgroup: ViewGroup?): View {
        val itemRowFontBinding: ItemRowFontBinding by lazy { ItemRowFontBinding.inflate(context.layoutInflater) }
        itemRowFontBinding.tvTitle.text = items[position].name
        itemRowFontBinding.bottomLine.visibility = View.GONE
        return itemRowFontBinding.root
    }

    override fun getItem(position: Int): FontItem {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return items.size
    }

}