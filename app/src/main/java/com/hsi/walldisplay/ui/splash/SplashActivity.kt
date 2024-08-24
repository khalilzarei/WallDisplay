package com.hsi.walldisplay.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import com.hsi.walldisplay.application.BaseActivity
import com.hsi.walldisplay.ui.login.LoginActivity
import com.hsi.walldisplay.ui.main.MainActivity

class SplashActivity : BaseActivity() {


    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        log("O")
        setFontAndFontSize()
        openActivity()
    }


    private fun openActivity() {
//        Handler(Looper.getMainLooper()).postDelayed({

        var intent = Intent(this, MainActivity::class.java)
        if (!sessionManager.isLoggedIn) {
            intent = Intent(this, LoginActivity::class.java)
        }
        startActivity(intent)
        finish()

//        }, 3000)

    }

}