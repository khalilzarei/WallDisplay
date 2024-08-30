package com.hsi.walldisplay.helper

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.view.View
import com.hsi.walldisplay.model.User

class SessionManager @SuppressLint("CommitPrefEdits") constructor(var context: Context) {
    companion object {
        private const val PREF_NAME = "TakeItFreePref"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_ICON_SIZE = "KEY_ICON_SIZE"
        private const val SEND_MESSAGE = "send_message";
        private const val TOPIC_MESSAGE = "TOPIC_MESSAGE";
        private const val KEY_DIM = "KEY_DIM"
        private const val KEY_DELAY = "KEY_DELAY"
        private const val KEY_ICON_BORDER_ON = "KEY_ICON_BORDER"
        private const val KEY_ICON_BACK_ON = "KEY_ICON_BACK"
        private const val RED_CODE = "RED_CODE"
        private const val GREEN_CODE = "GREEN_CODE"
        private const val BLUE_CODE = "BLUE_CODE"
        private const val SELECTED_BUILDING_ID = "SELECTED_BUILDING_ID"
        private const val MQTT_MESSAGE = "MQTT_MESSAGE"
        private const val MQTT_MESSAGE_NEW = "MQTT_MESSAGE_NEW"
        var KEY_IP_STATUS = "ipStatus"
        var KEY_SOUND_PLAY = "sound_play"
        var KEY_CHANGE_FONT = "change_font"
        var KEY_VIBRATION = "vibration"
        var KEY_TOKEN = "token"
        var KEY_IP = "ip"
        var KEY_SECRET = "secret"
        var KEY_EXPIRE_TOKEN = "expire_token"
        var ID = "id"
        var KEY_PROJECT_ID = "project_id"
        var KEY_CLIENT_ID = "key_client_id"
        var NAME = "name"
        var EMAIL = "email"
        var PHONE_NUMBER = "phone_number"
        var PROJECTS_COUNT = "project_count"
        var USER_IMAGE = "user_image"
        private const val IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch"
        private const val JOB_IP_LAYOUT_SHOWING = "job_ip_layout_showing"
        private const val KEY_MQTT_IP = "mqtt_ip"
        private const val KEY_FONT = "default_font"
        private const val KEY_CITY = "KEY_CITY"
        private const val KEY_FONT_SIZE = "default_font_size"
        private const val KEY_IS_NIGHT_MODE = "isNightModeOn"
        private const val BLUETOOTH_ADDRESS = "bluetooth_address"
        private const val KEY_RGBW_DEVICE_A0 = "rgbwDeviceA0"
        private const val KEY_RGBW_MASTER_ID = "key_rgbw_master_id"
        private const val KEY_ICON_SINGLE = "key_icon_single"
        private const val KEY_AVERAGEـPITCH = "key_averageـpitch"
        private const val KEY_ICON_RGB = "key_icon_rgb"
        private const val KEY_ICON_GROUP = "key_icon_group"
        private const val KEY_ICON_TUNABLE = "key_icon_tunable"
        private const val KEY_CLICKED = "KEY_CLICKED"
        private const val BLUETOOTH_CONNECTION = "BluetoothConnection"
        private const val FAST_SCAN_TOUCH = "FAST_SCAN_TOUCH"
        private const val IS_PORTRAIT = "IS_PORTRAIT"
        private const val QUERY_ACTUAL_LEVEL = "QUERY_ACTUAL_LEVEL"
        private const val QUERY_LONG_TOUCH_ID = "QUERY_LONG_TOUCH"
        private const val QUERY_LONG_TOUCH_MASTER = "QUERY_LONG_TOUCH_MASTER"
        private const val USER_KEY_NAME = "user_key_name"
        private const val USER_KEY_EMAIL = "user_key_email"
        private const val USER_KEY_IMAGE = "user_key_image"
        private const val BUILDING_IMAGE = "building_image"
        private const val USER_KEY_PROJECT_ID = "user_key_project_id"
        private const val USER_KEY_ACCESS_TOKEN = "user_key_access_token"
        private const val KEY_SETTING_LAYOUT = "key_setting_layout"
        private const val KEY_SCREEN_ORIENTATION = "key_screen_orientation"
        private const val KEY_SCREEN_ANGLE = "key_screen_angle"
        private const val KEY_LOCAL_JOB_IP = "key_local_job_ip"
        private const val KEY_ICON_BORDER_OFF = "key_icon_border_off"
        private const val KEY_ICON_BACK_OFF = "key_icon_back_off"
        private const val KEY_ICON_DAPC = "key_icon_dapc"

        // Shared Preferences
        private lateinit var pref: SharedPreferences
        private lateinit var editor: SharedPreferences.Editor
//region Vare

        //endregion
        var ipStatus: Int
            get() = pref.getInt(KEY_IP_STATUS, View.VISIBLE)
            set(offset) {
                editor.putInt(KEY_IP_STATUS, offset)
                editor.commit()
            }

        var changeFont: Boolean
            get() = pref.getBoolean(KEY_CHANGE_FONT, false)
            set(playSound) {
                editor.putBoolean(KEY_CHANGE_FONT, playSound)
                editor.commit()
            }


        var queryLongTouchID: Int
            get() = pref.getInt(QUERY_LONG_TOUCH_ID, -1)
            set(isFirstTime) {
                editor.putInt(QUERY_LONG_TOUCH_ID, isFirstTime)
                editor.commit()
            }

        var queryLongTouchMaster: Int
            get() = pref.getInt(QUERY_LONG_TOUCH_MASTER, -1)
            set(isFirstTime) {
                editor.putInt(QUERY_LONG_TOUCH_MASTER, isFirstTime)
                editor.commit()
            }

        var isFirstTimeLaunch: Boolean
            get() = pref.getBoolean(IS_FIRST_TIME_LAUNCH, true)
            set(isFirstTime) {
                editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime)
                editor.commit()
            }


