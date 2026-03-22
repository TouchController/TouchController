package top.fifthlight.touchcontroller.proxy.message

import java.nio.ByteBuffer

data class LargeMessage(
    val payload: ByteArray,
    val end: Boolean,
) : ProxyMessage() {
    companion object {
        const val MAX_PAYLOAD_LENGTH = 240
        const val TYPE = 6
    }

    init {
        require(payload.size <= MAX_PAYLOAD_LENGTH) { "Payload length too large: $MAX_PAYLOAD_LENGTH" }
    }

    override val type
        get() = TYPE

    override fun encode(buffer: ByteBuffer) {
        super.encode(buffer)
        buffer.put(payload.size.toByte())
        if (end) {
            buffer.put(1)
        } else {
            buffer.put(0)
        }
        buffer.put(payload)
    }

    object Decoder : ProxyMessageDecoder<LargeMessage>() {
        override fun decode(payload: ByteBuffer): LargeMessage {
            if (payload.remaining() < 2) {
                throw BadMessageLengthException(
                    expected = 2,
                    actual = payload.remaining()
                )
            }
            val length = payload.get().toUInt().toInt()
            val end = payload.get() != 0.toByte()
            if (payload.remaining() < length) {
                throw BadMessageLengthException(
                    expected = 1 + length,
                    actual = payload.remaining()
                )
            }
            val buffer = ByteArray(length)
            payload.get(buffer)
            return LargeMessage(
                payload = buffer,
                end = end,
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LargeMessage

        if (type != other.type) return false
        if (!payload.contentEquals(other.payload)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type
        result = 31 * result + payload.contentHashCode()
        return result
    }
}
