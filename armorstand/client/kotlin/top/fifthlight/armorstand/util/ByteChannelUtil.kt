package top.fifthlight.armorstand.util

import java.nio.ByteBuffer
import java.nio.channels.ReadableByteChannel
import java.nio.channels.WritableByteChannel

fun ReadableByteChannel.copyTo(target: WritableByteChannel) {
    val buffer = ByteBuffer.allocate(4096)
    var len = read(buffer)
    while (len != -1) {
        if (len == 0) {
            throw IllegalStateException("Source is a non-blocking channel")
        }
        buffer.flip()
        while (buffer.hasRemaining()) {
            target.write(buffer)
        }
        buffer.clear()
        len = read(buffer)
    }
}