        fun getValue(key: String?): String? {
            return pref.getString(key, null)
        }

        var dim: Int
            get() = pref.getInt(KEY_DIM, 0)
            set(iconSize) {
                editor.putInt(KEY_DIM, iconSize)
                editor.commit()
            }

        fun setValue(
            key: String?,
            value: String?
        ) {
            editor.putString(key, value)
            editor.commit()
        }

        var mQTTMessage: String?
            get() = pref.getString(MQTT_MESSAGE, "")
            set(message) {
                editor.putString(MQTT_MESSAGE, message)
                editor.commit()
            }
        var newMQTTMessage: String?
            get() = pref.getString(MQTT_MESSAGE_NEW, "")
            set(message) {
                editor.putString(MQTT_MESSAGE_NEW, message)
                editor.commit()
            }

        //rgbwDeviceA0 = null, rgbwMasterID
        var rgbwDeviceA0: String?
            get() = pref.getString(KEY_RGBW_DEVICE_A0, null)
            set(rgbwDeviceA0) {
                editor.putString(KEY_RGBW_DEVICE_A0, rgbwDeviceA0)
                editor.commit()
            }
        var rgbwMasterID: String?
            get() = pref.getString(KEY_RGBW_MASTER_ID, null)
            set(rgbwMasterID) {
                editor.putString(KEY_RGBW_MASTER_ID, rgbwMasterID)
                editor.commit()
            }


        fun setDelay(delay: Int) {
            editor.putInt(KEY_DELAY, delay)
            editor.commit()
        }

        val getDelay: Int
            get() = pref.getInt(KEY_DELAY, 100)
        var clicked: Boolean
            get() = pref.getBoolean(KEY_CLICKED, false)
            set(clicked) {
                editor.putBoolean(KEY_CLICKED, clicked)
                editor.commit()
            }

        //markup: - Test
        var localJobIp: String?
            get() = pref.getString(KEY_LOCAL_JOB_IP, Constants.SERVER_IP)
            set(rgbwMasterID) {
                editor.putString(KEY_LOCAL_JOB_IP, rgbwMasterID)
                editor.commit()
            }
        var font: String?
            get() = pref.getString(KEY_FONT, "roboto_thin.ttf")
            set(rgbwMasterID) {
                editor.putString(KEY_FONT, rgbwMasterID)
                editor.commit()
            }
        var city: String
            get() = pref.getString(KEY_CITY, "Tehran,Iran")!!
            set(rgbwMasterID) {
                editor.putString(KEY_CITY, rgbwMasterID)
                editor.commit()
            }
        var fontSize: Float
            get() = pref.getFloat(KEY_FONT_SIZE, 16f)
            set(rgbwMasterID) {
                editor.putFloat(KEY_FONT_SIZE, rgbwMasterID)
                editor.commit()
            }
        var mqttIP: String?
            get() = pref.getString(KEY_MQTT_IP, Constants.MQTT_SERVER_URI)
            set(rgbwMasterID) {
                editor.putString(KEY_MQTT_IP, rgbwMasterID)
                editor.commit()
            }


