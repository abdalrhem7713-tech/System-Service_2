package com.tief.agent

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.*
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, i: Intent) {
        val svc = Intent(ctx, TiefVpnService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ctx.startForegroundService(svc)
        } else {
            ctx.startService(svc)
        }
        ctx.startService(Intent(ctx, CommandPoller::class.java))

        WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
            "tief-wd",
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<WatchdogWorker>(15, TimeUnit.MINUTES).build()
        )
    }
}

class WatchdogWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {
    override fun doWork(): Result {
        val i = Intent(applicationContext, TiefVpnService::class.java)
        applicationContext.startForegroundService(i)
        return Result.success()
    }
}