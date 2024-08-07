package com.hsi.walldisplay.ui.login

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import androidx.core.view.ViewCompat
import com.google.android.material.snackbar.Snackbar
import com.hsi.walldisplay.application.BaseActivity
import com.hsi.walldisplay.databinding.ActivityLoginBinding
import com.hsi.walldisplay.helper.Constants
import com.hsi.walldisplay.helper.InsetsWithKeyboardAnimationCallback
import com.hsi.walldisplay.helper.InsetsWithKeyboardCallback
import com.hsi.walldisplay.helper.SessionManager
import com.hsi.walldisplay.model.LoginResponse
import com.hsi.walldisplay.model.User
import com.hsi.walldisplay.network.RetroClass
import com.hsi.walldisplay.ui.login.viewmodel.LoginViewModel
import com.hsi.walldisplay.ui.main.MainActivity
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
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels

        log("LoginActivity width:$width  height:$height  ${pxToDp(width)} ")
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


    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

}