        var buildingImage: String?
            get() = pref.getString(BUILDING_IMAGE, "")
            set(userImageLocal) {
                editor.putString(BUILDING_IMAGE, userImageLocal)
                editor.commit()
            }
    }


    var jobIpLayoutShowing: Boolean
        get() = pref.getBoolean(JOB_IP_LAYOUT_SHOWING, false)
        set(isFirstTime) {
            editor.putBoolean(JOB_IP_LAYOUT_SHOWING, isFirstTime)
            editor.commit()
        }

    var projectId: String?
        get() = pref.getString(KEY_PROJECT_ID, null)
        set(projectId) {
            editor.putString(KEY_PROJECT_ID, projectId)
            editor.commit()
        }


    var mqttClientId: String?
        get() = pref.getString(KEY_CLIENT_ID, null)
        set(projectId) {
            editor.putString(KEY_CLIENT_ID, projectId)
            editor.commit()
        }

    var isBluetoothConnection: Boolean
        get() = pref.getBoolean(BLUETOOTH_CONNECTION, false)
        set(bluetoothConnection) {
            editor.putBoolean(BLUETOOTH_CONNECTION, bluetoothConnection)
            editor.commit()
        }

    var portraitScreen: Boolean
        get() = pref.getBoolean(IS_PORTRAIT, false)
        set(isPortrait) {
            editor.putBoolean(IS_PORTRAIT, isPortrait)
            editor.commit()
        }

    var fastScanTouch: Boolean
        get() = pref.getBoolean(FAST_SCAN_TOUCH, false)
        set(bluetoothConnection) {
            editor.putBoolean(FAST_SCAN_TOUCH, bluetoothConnection)
            editor.commit()
        }

    var queryActualLevel: Int
        get() = pref.getInt(QUERY_ACTUAL_LEVEL, 0)
        set(iconResId) {
            editor.putInt(QUERY_ACTUAL_LEVEL, iconResId)
            editor.commit()
        }

    var sendMessage: String?
        get() = pref.getString(SEND_MESSAGE, null)
        set(city) {
            editor.putString(SEND_MESSAGE, city)
            editor.commit()
        }

    var topic: String?
        get() = pref.getString(TOPIC_MESSAGE, null)
        set(topic) {
            editor.putString(TOPIC_MESSAGE, topic)
            editor.commit()
        }

    var bluetoothAddress: String?
        get() = pref.getString(BLUETOOTH_ADDRESS, null)
        set(city) {
            editor.putString(BLUETOOTH_ADDRESS, city)
            editor.commit()
        }


    var userImageLocal: String?
        get() = pref.getString(USER_KEY_IMAGE, "")
        set(userImageLocal) {
            editor.putString(USER_KEY_IMAGE, userImageLocal)
            editor.commit()
        }

//    var singleIcon: Int
//        get() = pref.getInt(KEY_ICON_SINGLE, R.drawable.ic_icon_1)
//        set(iconResId) {
//            editor.putInt(KEY_ICON_SINGLE, iconResId)
//            editor.commit()
//        }

    var averagePitch: Float
        get() = pref.getFloat(KEY_AVERAGEـPITCH, 0.toFloat())
        set(iconResId) {
            editor.putFloat(KEY_AVERAGEـPITCH, iconResId)
            editor.commit()
        }

    var screenAngle: Int
        get() = pref.getInt(KEY_SCREEN_ANGLE, 0)
        set(value) {
            editor.putInt(KEY_SCREEN_ANGLE, value)
            editor.commit()
        }

    //MARK - MQTT IP
    var mqttIp: String?
        get() = pref.getString(KEY_MQTT_IP, Constants.MQTT_SERVER_URI)
        set(mQTTIp) {
            editor.putString(KEY_MQTT_IP, mQTTIp)
            editor.commit()
        }

    var screenOrientation: String?
        get() = pref.getString(KEY_SCREEN_ORIENTATION, Constants.KEY_LANDSCAPE)
        set(value) {
            editor.putString(KEY_SCREEN_ORIENTATION, value)
            editor.commit()
        }

