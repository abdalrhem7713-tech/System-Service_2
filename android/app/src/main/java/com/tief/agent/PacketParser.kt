package com.tief.agent

data class Flow(
    val src: String,
    val dst: String,
    val sport: Int,
    val dport: Int,
    val proto: Int,
    val ts: Long,
    val bytes: Int
)

object PacketParser {
    fun parse(buf: ByteArray, len: Int): Flow? {
        if (len < 20) return null
        if (((buf[0].toInt() and 0xF0) ushr 4) != 4) return null
        val ihl = (buf[0].toInt() and 0x0F) * 4
        if (len < ihl) return null
        val proto = buf[9].toInt() and 0xFF
        val src = "${buf[12].toInt() and 0xFF}.${buf[13].toInt() and 0xFF}.${buf[14].toInt() and 0xFF}.${buf[15].toInt() and 0xFF}"
        val dst = "${buf[16].toInt() and 0xFF}.${buf[17].toInt() and 0xFF}.${buf[18].toInt() and 0xFF}.${buf[19].toInt() and 0xFF}"
        var sport = 0
        var dport = 0
        if (proto == 6 || proto == 17) {
            if (len < ihl + 4) return Flow(src, dst, 0, 0, proto, System.currentTimeMillis(), len)
            sport = ((buf[ihl].toInt() and 0xFF) shl 8) or (buf[ihl + 1].toInt() and 0xFF)
            dport = ((buf[ihl + 2].toInt() and 0xFF) shl 8) or (buf[ihl + 3].toInt() and 0xFF)
        }
        return Flow(src, dst, sport, dport, proto, System.currentTimeMillis(), len)
    }
}