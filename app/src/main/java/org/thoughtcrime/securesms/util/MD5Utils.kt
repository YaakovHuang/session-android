package org.thoughtcrime.securesms.util

import java.security.MessageDigest
import java.util.Locale

/**
 * Created by Author on 2020/4/30
 */
object MD5Utils {
    fun md5(message: String): String {
        var md5 = ""
        try {
            val md = MessageDigest.getInstance("MD5")
            val messageByte = message.toByteArray(charset("UTF-8"))
            val md5Byte = md.digest(messageByte)
            md5 = bytesToHex(md5Byte)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return md5
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexStr = StringBuffer()
        var num: Int
        for (i in bytes.indices) {
            num = bytes[i].toInt()
            if (num < 0) {
                num += 256
            }
            if (num < 16) {
                hexStr.append("0")
            }
            hexStr.append(Integer.toHexString(num))
        }
        return hexStr.toString().uppercase(Locale.getDefault())
    }
}