//    var rgbIcon: Int
//        get() = pref.getInt(KEY_ICON_RGB, R.drawable.ic_rgb_on_1)
//        set(iconResId) {
//            editor.putInt(KEY_ICON_RGB, iconResId)
//            editor.commit()
//        }
//
//    var groupIcon: Int
//        get() = pref.getInt(KEY_ICON_GROUP, R.drawable.ic_group_on_1)
//        set(iconResId) {
//            editor.putInt(KEY_ICON_GROUP, iconResId)
//            editor.commit()
//        }
//
//    var tunableIcon: Int
//        get() = pref.getInt(KEY_ICON_TUNABLE, R.drawable.ic_tunable_02_on)
//        set(iconResId) {
//            editor.putInt(KEY_ICON_TUNABLE, iconResId)
//            editor.commit()
//        }

    var iconSize: Int
        get() = pref.getInt(KEY_ICON_SIZE, (40 * context.resources.displayMetrics.density).toInt())
        set(iconSize) {
            editor.putInt(KEY_ICON_SIZE, iconSize)
            editor.commit()
        }

    var iconAlpha: Float
        get() = pref.getFloat(KEY_ICON_DAPC, 0.2f)
        set(iconSize) {
            editor.putFloat(KEY_ICON_DAPC, iconSize)
            editor.commit()
        }


    var iconBorderColorOn: String?
        get() = pref.getString(KEY_ICON_BORDER_ON, "#FFFF00")
        set(rgbwMasterID) {
            editor.putString(KEY_ICON_BORDER_ON, rgbwMasterID)
            editor.commit()
        }
    var iconBackColorOn: String?
        get() = pref.getString(KEY_ICON_BACK_ON, "#000000")
        set(rgbwMasterID) {
            editor.putString(KEY_ICON_BACK_ON, rgbwMasterID)
            editor.commit()
        }

    var iconBorderColorOff: String?
        get() = pref.getString(KEY_ICON_BORDER_OFF, "#FFFF00")
        set(rgbwMasterID) {
            editor.putString(KEY_ICON_BORDER_OFF, rgbwMasterID)
            editor.commit()
        }
    var iconBackColorOff: String?
        get() = pref.getString(KEY_ICON_BACK_OFF, "#000000")
        set(rgbwMasterID) {
            editor.putString(KEY_ICON_BACK_OFF, rgbwMasterID)
            editor.commit()
        }


    var redCode: Int
        get() = pref.getInt(RED_CODE, 0)
        set(redCode) {
            editor.putInt(RED_CODE, redCode)
            editor.commit()
        }

    var greenCode: Int
        get() = pref.getInt(GREEN_CODE, 0)
        set(redCode) {
            editor.putInt(GREEN_CODE, redCode)
            editor.commit()
        }
    var blueCode: Int
        get() = pref.getInt(BLUE_CODE, 0)
        set(redCode) {
            editor.putInt(BLUE_CODE, redCode)
            editor.commit()
        }

    var selectedBuildingId: Int
        get() = pref.getInt(SELECTED_BUILDING_ID, -1)
        set(redCode) {
            editor.putInt(SELECTED_BUILDING_ID, redCode)
            editor.commit()
        }

    var playSound: Boolean
        get() = pref.getBoolean(KEY_SOUND_PLAY, true)
        set(playSound) {
            editor.putBoolean(KEY_SOUND_PLAY, playSound)
            editor.commit()
        }

    var vibration: Boolean
        get() = pref.getBoolean(KEY_VIBRATION, true)
        set(playSound) {
            editor.putBoolean(KEY_VIBRATION, playSound)
            editor.commit()
        }


    var isNightModeOn: Boolean
        get() = pref.getBoolean(KEY_IS_NIGHT_MODE, true)
        set(isNightModeOn) {
            editor.putBoolean(KEY_IS_NIGHT_MODE, isNightModeOn)
            editor.commit()
        }

    var isLoggedIn: Boolean
        get() = pref.getBoolean(KEY_IS_LOGGED_IN, false)
        set(loggedIn) {
            editor.putBoolean(KEY_IS_LOGGED_IN, loggedIn)
            editor.commit()
        }

    var user: User
        get() {
            val user = User()
            user.name = pref.getString(USER_KEY_NAME, "")
            user.email = pref.getString(USER_KEY_EMAIL, "")
            user.image = pref.getString(USER_KEY_IMAGE, "")
            user.projectId = pref.getInt(USER_KEY_PROJECT_ID, 0)
            user.accessToken = pref.getString(USER_KEY_ACCESS_TOKEN, "")
            return user
        }
        set(user) {
            editor.putString(USER_KEY_NAME, user.name)
            editor.putString(USER_KEY_EMAIL, user.email)
            editor.putString(USER_KEY_IMAGE, user.image)
            editor.putInt(USER_KEY_PROJECT_ID, (if (user.projectId != null) user.projectId else 0)!!)
            editor.putString(USER_KEY_ACCESS_TOKEN, "Bearer ${user.accessToken}")
            editor.commit()
        }

    init {
        // Shared pref mode
        val PRIVATE_MODE = 0
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        editor = pref.edit()
    }
}