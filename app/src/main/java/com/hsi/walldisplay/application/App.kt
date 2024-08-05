package com.hsi.walldisplay.application

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.hsi.walldisplay.helper.SessionManager

class App : Application() {


    override fun onCreate() {
        super.onCreate()
        val sessionManager = SessionManager(this)
        if (sessionManager.isNightModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

//                TypefaceUtil.overrideFont(this, "fonts/" + SessionManager.getFont() + ".TTF");
//                ViewPump.init(
//                        ViewPump.builder()
//                                .addInterceptor(
//                                        new CalligraphyInterceptor(
//                                                new CalligraphyConfig.Builder()
//                                                        .setDefaultFontPath("fonts/" + SessionManager.getFont() + ".TTF")
//                                                        .setFontAttrId(R.attr.fontPath)
//                                                        .build()
//                                        )
//                                ).build());
    }

}