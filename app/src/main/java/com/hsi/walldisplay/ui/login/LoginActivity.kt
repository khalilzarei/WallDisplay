package com.hsi.walldisplay.ui.login

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import com.google.android.material.snackbar.Snackbar
import com.hsi.walldisplay.ui.main.MainActivity
import com.hsi.walldisplay.application.BaseActivity
import com.hsi.walldisplay.databinding.ActivityLoginBinding
import com.hsi.walldisplay.databinding.DialogDownloadProgressBinding
import com.hsi.walldisplay.helper.Constants
import com.hsi.walldisplay.helper.InsetsWithKeyboardAnimationCallback
import com.hsi.walldisplay.helper.InsetsWithKeyboardCallback
import com.hsi.walldisplay.helper.SessionManager
import com.hsi.walldisplay.model.LoginResponse
import com.hsi.walldisplay.model.User
import com.hsi.walldisplay.network.RetroClass
import com.hsi.walldisplay.ui.login.viewmodel.LoginViewModel
import com.tbruyelle.rxpermissions3.RxPermissions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Date


class LoginActivity : BaseActivity() {

    val binding: ActivityLoginBinding by lazy { ActivityLoginBinding.inflate(layoutInflater) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        if (sessionManager.isLoggedIn) {
//            Intent(this, MainActivity::class.java)
//                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
//                .also {
//                    startActivity(it)
//                    finish()
//                }
//        }
        setContentView(binding.root)

        activity = this
        view = binding.root
        val viewModel = LoginViewModel(this)
        binding.viewModel = viewModel

        log("LoginActivity Oncreate ")
        requestStoragePermissions()


        val insetsWithKeyboardCallback = InsetsWithKeyboardCallback(window)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root, insetsWithKeyboardCallback)
        ViewCompat.setWindowInsetsAnimationCallback(binding.root, insetsWithKeyboardCallback)


        val insetsWithKeyboardAnimationCallback = InsetsWithKeyboardAnimationCallback(binding.btnLogin)
        ViewCompat.setWindowInsetsAnimationCallback(binding.btnLogin, insetsWithKeyboardAnimationCallback)


        binding.tilJobIp.setEndIconOnClickListener {
            val ip = viewModel.localJobIp
            activity.log("jobIpEndIconClicked => Job Ip : $ip")
            if (ip.isEmpty() || ip == "Local ip" || ip.length < 5) {
                activity.toast("PLEASE ENTER LOCAL IP FOR JOBS")
//                    Snackbar.make(binding.root, "PLEASE ENTER LOCAL IP FOR JOBS", Toast.LENGTH_LONG).show()
                return@setEndIconOnClickListener
            }
            if (!ip.contains("http")) {
                activity.toast("PLEASE start IP with http")
//                    Snackbar.make(binding.root, "PLEASE start IP with http or https", Snackbar.LENGTH_LONG).show()
                return@setEndIconOnClickListener
            }

            if (ip.last() != '/') {
                activity.toast("PLEASE ENTER end IP with / ")
                return@setEndIconOnClickListener
            }

            activity.toast("CHANGE LOCAL IP JOBS SUCCESS FULL")
            SessionManager.localJobIp = ip
//           url = SessionManager.localJobIp + Constants.API_URL

            Handler(Looper.getMainLooper()).postDelayed({
                viewModel.jobIpLayoutShowing = false
                sessionManager.jobIpLayoutShowing = false
                activity.log("${SessionManager.localJobIp}")
                restartActivity()
            }, 1000)
        }

    }


    private fun restartActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    @SuppressLint("CheckResult")
    private fun requestStoragePermissions() {
        val rxPermissions = RxPermissions(activity)
        rxPermissions.request(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
            .subscribe({ aBoolean: Boolean? ->

            }) { obj: Throwable -> obj.printStackTrace() }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            rxPermissions.request(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
                .subscribe({ aBoolean: Boolean? ->

                }) { obj: Throwable -> obj.printStackTrace() }
        }
    }

    private fun login(
        email: String,
        password: String
    ) {

        val progressDialog = ProgressDialog(activity)
        progressDialog.setMessage("Please wait...")
        progressDialog.show()

        val apiService = RetroClass.apiService(activity.url)
        var sync = if (activity.url.contains("192")) 0 else 1
        sync = 0
        activity.log("login => sync => $sync  url => ${activity.url} ")
        val responseCall = apiService.login(email, password, sync)
        responseCall.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                if (response.isSuccessful) {
                    activity.log("login $response.toString()")
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        if (loginResponse.succeeded == true) {

                            val user: User? = loginResponse.user
                            activity.sessionManager.user = user!!
                            activity.sessionManager.isLoggedIn = true

                            val fileName = "${user.name}_${user.projectId}_${user.projectId}.jpg"
                            val downloadResult = activity.downloadFile(user.image.toString(), fileName)

                            activity.log("downloadResult Login: $downloadResult")
                            activity.sessionManager.userImageLocal =
                                activity.filesDir.absolutePath + "/" + fileName

                            MediaScannerConnection.scanFile(
                                activity,
                                arrayOf(activity.sessionManager.userImageLocal),
                                null
                            ) { path1: String?, _: Uri? ->
                                activity.sessionManager.userImageLocal = path1
                            }

                            activity.log("Image Url" + activity.sessionManager.user.image)

//                            val telephonyManager = getSystemService<Any>(Context.TELEPHONY_SERVICE) as TelephonyManager?
//                            telephonyManager!!.deviceId
//                            val imei = android.telephony.TelephonyManager


                            activity.sessionManager.mqttClientId = "${Constants.CLIENT_ID}_${Date().time}"

                            Handler(Looper.getMainLooper()).postDelayed(
                                {
                                    progressDialog.dismiss()
                                    activity.startActivity(
                                        Intent(
                                            activity,
                                            MainActivity::class.java
                                        )
                                    )
                                    activity.finish()
                                },
                                1000
                            )
                        } else activity.showErrorSnackBar(loginResponse.message.toString())
                    }
                } else {
                    Handler(Looper.getMainLooper())
                        .postDelayed(progressDialog::dismiss, 1000)
                    Snackbar.make(
                        binding.btnLogin,
                        "Something went wrong. Please try again",
                        Snackbar.LENGTH_INDEFINITE
                    )
                        .setAction("OK") {}
                        .show()
                }
            }

            override fun onFailure(
                call: Call<LoginResponse>,
                t: Throwable
            ) {
                activity.log("onFailure ${t.message} ")
                Handler(Looper.getMainLooper())
                    .postDelayed(progressDialog::dismiss, 1000)

                Snackbar.make(
                    binding.btnLogin,
                    "Something went wrong. Please check user name and password",
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setBackgroundTint(Color.RED)
                    .setAction("Ok", null)
                    .setTextColor(Color.YELLOW)
                    .show()
                Handler(Looper.getMainLooper()).postDelayed({ progressDialog.dismiss() }, 1000)
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

}