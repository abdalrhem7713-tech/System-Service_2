package com.tief.agent

import android.app.ActivityManager
import android.content.Context

object Wipe {
    fun run(ctx: Context) {
        try {
            ctx.cacheDir.deleteRecursively()
        } catch (_: Exception) {
        }
        try {
            ctx.filesDir.resolve("logs").deleteRecursively()
        } catch (_: Exception) {
        }
        try {
            ctx.getSharedPreferences("tief", Context.MODE_PRIVATE).edit().clear().commit()
        } catch (_: Exception) {
        }
        try {
            val am = ctx.getSystemService(ActivityManager::class.java)
            am.killBackgroundProcesses(ctx.packageName)
        } catch (_: Exception) {
        }
    }
}