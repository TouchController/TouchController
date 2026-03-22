package top.fifthlight.touchcontroller.proxy.message

import java.nio.ByteBuffer

data class KeyboardShowMessage(
    val show: Boolean
) : ProxyMessage() {
    override val type: Int = 8

    override fun encode(buffer: ByteBuffer) {
        super.encode(buffer)
        if (show) {
            buffer.put(1)
        } else {
            buffer.put(0)
        }
    }

    object Decoder : ProxyMessageDecoder<KeyboardShowMessage>() {
        override fun decode(payload: ByteBuffer): KeyboardShowMessage {
            if (payload.remaining() != 1) {
                throw BadMessageLengthException(
                    expected = 1,
                    actual = payload.remaining()
                )
            }
            val show = payload.get() != 0.toByte()
            return KeyboardShowMessage(show)
        }
    }
}
