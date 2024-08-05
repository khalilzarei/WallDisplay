package com.hsi.walldisplay.ui.main

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.hsi.walldisplay.application.BaseActivity
import com.hsi.walldisplay.databinding.ActivityMainBinding
import com.hsi.walldisplay.mqtt.MqttHelper
import com.hsi.walldisplay.mqtt.MqttInterFace
import com.hsi.walldisplay.ui.main.viewmodel.MainViewModel
import org.eclipse.paho.client.mqttv3.MqttMessage

class MainActivity : BaseActivity(), MqttInterFace {

    //region Variables

    val TAG = "MainActivity"
    val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val viewModel: MainViewModel by lazy { MainViewModel(this) }
    private val mqttHelper by lazy { MqttHelper(this, this) }
    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.viewModel = viewModel
        logD("user.image: ${sessionManager.user.image}")
        logD("buildingItems ${dataBaseDao.buildingItems}")
        Handler(Looper.getMainLooper()).postDelayed({
            if (!mqttHelper.isConnected()) mqttHelper.connect()

        }, 500)
//

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
}