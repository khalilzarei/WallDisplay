package com.hsi.walldisplay.mqtt

import android.util.Log
import android.widget.Toast
import com.hsi.walldisplay.ui.main.MainActivity
import com.hsi.walldisplay.helper.Constants.MQTT_SERVER_URI
import info.mqtt.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

class MqttHelper(
    private val activity: MainActivity,
    private val mqttInterface: MqttInterFace
) {

    private  val TAG = MqttHelper::class.java.simpleName

    private var mqttAndroidClient: MqttAndroidClient =
        MqttAndroidClient(activity, MQTT_SERVER_URI, activity.sessionManager.mqttClientId!!)

    init {


        mqttAndroidClient.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(
                reconnect: Boolean,
                serverURI: String?
            ) {
                Log.d(TAG, "Connected to: $serverURI")
                mqttInterface.onConnectComplete(reconnect, serverURI)
            }

            override fun connectionLost(cause: Throwable?) {
                Log.d(TAG, "Connection lost: ${cause?.message}")
                mqttInterface.onConnectionLost(cause)
            }

            override fun messageArrived(
                topic: String?,
                message: MqttMessage?
            ) {
                Log.d(TAG, "Message arrived from topic $topic: ${message.toString()}")
                mqttInterface.onMessageArrived(topic, message)
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                Log.d(TAG, "Delivery complete")
            }
        })

        connect()
    }

    fun connect() {
        val options = MqttConnectOptions()
        options.isAutomaticReconnect = true
        options.isCleanSession = false

        try {
            mqttAndroidClient.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Connected successfully")
                    subscribe()
                }

                override fun onFailure(
                    asyncActionToken: IMqttToken?,
                    exception: Throwable?
                ) {
                    Log.d(TAG, "Failed to connect: ${exception?.message}")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    private fun subscribe() {
        if (activity.dataBaseDao.topicItems.isNotEmpty()) {
            for (item in activity.dataBaseDao.topicItems) {
//            activity.logI("Topic => ${item.topic}")
                if (!item.topic!!.contains("null")) subscribeToTopic(item.topic!!)
            }
        }
//        subscribe("${user.projectId}/${Constants.CURTAIN_IN}")
    }

    private fun subscribeToTopic(topic: String) {
        try {
            mqttAndroidClient.subscribe(topic, 0, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Subscribed to topic: $topic")
                }

                override fun onFailure(
                    asyncActionToken: IMqttToken?,
                    exception: Throwable?
                ) {
                    Log.d(TAG, "Failed to subscribe: ${exception?.message}")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun isConnected(): Boolean = mqttAndroidClient.isConnected

    fun publishMessage(
        topic: String,
        payload: String
    ) {
        if (isConnected()) try {
            val message = MqttMessage()
            message.payload = payload.toByteArray()
            mqttAndroidClient.publish(topic, message)
            Log.d(TAG, "Message published to topic $topic: $payload")
        } catch (e: MqttException) {
            e.printStackTrace()
        }
        else {
            Toast.makeText(activity, "Not Connected", Toast.LENGTH_SHORT)
                .show()
        }
    }

}
