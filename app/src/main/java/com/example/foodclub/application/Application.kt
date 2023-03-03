package com.example.foodclub.application

import com.zeugmasolutions.localehelper.LocaleAwareApplication
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class Application : LocaleAwareApplication() {
    override fun onCreate() {
        super.onCreate()
       
    }

}
