package com.hsi.walldisplay.ui.main

import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.res.ResourcesCompat
import com.hsi.walldisplay.R
import com.hsi.walldisplay.application.BaseActivity
import com.hsi.walldisplay.databinding.ActivityMainBinding
import com.hsi.walldisplay.helper.Constants
import com.hsi.walldisplay.helper.SessionManager
import com.hsi.walldisplay.model.CityItem
import com.hsi.walldisplay.model.FontItem
import com.hsi.walldisplay.mqtt.MqttHelper
import com.hsi.walldisplay.mqtt.MqttInterFace
import com.hsi.walldisplay.ui.main.viewmodel.MainViewModel
import org.eclipse.paho.client.mqttv3.MqttMessage


class MainActivity : BaseActivity(), MqttInterFace {

    //region Variables

    val TAG = "MainActivity"
    val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    val viewModel: MainViewModel by lazy { MainViewModel(this) }
    private val mqttHelper by lazy { MqttHelper(this, this) }
    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.viewModel = viewModel
        calligrapher.setFont(this, "fonts/${SessionManager.font}", true)
        logD("user.image: ${sessionManager.user.image}")
        logD("buildingItems ${dataBaseDao.buildingItems}")
        Handler(Looper.getMainLooper()).postDelayed({
            if (!mqttHelper.isConnected()) mqttHelper.connect()

        }, 500)

        settingLayoutInit()

        mainLayoutLightInit()

