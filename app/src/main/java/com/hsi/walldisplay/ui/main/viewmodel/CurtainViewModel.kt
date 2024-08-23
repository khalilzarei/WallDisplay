package com.hsi.walldisplay.ui.main.viewmodel

import android.view.View
import android.widget.TextView
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.BindingAdapter
import com.hsi.walldisplay.BR
import com.hsi.walldisplay.helper.Constants
import com.hsi.walldisplay.model.Curtain
import com.hsi.walldisplay.ui.main.MainActivity
import kotlin.properties.Delegates

class CurtainViewModel(
    val activity: MainActivity, curtain: Curtain
) : BaseObservable() {

    companion object {

        @JvmStatic
        @BindingAdapter("setCurtainPercent")
        fun setCurtainPercent(textView: TextView, value: Int) {
            textView.text = when (value) {
                100 -> "Open"
                0 -> "Closed"
                else -> {
                    "$value%"
                }
            }
        }
    }

    val topic = "${activity.sessionManager.user.projectId}/${Constants.CURTAIN_IN}"

    @get:Bindable
    var curtain: Curtain by Delegates.observable(curtain) { _, _, _ ->
        notifyPropertyChanged(BR.curtain)
    }


    @get:Bindable
    var layoutSeekBarVisibility: Boolean by Delegates.observable(
        false
    ) { _, _, _ ->
        notifyPropertyChanged(BR.layoutSeekBarVisibility)
    }

    @get:Bindable
    var curtainValue: Int by Delegates.observable(
        curtain.value!!
    ) { _, _, _ ->
        notifyPropertyChanged(BR.curtainValue)
    }

    fun changeVisibility(view: View) {
        layoutSeekBarVisibility = !layoutSeekBarVisibility
        curtain.showLayout = layoutSeekBarVisibility
    }

    fun open(view: View) {
        activity.publishMessage(topic, getMessage("open"))
        curtainValue = 100
        updateCurtain()
    }

    fun close(view: View) {
        activity.publishMessage(topic, getMessage("close"))
        curtainValue = 0
        updateCurtain()
    }

    fun updateCurtain(value: Int = curtainValue) {
        curtain.value = value
        curtain.showLayout = layoutSeekBarVisibility
        activity.dataBaseDao.updateCurtainValue(curtain.id!!, value)
//        activity.viewModel.curtainAdapter.setItems(
//            activity.dataBaseDao.getBuildingCurtain(activity.viewModel.building.id) as ArrayList
//        )
    }

    fun stop(view: View) {
        activity.publishMessage(topic, getMessage("stop"))
    }

    private fun getMessage(command: String): String {
        return "{\"id\":\"${curtain.serviceId}\",\"command\":\"$command\"}"
    }

}