package com.example.foodclub.application

import com.zeugmasolutions.localehelper.LocaleAwareApplication
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class Application : LocaleAwareApplication() {
    override fun onCreate() {
        super.onCreate()
      /*  MapboxSearchSdk.initialize(
            application = this,
            accessToken = getString(R.string.mapbox_access_token),
            locationEngine = LocationEngineProvider.getBestLocationEngine(this)
        )*/
       
    }

}
