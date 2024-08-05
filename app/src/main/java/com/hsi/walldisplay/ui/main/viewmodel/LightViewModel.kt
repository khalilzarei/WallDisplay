package com.hsi.walldisplay.ui.main.viewmodel

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.DataBindingUtil
import androidx.transition.Visibility
import com.flask.colorpicker.ColorPickerView
import com.hsi.walldisplay.model.Curtain
import com.hsi.walldisplay.BR
import com.hsi.walldisplay.R
import com.hsi.walldisplay.databinding.DialogCctBinding
import com.hsi.walldisplay.helper.Constants
import com.hsi.walldisplay.helper.SessionManager
import com.hsi.walldisplay.helper.SessionManager.Companion.dim
import com.hsi.walldisplay.model.BuildingService
import com.hsi.walldisplay.model.DeviceType
import com.hsi.walldisplay.ui.main.MainActivity
import com.hsi.walldisplay.view.ArcSeekBar
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.properties.Delegates

class LightViewModel(
    val activity: MainActivity,
    device: BuildingService
) : BaseObservable() {

    val topic = "${activity.sessionManager.user.projectId}/${Constants.CURTAIN_IN}"

    @get:Bindable
    var device: BuildingService by Delegates.observable(device) { _, _, _ ->
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
//        layoutSeekBarVisibility = !layoutSeekBarVisibility
        when (device.type) {
            DeviceType.CCT_DT8, DeviceType.CCT_DT6 -> showCCTDialog()
            DeviceType.RGB_DT8, DeviceType.RGB_DT6 -> showRGBWDialog()
            DeviceType.DALI_LIGHT -> showDimDialog()
            else -> {}
        }

    }

    private fun showDimDialog(
    ) {
        val d = if (device.value!!.contains(",")) dimToPercent(device.value!!.split(",")[0].toInt())
        else dimToPercent(device.value!!.toInt())

        activity.log("Dim ${device.value} = $d")
        val builder = AlertDialog.Builder(activity)
        val viewGroup: ViewGroup = activity.findViewById(android.R.id.content)
        val dialogView: View = LayoutInflater.from(activity)
            .inflate(R.layout.dialog_dim, viewGroup, false)
        builder.setView(dialogView)
        val alertDialog = builder.create()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvTitle)
        tvTitle.text = if (device.groupId != null) "GROUP" else "DALI LIGHT"
        val tvSeekResult = dialogView.findViewById<TextView>(R.id.tvSeekResult)
        val seekBar = dialogView.findViewById<SeekBar>(R.id.seekBar)
//        seekBar.thumb = null
        seekBar.progress = d
        tvSeekResult.text = "$d%"
//        tvSeekResult.visibility = View.GONE

        val arcSeekBar = dialogView.findViewById<ArcSeekBar>(R.id.arcSeekBar)
        arcSeekBar.setOnProgressChangeListener(object : ArcSeekBar.OnProgressChangeListener {
            override fun onProgressChanged(seekBar: ArcSeekBar?, progress: Int, isUser: Boolean) {
                tvSeekResult.text = "$progress%"
            }

            override fun onStartTrackingTouch(seekBar: ArcSeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: ArcSeekBar?) {
                val dim: Int = seekBar!!.progress
                sendDimMessage(device, dim, false)
            }

        })


        dialogView.findViewById<View>(R.id.btn25)
            .setOnClickListener {
                seekBar.progress = 1
                sendDimMessage(device, 1, false)
            }

        dialogView.findViewById<View>(R.id.btn50)
            .setOnClickListener {
                seekBar.progress = 50
                sendDimMessage(device, 50, false)
            }

        dialogView.findViewById<View>(R.id.btn75)
            .setOnClickListener {
                seekBar.progress = 75
                sendDimMessage(device, 75, false)
            }

        dialogView.findViewById<View>(R.id.btn100)
            .setOnClickListener {
                seekBar.progress = 100
                sendDimMessage(device, 100, false)
            }

        dialogView.findViewById<View>(R.id.btnClose)
            .setOnClickListener { view: View? ->
                SessionManager.queryLongTouchID = -1
                SessionManager.queryLongTouchMaster = -1
                alertDialog.dismiss()
            }

    }

    private fun showCCTDialog() {
        activity.log("Value: ${device.value} - Type: ${device.type}")
        var valueTemperature = 0
        val valueControl: Int = device.value!!.split(",")[0].toInt()
        if (device.value!!.split(",").size > 1) valueTemperature = device.value!!.split(",")[1].toInt()

//        log("Dim ${device.value} = $d")
        val builder = AlertDialog.Builder(activity)
        val binding: DialogCctBinding = DataBindingUtil.inflate(LayoutInflater.from(activity), R.layout.dialog_cct, null, false)
        builder.setView(binding.root)
        val alertDialog = builder.create()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()


        val seekBarTemperature: SeekBar = binding.seekBarTemperature

        binding.arcSeekBar.progress = percentToDim(valueControl)
        binding.arcSeekBar.setOnProgressChangeListener(object : ArcSeekBar.OnProgressChangeListener {
            override fun onProgressChanged(seekBar: ArcSeekBar?, progress: Int, isUser: Boolean) {
                binding.tvSeekResult.text = "$progress%"
            }

            override fun onStartTrackingTouch(seekBar: ArcSeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: ArcSeekBar?) {
                val dim: Int = seekBar!!.progress
                if (dim in 0..254) run {

                    val dim: Int = seekBar.progress * 254 / 100
                    val idLi = if (device.groupId != null) device.groupId
                    else {
                        device.serviceId
                    }

                    val addr = if (device.groupId != null) "GROUP_ADDRESS"
                    else {
                        "SHORT_ADDRESS"
                    }
                    val message = "{\"DAPC\r\n\":\"$dim\r\n\",\"$addr\r\n\":\"$idLi\r\n\"}"

                    val topic = "${activity.sessionManager.user.projectId}/${device.masterId}/${Constants.DALI_IN}"

                    device.value = "$dim,$valueTemperature"
                    activity.dataBaseDao.updateBuildingService(device)
                    activity.publishMessage(topic, message)
                }
            }

        })

        seekBarTemperature.max = 254
        seekBarTemperature.max = 350
        seekBarTemperature.progress = percentToDim(valueTemperature)


        seekBarTemperature.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                p1: Int,
                p2: Boolean
            ) {
                activity.log("seekBarControl progress:${seekBar?.progress}")
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (seekBar!!.progress >= 0 && seekBar.progress <= 350) run {

                    val dim: Int = seekBar.progress + 150

                    activity.log("seekBarControl DIM $dim")
                    val idLi = if (device.groupId != null) device.groupId
                    else {
                        device.serviceId
                    }

                    val addr = if (device.groupId != null) "GROUP_ADDRESS"
                    else {
                        "SHORT_ADDRESS"
                    }
                    val message = "{\"SET_TEMPORARY_COLOUR_TEMPERATURE_TC\r\n\":\"$dim\r\n\",\"$addr\r\n\":\"$idLi\r\n\"}"

                    val topic = "${activity.sessionManager.user.projectId}/${device.masterId}/${Constants.DALI_IN}"

                    device.value = "$valueControl,$dim"
                    activity.dataBaseDao.updateBuildingService(device)
                    activity.publishMessage(topic, message)
                }
            }
        })

        binding.btnClose.setOnClickListener { view: View? ->
            alertDialog.dismiss()
        }

    }

    private fun showRGBWDialog() {

        var white: Int = 0
        var red: Int = 0
        var green: Int = 0
        var blue: Int = 0
        SessionManager.rgbwDeviceA0 = (null)
        SessionManager.rgbwMasterID = (null)
        val builder = AlertDialog.Builder(activity)
        val viewGroup: ViewGroup = activity.findViewById(android.R.id.content)
        val dialogView: View = LayoutInflater.from(activity)
            .inflate(R.layout.dialog_rgbw, viewGroup, false)

        val lightId: String = device.serviceId!!

        val colorPickerView = dialogView.findViewById<ColorPickerView>(R.id.colorPickerView)
        val cardViewColor = dialogView.findViewById<CardView>(R.id.cardViewColor)
        val btnSetColor = dialogView.findViewById<View>(R.id.btnSetColor)
        val seekBar = dialogView.findViewById<SeekBar>(R.id.seekBarWhite)
        val ivCancel = dialogView.findViewById<View>(R.id.ivCancel)
        val seekBarLightness = dialogView.findViewById<SeekBar>(R.id.seekBarLightness)

        (dialogView.findViewById<View>(R.id.tvWhite25)).setOnClickListener {
            seekBar.progress = 25
            dim = 25 * 254 / 100
            val waf = dim * 65536
            sendRGBMinMaxMessage(waf)
        }
        (dialogView.findViewById<View>(R.id.tvWhite50)).setOnClickListener {
            seekBar.progress = 50
            dim = 50 * 254 / 100
            val waf = dim * 65536
            sendRGBMinMaxMessage(waf)

        }
        (dialogView.findViewById<View>(R.id.tvWhite75)).setOnClickListener {
            seekBar.progress = 75
            dim = 75 * 254 / 100
            val waf = dim * 65536
            sendRGBMinMaxMessage(waf)

        }
        (dialogView.findViewById<View>(R.id.tvWhite100)).setOnClickListener {
            seekBar.progress = 100
            val waf = 254 * 65536
            sendRGBMinMaxMessage(waf)

        }

        val colors = device.value?.split(",")
        var a = 254
        var r = 254
        var g = 254
        var b = 254
        if (device.type == DeviceType.RGB_DT6) {

            if (colors != null && colors.size == 4) {
                a = colors[0].toInt()
                r = colors[1].toInt()
                g = colors[2].toInt()
                b = colors[3].toInt()
            }
        } else {

            if (colors != null && colors.size == 2) {
                a = colors[0].toInt()
                r = colors[1].toInt()
            }
        }

        seekBar.progress = a
        cardViewColor.setCardBackgroundColor(Color.argb(a, r, g, b))

        white = seekBar.progress


        var dapc = 0

        colorPickerView.addOnColorSelectedListener { selectedColor: Int ->
            red = selectedColor shr 16 and 0xFF
            green = selectedColor shr 8 and 0xFF
            blue = selectedColor and 0xFF
            val alpha = selectedColor shr 24 and 0xFF
            val color = Color.argb(alpha, red, green, blue)
            cardViewColor.setCardBackgroundColor(color)
        }

        seekBarLightness.max = 100
        seekBarLightness.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(
                p0: SeekBar?,
                p1: Int,
                p2: Boolean
            ) {

                activity.log(" ${p1.toFloat()} ${(p1.toFloat() / 100)}")
                colorPickerView.setLightness(((p1.toFloat() / 100)))
//                cardViewColor.alpha = (p1 / 100).toFloat()
                if (p1 in 0..254)
                    dapc = p1
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                dapc = seekBar!!.progress
                if (dapc in 0..254)
                    run {
                        /*
                        100     255
                        50      x
                         */
                        if (dapc >= 255)
                            dapc = 254

                        val waf = ((dapc * 254) / 100)

                        val message = "{\"DAPC\r\n\":\"$waf\r\n\",\"SHORT_ADDRESS\r\n\":\"$lightId\r\n\"}"
                        val topic = "${activity.sessionManager.user.projectId}/${device.masterId}/${Constants.DALI_IN}"
                        activity.publishMessage(topic, message)
                        device.value = percentToDim(dim).toString()
                        activity.dataBaseDao.updateBuildingService(device)
                    }
            }


        })

        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

                run {
                    dim = seekBar!!.progress
                    /*
                    100     255
                    50      x
                     */

                    if (dim >= 255)
                        dim = 254
                    val waf = ((dim * 254) / 100) * 65536
                    sendRGBMinMaxMessage(waf)
                }
            }

        })

        builder.setView(dialogView)
        val alertDialog = builder.create()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        ivCancel.setOnClickListener { view: View? -> alertDialog.dismiss() }
        btnSetColor.setOnClickListener { view: View? ->
            SessionManager.rgbwDeviceA0 = device.serviceId.toString()
            SessionManager.rgbwMasterID = device.masterId.toString()
            val delay: Int = SessionManager.getDelay
            dapc = seekBarLightness.progress
            activity.toast("DAPC: $dapc  - ${seekBarLightness.progress} ${device.type}")
            activity.log("DAPC: $dapc  - ${seekBarLightness.progress}")
            if (device.type == DeviceType.RGB_DT6) {
                if (white in 0..254) {
                    if (red >= 255) {
                        red = 254
                    }
                    if (white >= 255) {
                        white = 254
                    }
                    if (green >= 255) {
                        green = 254
                    }
                    if (blue >= 255) {
                        blue = 254
                    }
                    val deviceValue = "$red,$green,$blue,$white"
                    device.value = deviceValue
                    Thread {
                        try {
                            publishRGBWMessage(device, lightId.toInt(), red)
                            Thread.sleep(delay.toLong())
                            publishRGBWMessage(device, lightId.toInt() + 1, green)
                            Thread.sleep(delay.toLong())
                            publishRGBWMessage(device, lightId.toInt() + 2, blue)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                    }.start()

                    SessionManager.clicked = (false)
                }
            } else {

                val deviceValue = "$dim,$white"
                device.value = deviceValue
                Thread {
                    try {
                        if (red >= 255) {
                            red = 254
                        }
                        if (white >= 255) {
                            white = 254
                        }
                        if (green >= 255) {
                            green = 254
                        }
                        if (blue >= 255) {
                            blue = 254
                        }

                        val waf = white * 65536
                        val message = "{\"SET_TEMPORARY_WAF_DIM_LEVEL\r\n\":\"0\r\n\"," +
                                "\"SHORT_ADDRESS\r\n\":\"$lightId\r\n\"}"
                        val topic = "${activity.sessionManager.user.projectId}/${device.masterId}/${Constants.DALI_IN}"
                        activity.publishMessage(topic, message)
                        Thread.sleep(delay.toLong())

                        val num = (red * 65536) + (green * 254) + blue
                        //convert hex to decimal
                        val teMessage =
                            "{\"SET_TEMPORARY_RGB_DIM_LEVEL\r\n\":\"$num\r\n\",\"SHORT_ADDRESS\r\n\":\"$lightId\r\n\"}"
                        activity.publishMessage(topic, teMessage)
                        Thread.sleep(delay.toLong())

                        val wafDim = ((dapc * 254) / 100)
                        val messageDIM = "{\"DAPC\r\n\":\"$wafDim\r\n\",\"SHORT_ADDRESS\r\n\":\"$lightId\r\n\"}"
                        activity.publishMessage(topic, messageDIM)

                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }.start()

                SessionManager.clicked = (false)
            }

            activity.dataBaseDao.updateBuildingService(device)
        }
    }

    private fun publishRGBWMessage(
        device: BuildingService,
        lightId: Int,
        dim: Int
    ) {

        val message = "{\"DAPC\r\n\":\"$dim\r\n\",\"SHORT_ADDRESS\r\n\":\"$lightId\r\n\"}"
        val topic = "${activity.sessionManager.user.projectId}/${device.masterId}/${Constants.DALI_IN}"
        activity.publishMessage(topic, message)
    }

    private fun sendRGBMinMaxMessage(waf: Int) {

        val lightId: String = device.serviceId!!
        val message = "{\"SET_TEMPORARY_WAF_DIM_LEVEL\r\n\":\"$waf\r\n\",\"SHORT_ADDRESS\r\n\":\"$lightId\r\n\"}"
        val topic = "${activity.sessionManager.user.projectId}/${device.masterId}/${Constants.DALI_IN}"
        activity.publishMessage(topic, message)
        device.value = percentToDim(dim).toString()
        activity.dataBaseDao.updateBuildingService(device)
    }

    fun sendDimMessage(
        device: BuildingService,
        di: Int,
        isCct: Boolean
    ) {
        val dim: Int = di * 254 / 100
        run {

            var message =
                if (isCct) "{\"SET_TEMPORARY_COLOUR_TEMPERATURE_TC\r\n\":\"$dim\r\n\"," + "\"SHORT_ADDRESS\r\n\":\"${device.serviceId}\r\n\"}"
                else if (device.groupId == null) "{\"DAPC\r\n\":\"$dim\r\n\",\"SHORT_ADDRESS\r\n\":\"${device.serviceId}\r\n\"}"
                else "{\"DAPC\r\n\":\"$dim\r\n\",\"GROUP_ADDRESS\r\n\":\"${device.groupId}\r\n\"}"

            if (di == 1) {
                message = if (device.groupId == null) {
                    "{\"CMD\r\n\":\"RECALL_MIN_LEVEL\r\n\",\"SHORT_ADDRESS\r\n\":\"${device.serviceId}\r\n\"}"
                } else {
                    "{\"CMD\r\n\":\"RECALL_MIN_LEVEL\r\n\",\"GROUP_ADDRESS\r\n\":\"${device.groupId}\r\n\"}"
                }
            }

            if (di == 100) {
                message = if (device.groupId == null) {
                    "{\"CMD\r\n\":\"RECALL_MAX_LEVEL\r\n\",\"SHORT_ADDRESS\r\n\":\"${device.serviceId}\r\n\"}"
                } else {
                    "{\"CMD\r\n\":\"RECALL_MAX_LEVEL\r\n\",\"GROUP_ADDRESS\r\n\":\"${device.groupId}\r\n\"}"
                }
            }

            val topic = "${activity.sessionManager.user.projectId}/${device.masterId}/${Constants.DALI_IN}"

            device.value = percentToDim(dim).toString()
            activity.dataBaseDao.updateBuildingService(device)
            activity.publishMessage(topic, message)
        }
    }


    private fun percentToDim(percent: Int): Int {
        return percent * 254 / 100
    }

    private fun dimToPercent(dim: Int): Int {
        return dim * 100 / 254
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
        return "{\"id\":\"${device.serviceId}\",\"command\":\"$command\"}"
    }

}