package com.hsi.walldisplay.mqtt

import org.eclipse.paho.client.mqttv3.MqttMessage

interface MqttInterFace {

   fun onConnectComplete(
      reconnect: Boolean,
      serverURI: String?
   )

   fun onConnectionLost(cause: Throwable?)

   fun onMessageArrived(
      topic: String?,
      message: MqttMessage?
   )
}