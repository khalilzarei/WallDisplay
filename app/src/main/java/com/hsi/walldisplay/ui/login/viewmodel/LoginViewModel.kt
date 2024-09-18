package com.hsi.walldisplay.ui.login.viewmodel

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.google.android.material.snackbar.Snackbar
import com.hsi.walldisplay.BR
import com.hsi.walldisplay.R
import com.hsi.walldisplay.databinding.DialogChangeIpBinding
import com.hsi.walldisplay.databinding.DialogDownloadProgressBinding
import com.hsi.walldisplay.helper.Constants
import com.hsi.walldisplay.helper.Constants.API_URL
import com.hsi.walldisplay.helper.SessionManager
import com.hsi.walldisplay.model.*
import com.hsi.walldisplay.network.RetroClass
import com.hsi.walldisplay.ui.login.LoginActivity
import com.hsi.walldisplay.ui.main.MainActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Date
import kotlin.properties.Delegates


class LoginViewModel(var activity: LoginActivity) : BaseObservable() {

    //region variable

    @Bindable
//    var email: String? = "dt8@gruneslicht.de"
    var email: String? = "hsilightingstudio@gruneslicht.de"

    //dt8@g...  hsilightingstudio

    @Bindable
    var password: String? = "Password123"

    @get:Bindable
    var jobIpLayoutShowing: Boolean by Delegates.observable(activity.sessionManager.jobIpLayoutShowing) { _, _, _ ->
        notifyPropertyChanged(BR.jobIpLayoutShowing)
    }

    @get:Bindable
    var localJobIp: String by Delegates.observable(SessionManager.localJobIp.toString()) { _, _, _ ->
        notifyPropertyChanged(BR.localJobIp)
    }

    @get:Bindable
    var mqttIP: String by Delegates.observable(SessionManager.mqttIP.toString()) { _, _, _ ->
        notifyPropertyChanged(BR.mqttIP)
    }


    private var downloadProgressDialog: AlertDialog
    private var downloadBinding: DialogDownloadProgressBinding
    private var dialogChangeIp: AlertDialog
    private val changeIpBinding: DialogChangeIpBinding by lazy { DialogChangeIpBinding.inflate(activity.layoutInflater) }

    //endregion

    //region INIT

    init {
        val builder = AlertDialog.Builder(activity)
        downloadBinding = DialogDownloadProgressBinding.inflate(activity.layoutInflater)
        builder.setView(downloadBinding.root)
        downloadProgressDialog = builder.create()
        downloadProgressDialog.setCancelable(false)
        downloadProgressDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val builderChangeIP = AlertDialog.Builder(activity)
        builderChangeIP.setView(changeIpBinding.root)
        dialogChangeIp = builderChangeIP.create()

    }
    //endregion

    //region OnClick
    fun checkLogin(view: View) {
        if (email == null || email!!.isEmpty()) {
            activity.showErrorSnackBar(activity.getString(R.string.error_text_email) + "")
            return
        }
        if (!activity.isEmailValid(email)) {
            activity.showErrorSnackBar(activity.getString(R.string.error_text_email) + "")
            return
        }
        if (password == null || password!!.isEmpty()) {
            activity.showErrorSnackBar(activity.getString(R.string.error_text_password) + "")
            return
        }
//        if (activity.isNetworkConnected) {
//            login(view)
//        } else {
//            activity.showErrorSnackBar("Please check internet connection!")
//        }

        activity.log("Login => ${activity.url}")
        login(view)
    }

    var count = 0
    fun showJobIpLayout(view: View?) {
        count++
        if (count > 10) {
            changeIps()
            count = 0
        }
    }


    private fun changeIps() {
        dialogChangeIp.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogChangeIp.setCanceledOnTouchOutside(false)
        dialogChangeIp.show()
        changeIpBinding.viewModel = this
        changeIpBinding.btnClose.setOnClickListener {
            dialogChangeIp.dismiss()
            count = 0
        }
    }

    //endregion


    //region Method

