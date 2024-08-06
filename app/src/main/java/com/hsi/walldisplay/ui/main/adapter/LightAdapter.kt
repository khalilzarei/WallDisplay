package com.hsi.walldisplay.ui.main.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.hsi.walldisplay.R
import com.hsi.walldisplay.databinding.LightItem
import com.hsi.walldisplay.helper.Constants
import com.hsi.walldisplay.helper.SessionManager
import com.hsi.walldisplay.helper.SessionManager.Companion.dim
import com.hsi.walldisplay.model.BuildingService
import com.hsi.walldisplay.model.DeviceType
import com.hsi.walldisplay.ui.main.MainActivity
import com.hsi.walldisplay.ui.main.viewmodel.LightViewModel
import de.hdodenhof.circleimageview.CircleImageView


class LightAdapter(
    val activity: MainActivity,
    devices: ArrayList<BuildingService>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items: ArrayList<BuildingService> = arrayListOf()

    init {
        for (device in devices) {
            if (device.groupId != null)
                items.add(device)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {

        val itemRow = DataBindingUtil.inflate<LightItem>(
            LayoutInflater.from(parent.context), R.layout.item_row_light, parent, false
        )
        return ViewHolder(itemRow)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        val device = items[position]
        val viewHolder = holder as ViewHolder
        val viewModel = LightViewModel(activity, device)
        viewHolder.item.viewModel = viewModel

//        viewHolder.item.layoutSeekBar.visibility = View.GONE

        val topic = "${activity.sessionManager.user.projectId}/${device.masterId}/${Constants.DALI_IN}"

        viewHolder.item.switchOnOff.isChecked = device.value!!.split(",")[0].toInt() > 0
        viewHolder.item.switchOffOn.isChecked = device.value!!.split(",")[0].toInt() > 0


        viewHolder.item.tvTitle.text = when (device.type) {
            DeviceType.RGB_DT6, DeviceType.RGB_DT8 -> "RGB ${device.serviceId}"
            DeviceType.CCT_DT6, DeviceType.CCT_DT8 -> "CCT ${device.serviceId}"
            DeviceType.RELAY -> "RELAY ${device.serviceId}"
            DeviceType.DALI_LIGHT -> if (device.groupId == null) "DALI LIGHT ${device.serviceId}" else "GROUP ${device.serviceId}"
            else -> "${device.type} ${device.serviceId}"
        }
        viewHolder.item.switchOnOff.setOnCheckedChangeListener { view, isChecked ->
            if (view.isPressed) {
                activity.logD("isChecked:$isChecked type ${device.type}")
                when (device.type) {
                    DeviceType.DALI_LIGHT -> {
                        lightClicked(device, topic)
                    }

                    DeviceType.RGB_DT8 -> {
                        dt8Clicked(device, topic)
                    }

                    DeviceType.RGB_DT6 -> {
                        dt6Clicked(device)
                    }

                    DeviceType.CCT_DT6, DeviceType.CCT_DT8 -> {

                        val lightId: String = device.serviceId!!
                        if (device.value != null && device.value!!.isNotEmpty() && device.value!!.split(",").size >= 2) {

                            activity.log("CCT_DT6 => dim:$dim value:${device.value} Default:${device.defaultValue}")
                            val dimTemp: Int =
                                if (device.value!!.split(",")[0] == "0") if (device.defaultValue != -1) device.defaultValue else 254 else 0
                            val dim = device.value!!.split(",")[1]
                            val idLi = if (device.groupId != null) device.groupId
                            else lightId
                            val addr = if (device.groupId != null) "GROUP_ADDRESS"
                            else "SHORT_ADDRESS"
                            val onOffMSG = "{\"DAPC\r\n\":\"$dimTemp\r\n\",\"$addr\r\n\":\"$idLi\r\n\"}"
                            device.value = "$dimTemp,$dim"
                            activity.log("CCT_DT6 => dim:$dim Temp:$dimTemp value:${device.value} Default:${device.defaultValue}")

                            activity.dataBaseDao.updateBuildingService(device)
                            activity.publishMessage(topic, onOffMSG)
                        }
                    }

                    else -> {}
                }
            }
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

    private fun dt6Clicked(device: BuildingService) {

        val lightId: String = device.serviceId!!

        val dimTemp: Int = if (device.value!!.split(",")[0] == "0") 200 else 0

        val delay: Int = SessionManager.getDelay
        device.value = dimTemp.toString()
        activity.dataBaseDao.updateBuildingService(device)
        Thread {
            try {
                publishRGBWMessage(device, lightId.toInt(), dimTemp)
                Thread.sleep(delay.toLong())
                publishRGBWMessage(device, lightId.toInt() + 1, dimTemp)
                Thread.sleep(delay.toLong())
                publishRGBWMessage(device, lightId.toInt() + 2, dimTemp)
                Thread.sleep(delay.toLong())
                publishRGBWMessage(device, lightId.toInt() + 3, dimTemp)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }.start()
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

    private fun dt8Clicked(device: BuildingService, topic: String) {

        var onOffMSG = ""
        val lightId: String = device.serviceId!!

        if (device.value!!.split(",").size >= 2) {
            val dimTemp: Int = if (device.value!!.split(",")[0] != "0") 0 else 254
            val temp = device.value!!.split(",")[1]
            device.value = "$dimTemp,$temp"
            activity.log("RGB_DT8 => device.value: ${device.value}")
            activity.dataBaseDao.updateBuildingService(device)

            onOffMSG += "{\"DAPC\r\n\":\"$dimTemp"
            val addr = if (device.groupId != null) "GROUP_ADDRESS"
            else {
                "SHORT_ADDRESS"
            }

            onOffMSG += "\r\n\",\"$addr\r\n\":\"$lightId\r\n\"}"

            activity.publishMessage(topic, onOffMSG)
        }
    }

    private fun lightClicked(device: BuildingService, topic: String) {
        val dimOnOff = if (device.value.equals("0")) 254 else 0
        var onOffMSG = ""
        val lightId: String = device.serviceId!!
        device.value = dimOnOff.toString()

        if (device.groupId == null) {
            if (dimOnOff > 0) {
                if (device.defaultValue == -1) onOffMSG += "{\"CMD\r\n\":\"ON_AND_STEP_UP"
                else {
                    onOffMSG += "{\"DAPC\r\n\":\"${device.defaultValue}"
                    device.value = device.defaultValue.toString()
                }

            } else onOffMSG += "{\"CMD\r\n\":\"OFF"
            onOffMSG += "\r\n\",\"SHORT_ADDRESS\r\n\":\"$lightId\r\n\"}"

            //"{\"CMD\r\n\":\"OFF\r\n\",\"SHORT_ADDRESS\r\n\":\"2\r\n\"}"
        } else {
            onOffMSG += if (dimOnOff > 0) {
                if (device.defaultValue == -1) "{\"DAPC\r\n\":\"254"
                else "{\"DAPC\r\n\":\"${device.defaultValue}"

            } else "{\"DAPC\r\n\":\"0"

            onOffMSG += "\r\n\",\"GROUP_ADDRESS\r\n\":\"${device.groupId}\r\n\"}"
        }
        activity.dataBaseDao.updateBuildingService(device)

        activity.publishMessage(topic, onOffMSG)

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

    class ViewHolder internal constructor(var item: LightItem) : RecyclerView.ViewHolder(item.root)


}