package com.hsi.walldisplay.ui.main.viewmodel

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AlertDialog
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.hsi.walldisplay.BR
import com.hsi.walldisplay.databinding.DialogCctBinding
import com.hsi.walldisplay.databinding.DialogChangeNameBinding
import com.hsi.walldisplay.databinding.DialogDimBinding
import com.hsi.walldisplay.databinding.DialogRgbwBinding
import com.hsi.walldisplay.helper.Constants
import com.hsi.walldisplay.helper.SessionManager
import com.hsi.walldisplay.helper.SessionManager.Companion.dim
import com.hsi.walldisplay.model.BuildingService
import com.hsi.walldisplay.model.DeviceType
import com.hsi.walldisplay.ui.main.MainActivity
import com.hsi.walldisplay.view.ArcSeekBar
import kotlin.math.abs
import kotlin.properties.Delegates

class LightViewModel(
    val activity: MainActivity,
    device: BuildingService
) : BaseObservable() {

    //region Variables


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

    //endregion

    //region change Visibility

    fun changeVisibility(view: View) {
//        layoutSeekBarVisibility = !layoutSeekBarVisibility
        when (device.type) {
            DeviceType.CCT_DT8, DeviceType.CCT_DT6 -> showCCTDialog()
            DeviceType.RGB_DT8, DeviceType.RGB_DT6 -> showRGBWDialog()
            DeviceType.DALI_LIGHT -> showDimDialog()
            else -> {}
        }

    }

    //endregion

    //region changeTitle

    fun changeTitle(view: View): Boolean {
        val dialogChangeName: DialogChangeNameBinding by lazy { DialogChangeNameBinding.inflate(activity.layoutInflater) }
        val builder = AlertDialog.Builder(activity)
        builder.setView(dialogChangeName.root)
        val alertDialog = builder.create()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()

        activity.setFontAndFontSize(dialogChangeName.root)

        dialogChangeName.tvTitle.text = "Change title of ${device!!.name}"
        val etBuildingName = dialogChangeName.etBuildingName
        etBuildingName.setText(device.name)

        dialogChangeName.btnClose.setOnClickListener { alertDialog.dismiss() }

        dialogChangeName.btnSave.setOnClickListener {
            val name: String = etBuildingName.text.toString()
            if (name.isEmpty()) {
                activity.toast("Please Enter name")
                return@setOnClickListener
            }
            device.name = name
            activity.dataBaseDao.updateBuildingService(device)

            activity.viewModel.lightAdapter.setItems(
                activity.dataBaseDao.getDaliLightsOfBuilding(device.buildingId) as ArrayList
            )
            alertDialog.dismiss()
        }
        return true
    }

    //endregion

    //region showDimDialog

    private fun showDimDialog(
    ) {
        val d = if (device.value!!.contains(",")) dimToPercent(device.value!!.split(",")[0].toInt())
        else dimToPercent(device.value!!.toInt())

        val builder = AlertDialog.Builder(activity)
        val dialogDim: DialogDimBinding by lazy { DialogDimBinding.inflate(activity.layoutInflater) }
        builder.setView(dialogDim.root)
        val alertDialog = builder.create()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()

        activity.setFontAndFontSize(dialogDim.root)

        dialogDim.tvTitle.text = device.name
        val tvSeekResult = dialogDim.tvSeekResult
//        val seekBar = dialogDimBinding.seekBar
//        seekBar.thumb = null
//        seekBar.progress = d
        tvSeekResult.text = "$d%"
//        tvSeekResult.visibility = View.GONE

        val arcSeekBar = dialogDim.arcSeekBar
        arcSeekBar.progress = d
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


        dialogDim.btn25.setOnClickListener {
            arcSeekBar.progress = 1
            sendDimMessage(device, 1, false)
        }

        dialogDim.btn50.setOnClickListener {
            arcSeekBar.progress = 50
            sendDimMessage(device, 50, false)
        }

        dialogDim.btn75.setOnClickListener {
            arcSeekBar.progress = 75
            sendDimMessage(device, 75, false)
        }

        dialogDim.btn100.setOnClickListener {
            arcSeekBar.progress = 100
            sendDimMessage(device, 100, false)
        }

        dialogDim.btnClose.setOnClickListener { view: View? ->
            SessionManager.queryLongTouchID = -1
            SessionManager.queryLongTouchMaster = -1
            alertDialog.dismiss()
        }

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

    //endregion

    //region showCCTDialog

    private fun showCCTDialog() {
        activity.log("Value: ${device.value} - Type: ${device.type}")
        var valueTemperature = 0
        val valueControl: Int = device.value!!.split(",")[0].toInt()
        if (device.value!!.split(",").size > 1) valueTemperature = device.value!!.split(",")[1].toInt()

        val topic = "${activity.sessionManager.user.projectId}/${device.masterId}/${Constants.DALI_IN}"
        val idLi = if (device.groupId != null) device.groupId
        else {
            device.serviceId
        }

        val addr = if (device.groupId != null) "GROUP_ADDRESS"
        else {
            "SHORT_ADDRESS"
        }

        val builder = AlertDialog.Builder(activity)
        val dialogCct: DialogCctBinding by lazy { DialogCctBinding.inflate(activity.layoutInflater) }
        builder.setView(dialogCct.root)
        val alertDialog = builder.create()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()

        activity.setFontAndFontSize(dialogCct.root)

        dialogCct.tvTitle.text = device.name

        val seekBarTemperature: SeekBar = dialogCct.seekBarTemperature

        seekBarTemperature.max = 254

        dialogCct.tvPercent.text = "${dimToPercent(valueTemperature)}%"
        seekBarTemperature.progress = valueTemperature
        seekBarTemperature.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                p1: Int,
                p2: Boolean
            ) {
                val dim = seekBar!!.progress
                if (dim in 0..254) {
                    dialogCct.tvPercent.text = "${dimToPercent(dim)}%"
                    dialogCct.tvPercent.visibility = View.VISIBLE
                } else
                    dialogCct.tvPercent.visibility = View.GONE
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val dim = seekBar!!.progress
                if (dim in 0..254) run {

                    val message = "{\"DAPC\r\n\":\"$dim\r\n\",\"$addr\r\n\":\"$idLi\r\n\"}"
                    device.value = "$dim,$valueTemperature"
                    activity.dataBaseDao.updateBuildingService(device)
                    activity.publishMessage(topic, message)
                }
            }
        })

        dialogCct.arcSeekBar.progress = valueControl

        dialogCct.arcSeekBar.setOnProgressChangeListener(object : ArcSeekBar.OnProgressChangeListener {
            override fun onProgressChanged(seekBar: ArcSeekBar?, progress: Int, isUser: Boolean) {
//                binding.tvSeekResult.text = "${150 + abs(500 - progress)}"
            }

            override fun onStartTrackingTouch(seekBar: ArcSeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: ArcSeekBar?) {

                if (seekBar!!.progress >= 150 && seekBar.progress <= 500) run {
                    val dim = 150 + abs(500 - seekBar.progress)
                    val message = "{\"SET_TEMPORARY_COLOUR_TEMPERATURE_TC\r\n\":\"$dim\r\n\",\"$addr\r\n\":\"$idLi\r\n\"}"
                    device.value = "$valueControl,$dim"
                    activity.dataBaseDao.updateBuildingService(device)
                    activity.publishMessage(topic, message)
                }
            }

        })


        dialogCct.btnClose.setOnClickListener { view: View? ->
            alertDialog.dismiss()
        }


        dialogCct.btn35.setOnClickListener {
            /*
                500     100
                415     x

                x = 415 * 100 / 500
             */
            val dim = 415
            dialogCct.arcSeekBar.progress = 150 + abs(500 - dim)
            val message = "{\"SET_TEMPORARY_COLOUR_TEMPERATURE_TC\r\n\":\"$dim\r\n\",\"$addr\r\n\":\"$idLi\r\n\"}"
            device.value = "$dim,$valueTemperature"
            activity.dataBaseDao.updateBuildingService(device)
            activity.publishMessage(topic, message)
        }

        dialogCct.btn40.setOnClickListener {
            val dim = 362
            dialogCct.arcSeekBar.progress = 150 + abs(500 - dim)
            val message = "{\"SET_TEMPORARY_COLOUR_TEMPERATURE_TC\r\n\":\"$dim\r\n\",\"$addr\r\n\":\"$idLi\r\n\"}"
            device.value = "$dim,$valueTemperature"
            activity.dataBaseDao.updateBuildingService(device)
            activity.publishMessage(topic, message)
        }

        dialogCct.btn50.setOnClickListener {
            val dim = 256
            dialogCct.arcSeekBar.progress = 150 + abs(500 - dim)
            val message = "{\"SET_TEMPORARY_COLOUR_TEMPERATURE_TC\r\n\":\"$dim\r\n\",\"$addr\r\n\":\"$idLi\r\n\"}"
            device.value = "$dim,$valueTemperature"
            activity.dataBaseDao.updateBuildingService(device)
            activity.publishMessage(topic, message)
        }

        dialogCct.btn60.setOnClickListener {
            val dim = 150
            dialogCct.arcSeekBar.progress = 150 + abs(500 - dim)
            val message = "{\"SET_TEMPORARY_COLOUR_TEMPERATURE_TC\r\n\":\"$dim\r\n\",\"$addr\r\n\":\"$idLi\r\n\"}"
            device.value = "$dim,$valueTemperature"
            activity.dataBaseDao.updateBuildingService(device)
            activity.publishMessage(topic, message)
        }
    }

    //endregion

    //region RGBWDialog

    private fun showRGBWDialog() {

        var white: Int = 0
        var red: Int = 0
        var green: Int = 0
        var blue: Int = 0
        SessionManager.rgbwDeviceA0 = (null)
        SessionManager.rgbwMasterID = (null)

        val builder = AlertDialog.Builder(activity)
        val dialogRgbw: DialogRgbwBinding by lazy { DialogRgbwBinding.inflate(activity.layoutInflater) }
        builder.setView(dialogRgbw.root)
        val alertDialog = builder.create()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()

        activity.setFontAndFontSize(dialogRgbw.root)

        val lightId = if (device.groupId != null) device.groupId!!
        else {
            device.serviceId!!
        }

        val address: String = if (device.groupId == null) "SHORT_ADDRESS"
        else
            "GROUP_ADDRESS"
        dialogRgbw.tvTitle.text = device.name
        val colorPickerView = dialogRgbw.colorPickerView
        val cardViewColor = dialogRgbw.cardViewColor
        val btnSetColor = dialogRgbw.btnSetColor
        val seekBar = dialogRgbw.seekBarWhite
        val ivCancel = dialogRgbw.btnClose
        val seekBarLightness = dialogRgbw.seekBarLightness

        dialogRgbw.tvWhite25.setOnClickListener {
            seekBar.progress = 25
            dim = 25 * 254 / 100
            val waf = dim * 65536
            sendRGBMinMaxMessage(waf)
        }
        dialogRgbw.tvWhite50.setOnClickListener {
            seekBar.progress = 50
            dim = 50 * 254 / 100
            val waf = dim * 65536
            sendRGBMinMaxMessage(waf)

        }
        dialogRgbw.tvWhite75.setOnClickListener {
            seekBar.progress = 75
            dim = 75 * 254 / 100
            val waf = dim * 65536
            sendRGBMinMaxMessage(waf)

        }
        dialogRgbw.tvWhite100.setOnClickListener {
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
                if (p1 in 0..254) {
                    dapc = p1

                    dialogRgbw.tvLightIntensity.text = "$p1%"
                }
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

                        val message = "{\"DAPC\r\n\":\"$waf\r\n\",\"$address\r\n\":\"$lightId\r\n\"}"
                        val topic = "${activity.sessionManager.user.projectId}/${device.masterId}/${Constants.DALI_IN}"
                        activity.publishMessage(topic, message)
                        device.value = percentToDim(dim).toString()
                        activity.dataBaseDao.updateBuildingService(device)
                    }
            }


        })

        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                dialogRgbw.tvWhiteIntensity.text = "$progress%"
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
                                "\"$address\r\n\":\"$lightId\r\n\"}"
                        val topic = "${activity.sessionManager.user.projectId}/${device.masterId}/${Constants.DALI_IN}"
                        activity.publishMessage(topic, message)
                        Thread.sleep(delay.toLong())

                        val num = (red * 65536) + (green * 254) + blue
                        //convert hex to decimal
                        val teMessage =
                            "{\"SET_TEMPORARY_RGB_DIM_LEVEL\r\n\":\"$num\r\n\",\"$address\r\n\":\"$lightId\r\n\"}"
                        activity.publishMessage(topic, teMessage)
                        Thread.sleep(delay.toLong())

                        val wafDim = ((dapc * 254) / 100)
                        val messageDIM = "{\"DAPC\r\n\":\"$wafDim\r\n\",\"$address\r\n\":\"$lightId\r\n\"}"
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

        val address: String = if (device.groupId == null) "SHORT_ADDRESS"
        else
            "GROUP_ADDRESS"

        val message = "{\"DAPC\r\n\":\"$dim\r\n\",\"$address\r\n\":\"$lightId\r\n\"}"
        val topic = "${activity.sessionManager.user.projectId}/${device.masterId}/${Constants.DALI_IN}"
        activity.publishMessage(topic, message)
    }

    private fun sendRGBMinMaxMessage(waf: Int) {

        val lightId: String = if (device.groupId != null) device.groupId!!
        else
            device.serviceId!!

        val address: String = if (device.groupId == null) "SHORT_ADDRESS"
        else
            "GROUP_ADDRESS"


        val message = "{\"SET_TEMPORARY_WAF_DIM_LEVEL\r\n\":\"$waf\r\n\",\"$address\r\n\":\"$lightId\r\n\"}"
        val topic = "${activity.sessionManager.user.projectId}/${device.masterId}/${Constants.DALI_IN}"
        activity.publishMessage(topic, message)
        device.value = percentToDim(dim).toString()
        activity.dataBaseDao.updateBuildingService(device)
    }

    //endregion


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