    private fun login(view: View) {
        downloadProgressDialog.show()

        val apiService = RetroClass.apiService("${SessionManager.localJobIp}$API_URL")
        var sync = if (activity.url.contains("192")) 0 else 1
        sync = 0
        activity.log("login => sync => $sync  url => ${activity.url} ")
        val responseCall = apiService.login(email, password, sync)
        responseCall.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>, response: Response<LoginResponse>
            ) {
                if (response.isSuccessful) {
                    activity.log("login $response.toString()")
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        if (loginResponse.succeeded == true && loginResponse.user != null) {
                            val user: User = loginResponse.user!!
                            val fileName = "${user.name}_${user.projectId}_${user.projectId}.jpg"

                            PRDownloader.download(
                                user.image,
                                activity.filesDir.absolutePath,
                                fileName,
                            ).build().setOnProgressListener { progress ->
                                val progressPercent: Long = progress.currentBytes * 100 / progress.totalBytes
                                downloadBinding.progressBar.progress =
                                    progressPercent.toInt() //                            dialogDownload.tvTitle.text = ""
                                downloadBinding.progressBar.isIndeterminate = false
                            }.start(
                                object : OnDownloadListener {
                                    override fun onDownloadComplete() {
                                        activity.logD(
                                            "onDownloadComplete ${activity.filesDir.absolutePath}/$fileName"
                                        )
                                        val imageUrl = "${activity.filesDir.absolutePath}/$fileName"
                                        user.image = imageUrl
                                        activity.sessionManager.userImageLocal = imageUrl
                                        activity.sessionManager.user = user
                                        activity.sessionManager.mqttClientId = "${Constants.CLIENT_ID}_${Date().time}"
                                        activity.sessionManager.isLoggedIn = true
                                        getBuildings()
                                    }

                                    override fun onError(error: com.downloader.Error?) {
                                        activity.logD("onError  $error")
                                        Toast.makeText(
                                            activity,
                                            "Failed to download the ",
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                    }
                                },
                            )

//                            dismissProgressDialog()
//                            activity.startActivity(
//                                Intent(
//                                    activity,
//                                    MainActivity::class.java
//                                )
//                            )

                        } else activity.showErrorSnackBar(loginResponse.message.toString())
                    }
                } else {
                    dismissProgressDialog()
                    Snackbar.make(
                        view, "Something went wrong. Please try again", Snackbar.LENGTH_INDEFINITE
                    ).setAction("OK") {}.show()
                }
            }

