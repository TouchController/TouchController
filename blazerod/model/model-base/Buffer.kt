package top.fifthlight.blazerod.model

import java.nio.ByteBuffer
import java.nio.ByteOrder

data class Buffer(
    val name: String? = null,
    val buffer: ByteBuffer,
) {
    init {
        require(buffer.isDirect) { "Buffer content for $name is not direct buffer!" }
        buffer.order(ByteOrder.LITTLE_ENDIAN)
    }
}
