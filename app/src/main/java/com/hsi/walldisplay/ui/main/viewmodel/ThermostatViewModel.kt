package com.hsi.walldisplay.ui.main.viewmodel

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.BindingAdapter
import com.hsi.walldisplay.BR
import com.hsi.walldisplay.R
import com.hsi.walldisplay.databinding.DialogThermostatBinding
import com.hsi.walldisplay.helper.Constants
import com.hsi.walldisplay.model.BuildingService
import com.hsi.walldisplay.model.DeviceType
import com.hsi.walldisplay.model.FanSpeed
import com.hsi.walldisplay.ui.main.MainActivity
import com.hsi.walldisplay.view.ArcSeekBar
import com.hsi.walldisplay.view.ArcSeekBar.OnProgressChangeListener
import kotlin.properties.Delegates


class ThermostatViewModel(
    val activity: MainActivity,
    thermostat: BuildingService
) : BaseObservable() {

//region Variable

    companion object {
        private const val TAG = "ThermostatViewModel"

        @JvmStatic
        @BindingAdapter("setImageViewTint")
        fun setImageViewTint(imageView: ImageView, isSelected: Boolean) {
            Log.d(TAG, "setImageViewTint: $isSelected")

            imageView.setColorFilter(
                ContextCompat.getColor(imageView.context, if (isSelected) R.color.background else R.color.background),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
            imageView.backgroundTintList =
                ContextCompat.getColorStateList(imageView.context, if (isSelected) R.color.yellow_300 else R.color.text_color)

        }

        @JvmStatic
        @BindingAdapter("setThermostatValue")
        fun setThermostatValue(textView: TextView, value: Int) {

            val thermostatValue = if (value % 5 == 0) {
                value
            } else {
                value - (value % 5)
            }

            val finalValue: Double = thermostatValue.toDouble()
            textView.text = "${finalValue / 10} ˚c"

        }

        @JvmStatic
        @BindingAdapter("setFanSpeedImage")
        fun setFanSpeedImage(imageView: ImageView, level: String) {
            when (level) {
                FanSpeed.LOW -> {
                    imageView.setImageResource(R.drawable.ic_fan_low)
                }

                FanSpeed.MEDIUM -> {
                    imageView.setImageResource(R.drawable.ic_fan_meduim)
                }

                FanSpeed.HIGH -> {
                    imageView.setImageResource(R.drawable.ic_fan_high)
                }

                else -> {
                    imageView.setImageResource(R.drawable.ic_fan_auto)
                }
            }

        }
    }

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
        thermostat.level!!
    ) { _, _, _ ->
        notifyPropertyChanged(BR.fanSpeed)
    }

//endregion

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


        thermostat.level = fanSpeed

        activity.dataBaseDao.updateBuildingService(thermostat)
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
        val dialogThermostat: DialogThermostatBinding by lazy { DialogThermostatBinding.inflate(activity.layoutInflater) }
        builder.setView(dialogThermostat.root)
        val alertDialog = builder.create()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()

        activity.setFontAndFontSize(dialogThermostat.root)

        dialogThermostat.viewModel = this
        activity.logD("fanSpeed: $fanSpeed level:${thermostat.level}")
        val tvCurrent = dialogThermostat.tvCurrent
        val tvDegree = dialogThermostat.tvDegree
        val arcSeekBar = dialogThermostat.arcSeekBar
//        val lottieAnimationView = dialogView.findViewById<LottieAnimationView>(R.id.lottieAnimationView)
        val switchOnOff = dialogThermostat.switchOnOff
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
//        when (device.level) {
//            FanSpeed.HIGH -> {
//                selectedFanSpeed(thermostatBinding.btnHigh)
//            }
//
//            FanSpeed.MEDIUM -> {
//                selectedFanSpeed(thermostatBinding.btnMedium)
//            }
//
//            FanSpeed.LOW -> {
//                selectedFanSpeed(thermostatBinding.btnLow)
//            }
//
//            FanSpeed.AUTO -> {
//                selectedFanSpeed(thermostatBinding.btnAuto)
//            }
//        }

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
                thermostat.toValue = value
                activity.dataBaseDao.updateBuildingService(thermostat)
                activity.viewModel.thermostatAdapter.setItems(
                    activity.dataBaseDao.getBuildingDevices(
                        activity.viewModel.building.id,
                        DeviceType.THERMOSTAT
                    ) as ArrayList
                )
            }
        })


        dialogThermostat.btnClose.setOnClickListener {
            alertDialog.dismiss()
        }

        dialogThermostat.btnPlus.setOnClickListener {
            thermostatProgress += 5
            if (thermostatProgress > 350) thermostatProgress = 350

            arcSeekBar.progress = thermostatProgress
            val value = thermostatProgress
            val message = "{\"id\":\"${device.serviceId}\",\"command\":\"settemp\",\"value\":\"$value\"}"
            activity.publishMessage(topic, message)

            val finalValue: Double = (value).toDouble()
        }

        dialogThermostat.btnMinus.setOnClickListener {
            thermostatProgress -= 5
            if (thermostatProgress < 150) thermostatProgress = 150

            arcSeekBar.progress = thermostatProgress
            val value = thermostatProgress

            val message = "{\"id\":\"${device.serviceId}\",\"command\":\"settemp\",\"value\":\"$value\"}"
            activity.publishMessage(topic, message)

            val finalValue: Double = (value).toDouble()
        }

    }

    private fun selectedFanSpeed(imageView: ImageView) {
        imageView.setColorFilter(
            ContextCompat.getColor(activity, R.color.background),
            android.graphics.PorterDuff.Mode.SRC_IN
        )
        imageView.backgroundTintList = ContextCompat.getColorStateList(activity, R.color.yellow_300)
    }
}