            override fun onFailure(
                call: Call<LoginResponse>, t: Throwable
            ) {
                activity.log("onFailure ${t.message} ")
                dismissProgressDialog()
                activity.showErrorSnackBar("Something went wrong. Please check user name and password")
            }
        })
    }

    fun dismissProgressDialog() {
        if (downloadProgressDialog.isShowing)
            Handler(Looper.getMainLooper()).postDelayed({
                if (downloadProgressDialog.isShowing)
                    downloadProgressDialog.dismiss()
            }, 1000)
    }

    fun checkLogin() {

    }


    fun saveLocalIP(view: View) {
        val ip = localJobIp
        activity.log("jobIpEndIconClicked => Job Ip : $ip")
        if (ip.isEmpty() || ip == "Local ip" || ip.length < 5) {
            activity.toast("PLEASE ENTER LOCAL IP FOR JOBS")
//                    Snackbar.make(binding.root, "PLEASE ENTER LOCAL IP FOR JOBS", Toast.LENGTH_LONG).show()
            return
        }
        if (!ip.contains("http")) {
            activity.toast("PLEASE start IP with http")
//                    Snackbar.make(binding.root, "PLEASE start IP with http or https", Snackbar.LENGTH_LONG).show()
            return
        }

        if (ip.last() != '/') {
            activity.toast("PLEASE ENTER end IP with / ")
            return
        }

        activity.toast("CHANGE LOCAL IP JOBS SUCCESS FULL")
        SessionManager.localJobIp = ip
    }

    fun saveMqttIP(view: View) {
        activity.log("jobIpEndIconClicked => Job Ip : $mqttIP")
        if (mqttIP.isEmpty() || mqttIP.length < 5) {
            activity.toast("PLEASE ENTER MQTT IP")
//                    Snackbar.make(binding.root, "PLEASE ENTER LOCAL IP FOR JOBS", Toast.LENGTH_LONG).show()
            return
        }
        if (!mqttIP.contains("tcp")) {
            activity.toast("PLEASE start IP with tcp")
//                    Snackbar.make(binding.root, "PLEASE start IP with http or https", Snackbar.LENGTH_LONG).show()
            return
        }
        activity.toast("CHANGE MQTT IP JOBS SUCCESS FULL")
        SessionManager.mqttIP = mqttIP
    }

    //endregion

    //region getBuildings

    private fun getBuildings() {
        val apiService = RetroClass.apiService(activity.url)
        val responseCall = apiService.getBuildings(activity.sessionManager.user.accessToken)
        responseCall.enqueue(object : Callback<BuildingResponse> {
            override fun onResponse(
                call: Call<BuildingResponse>, response: Response<BuildingResponse>
            ) {
                if (response.isSuccessful) {
                    activity.logD("getBuildings $response")
                    val responseBody = response.body()
                    if (responseBody != null) {
                        if (responseBody.succeeded == true) {
                            var count = 0
                            for (building in responseBody.data) {

                                val projectId = activity.sessionManager.user.projectId
                                val imgName = "${building.name}_${building.id}_$projectId.jpg"
                                SessionManager.buildingImage = building.image
                                val buildImageUrl = activity.filesDir.absolutePath + "/" + imgName

                                activity.log("downloadResult : ${building.image}")

                                PRDownloader.download(
                                    building.image.toString(),
                                    activity.filesDir.absolutePath,
                                    imgName,
                                ).build().setOnProgressListener { progress ->
                                    val progressPercent: Long = progress.currentBytes * 100 / progress.totalBytes
                                    downloadBinding.progressBar.progress =
                                        progressPercent.toInt() //                            dialogDownload.tvTitle.text = ""
                                    downloadBinding.progressBar.isIndeterminate = false
                                }.start(
                                    object : OnDownloadListener {
                                        override fun onDownloadComplete() {
                                            count++
                                            activity.logD("getBuildings onDownloadComplete $count ${responseBody.data.size} $buildImageUrl")
                                            building.image = buildImageUrl
                                            activity.dataBaseDao.insertBuilding(building)
                                            if (count == responseBody.data.size)
                                                Handler(Looper.getMainLooper()).postDelayed({ getServices() }, 50)
                                        }

                                        override fun onError(error: com.downloader.Error?) {
                                            activity.logD("onError  $error")
                                            activity.toast("Failed to download the ")
                                        }
                                    },
                                )

                            }


                        } else activity.showErrorSnackBar("Error ${responseBody.message}")
                    }
                }
            }

            override fun onFailure(
                call: Call<BuildingResponse>, t: Throwable
            ) {
                activity.log("onFailure Building : ${t.message}")
//                progressDialog.dismiss()
            }
        })
    }
    //endregion

    //region getServices

    private fun getServices() {
        val apiService = RetroClass.apiService(activity.url)
        val responseCall = apiService.getServices(activity.sessionManager.user.accessToken)
        responseCall.enqueue(object : Callback<ServiceResponse> {
            override fun onResponse(
                call: Call<ServiceResponse>,
                response: Response<ServiceResponse>
            ) {
                if (response.isSuccessful) {
                    activity.logD("getServices $response")
                    val responseBody = response.body()
                    if (responseBody != null) {
                        if (responseBody.succeeded == true) {

                            for (buildingService in responseBody.buildingServices) {
                                activity.logD("Home => ${buildingService.type} ${buildingService.value} ")
                                buildingService.name = when (buildingService.type) {
                                    DeviceType.RGB_DT6, DeviceType.RGB_DT8 -> if (buildingService.groupId != null) "RGB ${buildingService.groupId}" else "RGB ${buildingService.serviceId}"
                                    DeviceType.CCT_DT6, DeviceType.CCT_DT8 -> if (buildingService.groupId != null) "CCT ${buildingService.groupId}" else "CCT ${buildingService.serviceId}"
                                    DeviceType.RELAY -> "RELAY ${buildingService.serviceId}"
                                    DeviceType.DALI_LIGHT -> if (buildingService.groupId == null) "DALI LIGHT ${buildingService.serviceId}" else "GROUP ${buildingService.groupId}"
                                    else -> "${buildingService.type} ${buildingService.serviceId}"
                                }
                                activity.dataBaseDao.insertBuildingService(buildingService)
                                if (buildingService.type == DeviceType.CURTAIN) {
                                    activity.dataBaseDao.insertCurtain(
                                        Curtain(
                                            buildingService.id,
                                            buildingService.serviceId!!,
                                            0,
                                            buildingService.masterId,
                                            buildingService.buildingId,
                                            "Curtain ${buildingService.serviceId}",
                                        )
                                    )
                                }
                                val topicIn =
                                    "${activity.sessionManager.user.projectId}/${buildingService.masterId}/${Constants.DALI_IN}"
                                val topicOut =
                                    "${activity.sessionManager.user.projectId}/${buildingService.masterId}/${Constants.DALI_OUT}"

                                activity.log("TOPIC=> IN: $topicIn OUT: $topicOut")
                                if (!existTopic(topicIn)) {
                                    activity.dataBaseDao.insertTopic(Topic(topicIn))
                                }

                                if (!existTopic(topicOut)) {
                                    activity.log("topic : $topicOut ")
                                    activity.dataBaseDao.insertTopic(Topic(topicOut))
                                }
                            }

                        } else activity.showErrorSnackBar("Error ${responseBody.message}")
                    }
                }

//                Handler(Looper.getMainLooper()).postDelayed({progressDialog.dismiss()}, 1000)
                Handler(Looper.getMainLooper()).postDelayed({
                    getThermostat()
                }, 50)
            }

            override fun onFailure(
                call: Call<ServiceResponse>,
                t: Throwable
            ) {
                activity.log("onFailure Service : ${t.message}")
            }
        })
    }
    //endregion

    //region getThermostat

    private fun getThermostat() {
        val apiService = RetroClass.apiService(activity.url)
        val responseCall = apiService.getThermostats(activity.sessionManager.user.accessToken)
        responseCall.enqueue(object : Callback<ServiceResponse> {
            override fun onResponse(
                call: Call<ServiceResponse>,
                response: Response<ServiceResponse>
            ) {
                if (response.isSuccessful) {
                    activity.log("getServices $response")
                    val responseBody = response.body()
                    if (responseBody != null) {
                        if (responseBody.succeeded == true) {
                            for (buildingService in responseBody.buildingServices) {
                                activity.log("THERMOSTAT => ${buildingService.off} ${buildingService.serviceId} ")
                                buildingService.type = DeviceType.THERMOSTAT

                                activity.dataBaseDao.insertBuildingService(buildingService)

                                val topicIn = "${activity.sessionManager.user.projectId}/Thermo/In"
                                val topicOut = "${activity.sessionManager.user.projectId}/Thermo/Out"

                                activity.log("TOPIC=> IN: $topicIn OUT: $topicOut")
                                if (!existTopic(topicIn)) {
                                    activity.dataBaseDao.insertTopic(Topic(topicIn))
                                }

                                if (!existTopic(topicOut)) {
                                    activity.log("topic : $topicOut ")
                                    activity.dataBaseDao.insertTopic(Topic(topicOut))
                                }
                            }

                        } else activity.showErrorSnackBar("Error ${responseBody.message}")
                    }
                }

//                Handler(Looper.getMainLooper()).postDelayed({progressDialog.dismiss()}, 1000)
                Handler(Looper.getMainLooper()).postDelayed({
                    getScenes()
                }, 50)
            }

            override fun onFailure(
                call: Call<ServiceResponse>,
                t: Throwable
            ) {
                activity.log("onFailure Service : ${t.message}")
            }
        })
    }
    //endregion

    //region existTopic

    fun existTopic(topic: String): Boolean {
        if (activity.dataBaseDao.topicItems.isNotEmpty())
            for (item in activity.dataBaseDao.topicItems)
                if (item.topic == topic)
                    return true
        return false
    }
    //endregion

    //region getScenes

    private fun getScenes() {
        val apiService = RetroClass.apiService(activity.url)
        val responseCall = apiService.getScenes(activity.sessionManager.user.accessToken)
        responseCall.enqueue(object : Callback<SceneResponse> {
            override fun onResponse(
                call: Call<SceneResponse>,
                response: Response<SceneResponse>
            ) {
                if (response.isSuccessful) {
                    activity.log("getScene $response")
                    val responseBody = response.body()
                    if (responseBody != null) {
                        if (responseBody.succeeded == true) {
                            for (item in responseBody.scenes) {
                                activity.log("Scene name => ${item.name} , masterId => ${item.masterId}")
                                activity.dataBaseDao.insertScene(item)
                            }

                        } else activity.showErrorSnackBar("Error ${responseBody.message}")
                    }
                }
                dismissProgressDialog()
                openActivity()
                activity.sessionManager.isLoggedIn = true

            }

            override fun onFailure(
                call: Call<SceneResponse>,
                t: Throwable
            ) {
                activity.log("onFailure Service : ${t.message}")
                dismissProgressDialog()
            }
        })
    }

    //endregion


    //region openActivity
    private fun openActivity() {

        val intent = Intent(activity, MainActivity::class.java)
        activity.startActivity(intent)
        activity.finish()

    }
    //endregion
}