package com.hsi.walldisplay.ui.main.viewmodel

import android.view.View
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.transition.Visibility
import com.hsi.walldisplay.model.Curtain
import com.hsi.walldisplay.BR
import com.hsi.walldisplay.helper.Constants
import com.hsi.walldisplay.ui.main.MainActivity
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.properties.Delegates

class CurtainViewModel(
    val activity: MainActivity,
    curtain: Curtain
) : BaseObservable() {

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
    var progress: Int by Delegates.observable(
        0
    ) { _, _, _ ->
        notifyPropertyChanged(BR.progress)
    }

    fun changeVisibility(view: View) {
        layoutSeekBarVisibility = !layoutSeekBarVisibility
    }

    fun open(view: View) {
        activity.publishMessage(topic, getMessage("open"))
    }

    fun close(view: View) {
        activity.publishMessage(topic, getMessage("close"))
    }

    fun stop(view: View) {
        activity.publishMessage(topic, getMessage("stop"))
    }

    private fun getMessage(command: String): String {
        return "{\"id\":\"${curtain.serviceId}\",\"command\":\"$command\"}"
    }

}