        setTypeFaces()

    }

    private fun mainLayoutLightInit() {
        val topic = if (viewModel.lightAdapter.getItems().size > 0) {
            "${activity.sessionManager.user.projectId}/${viewModel.lightAdapter.getItems()[0].masterId}/${Constants.DALI_IN}"
        } else {
            null
        }
        binding.mainLayoutLights.tbLights.setOnCheckedChangeListener { buttonView, isChecked ->
            buttonView.setTextColor(resources.getColor(if (isChecked) com.hsi.walldisplay.R.color.yellow else com.hsi.walldisplay.R.color.text_color_invert))
            buttonView.setCompoundDrawableTintList(ColorStateList.valueOf(resources.getColor(if (isChecked) com.hsi.walldisplay.R.color.yellow else com.hsi.walldisplay.R.color.text_color_invert)))

            if (topic != null) {
                val message = "{\"DAPC\":\"${if (isChecked) "254" else "0"}\",\"BROADCAST\":\"ALL \"}"
                publishMessage(topic, message)
            }
        }

        binding.mainLayoutLights.seekBarLightness.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.mainLayoutLights.tvLightIntensity.text = "$progress%"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                /*  100     255
                    50      x  */

                run {
                    var dim = seekBar!!.progress
                    if (dim >= 255)
                        dim = 254
                    val waf = ((dim * 254) / 100)
                    viewModel.sendLightsCommand(waf)

                }
            }

        })


    }


    private fun settingLayoutInit() {

        binding.mainLayoutSettings.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbAmsterdam -> SessionManager.font = "amsterdam.ttf"
                R.id.rbRobotoBold -> SessionManager.font = "roboto_bold.ttf"
                R.id.rbRobotoMedium -> SessionManager.font = "roboto_medium.ttf"
                R.id.rbAnekBold -> SessionManager.font = "anek_bold.ttf"
                R.id.rbAnekMedium -> SessionManager.font = "anek_medium.ttf"
                R.id.rbAnekThin -> SessionManager.font = "anek_thin.ttf"
                R.id.rbNotoBold -> SessionManager.font = "noto_bold.ttf"
                R.id.rbNotoMedium -> SessionManager.font = "noto_medium.ttf"
                R.id.rbNotoThin -> SessionManager.font = "noto_thin.ttf"
                R.id.rbSairaBold -> SessionManager.font = "saira_bold.ttf"
                R.id.rbSairaMedium -> SessionManager.font = "saira_medium.ttf"
                R.id.rbSairaThin -> SessionManager.font = "saira_thin.ttf"
                R.id.rbSansBold -> SessionManager.font = "sans_bold.ttf"
                R.id.rbSansThin -> SessionManager.font = "sans_thin.ttf"
                else -> SessionManager.font = "roboto_thin.ttf"

            }
            setTypeFaces()
        }

        //smaller
        binding.mainLayoutSettings.btnZoomIn.setOnClickListener {
            if (SessionManager.fontSize < 28f) {
                SessionManager.fontSize += 4f

                setTypeFaces()
                logD("fontSize btnZoomIn: ${SessionManager.fontSize}")
            }
        }

        //bigger
        binding.mainLayoutSettings.btnZoomOut.setOnClickListener {
            if (SessionManager.fontSize > 16f) {
                SessionManager.fontSize -= 4f

                setTypeFaces()
                logD("fontSize btnZoomOut: ${SessionManager.fontSize}")
            }
        }

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
            binding.mainLayoutSettings.switchCompatDayNight.isChecked = true

        binding.mainLayoutSettings.switchCompatDayNight.setOnCheckedChangeListener { _, isChecked ->
            updateUI(isChecked)
        }
    }

    private fun updateUI(isChecked: Boolean) {
        sessionManager.isNightModeOn = isChecked
        setTypeFaces()
    }

    fun setTypeFaces() {
        setFontAndFontSize()
        setFont(binding.mainLayoutSettings.rbAmsterdam, com.hsi.walldisplay.R.font.amsterdam)
        setFont(binding.mainLayoutSettings.rbRobotoBold, com.hsi.walldisplay.R.font.roboto_bold)
        setFont(binding.mainLayoutSettings.rbRobotoMedium, com.hsi.walldisplay.R.font.roboto_medium)
        setFont(binding.mainLayoutSettings.rbRobotoThin, com.hsi.walldisplay.R.font.roboto_thin)
        setFont(binding.mainLayoutSettings.rbAnekBold, com.hsi.walldisplay.R.font.anek_bold)
        setFont(binding.mainLayoutSettings.rbAnekMedium, com.hsi.walldisplay.R.font.anek_medium)
        setFont(binding.mainLayoutSettings.rbAnekThin, com.hsi.walldisplay.R.font.anek_thin)
        setFont(binding.mainLayoutSettings.rbNotoBold, com.hsi.walldisplay.R.font.noto_bold)
        setFont(binding.mainLayoutSettings.rbNotoMedium, com.hsi.walldisplay.R.font.noto_medium)
        setFont(binding.mainLayoutSettings.rbNotoThin, com.hsi.walldisplay.R.font.noto_thin)
        setFont(binding.mainLayoutSettings.rbSairaBold, com.hsi.walldisplay.R.font.saira_bold)
        setFont(binding.mainLayoutSettings.rbSairaMedium, com.hsi.walldisplay.R.font.saira_medium)
        setFont(binding.mainLayoutSettings.rbSairaThin, com.hsi.walldisplay.R.font.saira_thin)
        setFont(binding.mainLayoutSettings.rbSansBold, com.hsi.walldisplay.R.font.sans_bold)
        setFont(binding.mainLayoutSettings.rbSansThin, com.hsi.walldisplay.R.font.sans_thin)
        setFontSize(binding.mainLayoutMain.tvThermostatDegree, 25f)

        selectedFont()

    }

    private fun selectedFont() {
        when (SessionManager.font) {
            "amsterdam.ttf" -> binding.mainLayoutSettings.rbAmsterdam.isChecked = true
            "roboto_bold.ttf" -> binding.mainLayoutSettings.rbRobotoBold.isChecked = true
            "roboto_medium.ttf" -> binding.mainLayoutSettings.rbRobotoMedium.isChecked = true
            "roboto_thin.ttf" -> binding.mainLayoutSettings.rbRobotoThin.isChecked = true
            "anek_bold.ttf" -> binding.mainLayoutSettings.rbAnekBold.isChecked = true
            "anek_medium.ttf" -> binding.mainLayoutSettings.rbAnekMedium.isChecked = true
            "anek_thin.ttf" -> binding.mainLayoutSettings.rbAnekThin.isChecked = true
            "noto_bold.ttf" -> binding.mainLayoutSettings.rbNotoBold.isChecked = true
            "noto_medium.ttf" -> binding.mainLayoutSettings.rbNotoMedium.isChecked = true
            "noto_thin.ttf" -> binding.mainLayoutSettings.rbNotoThin.isChecked = true
            "saira_bold.ttf" -> binding.mainLayoutSettings.rbSairaBold.isChecked = true
            "saira_medium.ttf" -> binding.mainLayoutSettings.rbSairaMedium.isChecked = true
            "saira_thin.ttf" -> binding.mainLayoutSettings.rbSairaThin.isChecked = true
            "sans_bold.ttf" -> binding.mainLayoutSettings.rbSansBold.isChecked = true
            "sans_thin.ttf" -> binding.mainLayoutSettings.rbSansThin.isChecked = true
        }
    }

    fun getFontList(): ArrayList<FontItem> {

        return arrayListOf(
            FontItem("Font: ${getDefaultFont().name}", SessionManager.font),
            FontItem("Amsterdam", "amsterdam.ttf"),
            FontItem("Roboto Bold", "roboto_bold.ttf"),
            FontItem("Roboto Medium", "roboto_medium.ttf"),
            FontItem("Roboto Thin", "roboto_thin.ttf"),
            FontItem("Anek Bold", "anek_bold.ttf"),
            FontItem("Anek Medium", "anek_medium.ttf"),
            FontItem("Anek Thin", "anek_thin.ttf"),
            FontItem("Noto Bold", "noto_bold.ttf"),
            FontItem("Noto Medium", "noto_medium.ttf"),
            FontItem("Noto Thin", "noto_thin.ttf"),
            FontItem("Saira Bold", "saira_bold.ttf"),
            FontItem("Saira Medium", "saira_medium.ttf"),
            FontItem("Saira Thin", "saira_thin.ttf"),
            FontItem("Sans Bold", "sans_bold.ttf"),
            FontItem("Sans Thin", "sans_thin.ttf"),
        )
    }

    fun getCityList(): ArrayList<CityItem> {

        return arrayListOf(
            CityItem("${SessionManager.city.split(",")[0]}", "${SessionManager.city.split(",")[1]}"),
            CityItem("Tehran", "Iran"),
            CityItem("DUBAI", "UAE"),
            CityItem("SHARJAH", "UAE"),
            CityItem("ABUDHABI", "UAE"),
            CityItem("AJMAN", "UAE"),
            CityItem("FUJAIRAH", "UAE"),
            CityItem("RAS AL KHAIMAH", "UAE"),
            CityItem("UMM AL QUWAIN", "UAE"),
        )
    }

    private fun getDefaultFont(): FontItem {

        return when (SessionManager.font) {
            "amsterdam.ttf" -> FontItem("Amsterdam", "amsterdam.ttf")
            "roboto_bold.ttf" -> FontItem("Roboto Bold", "roboto_bold.ttf")
            "roboto_medium.ttf" -> FontItem("Roboto Medium", "roboto_medium.ttf")
            "roboto_thin.ttf" -> FontItem("Roboto Thin", "roboto_thin.ttf")
            "anek_bold.ttf" -> FontItem("Anek Bold", "anek_bold.ttf")
            "anek_medium.ttf" -> FontItem("Anek Medium", "anek_medium.ttf")
            "anek_thin.ttf" -> FontItem("Anek Thin", "anek_thin.ttf")
            "noto_bold.ttf" -> FontItem("Noto Bold", "noto_bold.ttf")
            "noto_medium.ttf" -> FontItem("Noto Medium", "noto_medium.ttf")
            "noto_thin.ttf" -> FontItem("Noto Thin", "noto_thin.ttf")
            "saira_bold.ttf" -> FontItem("Saira Bold", "saira_bold.ttf")
            "saira_medium.ttf" -> FontItem("Saira Medium", "saira_medium.ttf")
            "saira_thin.ttf" -> FontItem("Saira Thin", "saira_thin.ttf")
            "sans_bold.ttf" -> FontItem("Sans Bold", "sans_bold.ttf")
            "sans_thin.ttf" -> FontItem("Sans Thin", "sans_thin.ttf")
            else -> FontItem("Roboto Thin", "roboto_thin.ttf")
        }

    }

    private fun setFont(textView: TextView, fontId: Int) {
        val typeface = ResourcesCompat.getFont(activity, fontId)
        textView.typeface = typeface
    }

    private fun setFontSize(textView: TextView, fontSize: Float) {
        textView.textSize = fontSize
    }

    private fun setResultFont(fontId: Int) {
        val typeface = ResourcesCompat.getFont(activity, fontId)
        binding.mainLayoutSettings.tvResult.typeface = typeface
    }

    ////region MQTT

    fun publishMessage(
        topic: String,
        message: String
    ) {

        sessionManager.sendMessage = message
        sessionManager.topic = topic
        if (mqttHelper.isConnected()) mqttHelper.publishMessage(topic, message)
        else logE("Not Connected")
    }

    override fun onConnectComplete(reconnect: Boolean, serverURI: String?) {
    }

    override fun onConnectionLost(cause: Throwable?) {
    }

    override fun onMessageArrived(topic: String?, message: MqttMessage?) {
    }

    //endregion


    override fun onBackPressed() {

    }
}