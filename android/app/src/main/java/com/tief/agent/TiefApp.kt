package com.tief.agent

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class TiefApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel("sync", "Sync", NotificationManager.IMPORTANCE_MIN)
            ch.setShowBadge(false)
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
    }
}