package com.hsi.walldisplay.ui.login

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.core.view.ViewCompat
import com.hsi.walldisplay.application.BaseActivity
import com.hsi.walldisplay.databinding.ActivityLoginBinding
import com.hsi.walldisplay.helper.InsetsWithKeyboardAnimationCallback
import com.hsi.walldisplay.helper.InsetsWithKeyboardCallback
import com.hsi.walldisplay.ui.login.viewmodel.LoginViewModel
import com.tbruyelle.rxpermissions3.RxPermissions


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

        }


    }


    fun restartActivity() {
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