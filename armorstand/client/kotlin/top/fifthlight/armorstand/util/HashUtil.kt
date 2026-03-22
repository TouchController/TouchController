package top.fifthlight.armorstand.util

import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Path
import java.security.MessageDigest

internal fun Path.calculateSha256(): ByteArray {
    val digest = MessageDigest.getInstance("SHA-256")
    FileChannel.open(this).use { channel ->
        val buffer = ByteBuffer.allocate(256 * 1024)
        while (true) {
            buffer.clear()
            if (channel.read(buffer) == -1) {
                break
            }
            buffer.flip()
            digest.update(buffer)
        }
    }
    return digest.digest()
}
