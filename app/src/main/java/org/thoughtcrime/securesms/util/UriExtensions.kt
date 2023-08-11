package org.thoughtcrime.securesms.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.nio.charset.Charset

fun Uri.isContentScheme() = this.scheme == "content"


@Throws(Exception::class)
fun Uri.readBytes(context: Context): ByteArray {
    return if (this.isContentScheme()) {
        context.contentResolver.openInputStream(this)?.let {
            val len: Int = it.available()
            val buffer = ByteArray(len)
            it.read(buffer)
            it.close()
            return buffer
        } ?: throw Exception("打开文件失败\n${this}")
    } else {
        val path = RealPathUtil.getPath(context, this)
        if (path?.isNotEmpty() == true) {
            File(path).readBytes()
        } else {
            throw Exception("获取文件真实地址失败\n${this.path}")
        }
    }
}

@Throws(Exception::class)
fun Uri.writeBytes(
    context: Context,
    byteArray: ByteArray
): Boolean {
    if (this.isContentScheme()) {
        context.contentResolver.openOutputStream(this)?.let {
            it.write(byteArray)
            it.close()
            return true
        }
        return false
    } else {
        val path = RealPathUtil.getPath(context, this)
        if (path?.isNotEmpty() == true) {
            File(path).writeBytes(byteArray)
            return true
        }
    }
    return false
}

@Throws(Exception::class)
fun Uri.writeText(context: Context, text: String, charset: Charset = Charsets.UTF_8): Boolean {
    return writeBytes(context, text.toByteArray(charset))
}
