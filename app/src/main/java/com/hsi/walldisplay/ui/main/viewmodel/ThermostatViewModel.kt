package com.hsi.walldisplay.ui.main.viewmodel

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.hsi.walldisplay.BR
import com.hsi.walldisplay.R
import com.hsi.walldisplay.helper.Constants
import com.hsi.walldisplay.model.BuildingService
import com.hsi.walldisplay.model.FanSpeed
import com.hsi.walldisplay.ui.main.MainActivity
import com.hsi.walldisplay.view.ArcSeekBar
import com.hsi.walldisplay.view.ArcSeekBar.OnProgressChangeListener
import kotlin.properties.Delegates


class ThermostatViewModel(
    val activity: MainActivity,
    thermostat: BuildingService
) : BaseObservable() {


    val topic = "${activity.sessionManager.user.projectId}/${Constants.CURTAIN_IN}"

    @get:Bindable
    var thermostat: BuildingService by Delegates.observable(thermostat) { _, _, _ ->
        notifyPropertyChanged(BR.thermostat)
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

    @get:Bindable
    var thermostatProgress: Int by Delegates.observable(
        150
    ) { _, _, _ ->
        notifyPropertyChanged(BR.thermostatProgress)
    }

    @get:Bindable
    var fanSpeed: String by Delegates.observable(
        ""
    ) { _, _, _ ->
        notifyPropertyChanged(BR.fanSpeed)
    }


    //region setFanSpeed
    fun setFanSpeed(view: View) {
        fanSpeed = when (view.id) {
            R.id.btnLow -> FanSpeed.LOW
            R.id.btnMedium -> FanSpeed.MEDIUM
            R.id.btnHigh -> FanSpeed.HIGH
            R.id.btnAuto -> FanSpeed.AUTO
            else -> ""
        }
        if (fanSpeed.isNotEmpty()) {
            val message = "{\"id\":\"${thermostat.serviceId}\",\"command\":\"$fanSpeed\"}"
            activity.publishMessage(topic, message)
        }


//        thermostat!!.fanLevel = fanSpeed
    }

    //endregion

    fun plusThermostatDegree(view: View) {
        thermostatProgress += 5
        if (thermostatProgress > 350) thermostatProgress = 350

        val value = thermostatProgress
        val message = "{\"id\":\"${thermostat.serviceId}\",\"command\":\"settemp\",\"value\":\"$value\"}"
        activity.publishMessage(topic, message)

    }

    fun minusThermostatDegree(view: View) {
        thermostatProgress -= 5
        if (thermostatProgress < 150) thermostatProgress = 150

        val value = thermostatProgress
        val message = "{\"id\":\"${thermostat.serviceId}\",\"command\":\"settemp\",\"value\":\"$value\"}"
        activity.publishMessage(topic, message)

    }

    fun changeVisibility(view: View) {
        showThermostatDialog(thermostat)
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
        return "{\"id\":\"${thermostat.serviceId}\",\"command\":\"$command\"}"
    }

    private fun showThermostatDialog(device: BuildingService) {
        val topic = "${activity.sessionManager.user.projectId}/Thermo/In"

        val builder = AlertDialog.Builder(activity)
        val viewGroup: ViewGroup = activity.findViewById(android.R.id.content)
        val dialogView: View = LayoutInflater.from(activity)
            .inflate(R.layout.dialog_thermostat, viewGroup, false)
        builder.setView(dialogView)
        val alertDialog = builder.create()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()

        val tvCurrent = dialogView.findViewById<TextView>(R.id.tvCurrent)
        val tvDegree = dialogView.findViewById<TextView>(R.id.tvDegree)
        val arcSeekBar = dialogView.findViewById<ArcSeekBar>(R.id.arcSeekBar)
//        val lottieAnimationView = dialogView.findViewById<LottieAnimationView>(R.id.lottieAnimationView)
        val switchOnOff = dialogView.findViewById<ToggleButton>(R.id.switchOnOff)
        switchOnOff.isChecked = device.off == 1
        switchOnOff.text = if (device.off == 1) "ON" else "OFF"
        switchOnOff.setOnCheckedChangeListener { _, isChecked ->
            switchOnOff.text = if (isChecked) "ON" else "OFF"

            var message = "{\"id\":\"${device.serviceId}\",\"command\":\"off\"}"
            if (isChecked) {
                message = "{\"id\":\"${device.serviceId}\",\"command\":\"on\"}"
            }

            activity.publishMessage(topic, message)

        }
//        lottieAnimationView.speed = device.value!!.toFloat() / 10

        arcSeekBar.progress = device.toValue

        val degree: Double = device.toValue.toDouble() / 10
        tvDegree.text = "$degree˚c"

        val currentDegree: Double = device.value!!.toDouble() / 10
        tvCurrent.text = "Current $currentDegree"
//        if (device.level.equals("high")) {
//            lottieAnimationView.speed = 8f
//        } else if (device.level.equals("medium")) {
//            lottieAnimationView.speed = 5f
//        } else {
//            lottieAnimationView.speed = 3f
//        }

        var thermostatProgress = arcSeekBar.progress

        arcSeekBar.setOnProgressChangeListener(object : OnProgressChangeListener {
            override fun onProgressChanged(
                seekBar: ArcSeekBar,
                progress: Int,
                isUser: Boolean
            ) {
                val degree: Double = (arcSeekBar.progress).toDouble() / 10
                tvDegree.text = "$degree ˚c"
                thermostatProgress = arcSeekBar.progress

            }

            override fun onStartTrackingTouch(seekBar: ArcSeekBar) {}
            override fun onStopTrackingTouch(seekBar: ArcSeekBar) {
                thermostatProgress = arcSeekBar.progress


                val value = if (thermostatProgress % 5 == 0) {
                    thermostatProgress
                } else {
                    thermostatProgress - (thermostatProgress % 5)
                }

                thermostatProgress = value
                val message = "{\"id\":\"${device.serviceId}\",\"command\":\"settemp\",\"value\":\"$value\"}"

                activity.publishMessage(topic, message)

                val finalValue: Double = (value).toDouble()
                tvDegree.text = "${finalValue / 10} ˚c"
            }
        })


        dialogView.findViewById<View>(R.id.btnClose)
            .setOnClickListener {
                alertDialog.dismiss()
            }

        dialogView.findViewById<View>(R.id.btnHigh)
            .setOnClickListener {
                val message = "{\"id\":\"${device.serviceId}\",\"command\":\"high\"}"
                activity.publishMessage(topic, message)
//                lottieAnimationView.speed = 8f
//                log("High ")
            }

        dialogView.findViewById<View>(R.id.btnMedium)
            .setOnClickListener {
                val message = "{\"id\":\"${device.serviceId}\",\"command\":\"medium\"}"
                activity.publishMessage(topic, message)

//                lottieAnimationView.speed = 5f
//
//                log("High ")
            }

        dialogView.findViewById<View>(R.id.btnLow)
            .setOnClickListener {
                val message = "{\"id\":\"${device.serviceId}\",\"command\":\"low\"}"
                activity.publishMessage(topic, message)

//                lottieAnimationView.speed = 3f
//                log("High ")
            }

        dialogView.findViewById<View>(R.id.btnAuto)
            .setOnClickListener {
                val message = "{\"id\":\"${device.serviceId}\",\"command\":\"auto\"}"
                activity.publishMessage(topic, message)

//                log("Auto ")
            }
        dialogView.findViewById<View>(R.id.btnPlus)
            .setOnClickListener {
                thermostatProgress += 5
                if (thermostatProgress > 350) thermostatProgress = 350

                arcSeekBar.progress = thermostatProgress
                val value = thermostatProgress
                val message = "{\"id\":\"${device.serviceId}\",\"command\":\"settemp\",\"value\":\"$value\"}"
                activity.publishMessage(topic, message)

                val finalValue: Double = (value).toDouble()
            }

        dialogView.findViewById<View>(R.id.btnMinus)
            .setOnClickListener {
                thermostatProgress -= 5
                if (thermostatProgress < 150) thermostatProgress = 150

                arcSeekBar.progress = thermostatProgress
                val value = thermostatProgress

                val message = "{\"id\":\"${device.serviceId}\",\"command\":\"settemp\",\"value\":\"$value\"}"
                activity.publishMessage(topic, message)

                val finalValue: Double = (value).toDouble()
            }


    }

}