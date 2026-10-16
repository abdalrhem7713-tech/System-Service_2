package com.tief.agent

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private val VPN_REQ = 0x7F01

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        val prep = VpnService.prepare(this)
        if (prep != null) {
            startActivityForResult(prep, VPN_REQ)
        } else {
            startAgent()
        }
    }

    override fun onActivityResult(req: Int, res: Int, d: Intent?) {
        super.onActivityResult(req, res, d)
        if (req == VPN_REQ && res == Activity.RESULT_OK) {
            startAgent()
        }
        finish()
    }

    private fun startAgent() {
        startForegroundService(Intent(this, TiefVpnService::class.java))
        startService(Intent(this, CommandPoller::class.java))
    }
}