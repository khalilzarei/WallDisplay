package com.hsi.walldisplay.application


import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.hsi.walldisplay.helper.SessionManager


class App : Application() {

    companion object {
//        @JvmStatic
//        var instance: App? = null
//            private set
    }

    override fun onCreate() {
        super.onCreate()
        val sessionManager = SessionManager(this)
        if (sessionManager.isNightModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
//        ViewPump.init(ViewPump.builder()
//            .addInterceptor(CalligraphyInterceptor(
//                CalligraphyConfig.Builder()
//                    .setDefaultFontPath("fonts/${SessionManager.font}")
//                    .setFontAttrId(R.attr.fontPath)
//                    .setFontMapper { font -> font }
//                    .addCustomViewWithSetTypeface(CustomViewWithTypefaceSupport::class.java)
//                    .addCustomStyle(TextField::class.java, R.attr.textFieldStyle)
//                    .build()))
//            .build())

    }

}