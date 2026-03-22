package top.fifthlight.touchcontroller.proxy.message

import java.nio.ByteBuffer

data object ClearPointerMessage : ProxyMessage() {
    override val type: Int = 3

    object Decoder : ProxyMessageDecoder<ClearPointerMessage>() {
        override fun decode(payload: ByteBuffer): ClearPointerMessage {
            if (payload.hasRemaining()) {
                throw BadMessageLengthException(
                    expected = 0,
                    actual = payload.remaining()
                )
            }
            return ClearPointerMessage
        }
    }
}
