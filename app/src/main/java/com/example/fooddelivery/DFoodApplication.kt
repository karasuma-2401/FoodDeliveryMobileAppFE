package com.example.fooddelivery

import android.app.Application
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FoodDeliveryApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Khởi tạo Facebook SDK thủ công để đảm bảo
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)
    }
}
