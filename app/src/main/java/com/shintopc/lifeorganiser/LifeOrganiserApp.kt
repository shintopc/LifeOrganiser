package com.shintopc.lifeorganiser

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LifeOrganiserApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        // Initialize Notification Channel
        com.shintopc.lifeorganiser.util.NotificationHelper.createNotificationChannel(this)
    }

    companion object {
        lateinit var instance: LifeOrganiserApp
            private set
    }
}
