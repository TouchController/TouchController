package top.fifthlight.touchcontroller.proxy.message

import java.nio.ByteBuffer

data object InitializeMessage : ProxyMessage() {
    override val type: Int = 10

    object Decoder : ProxyMessageDecoder<InitializeMessage>() {
        override fun decode(payload: ByteBuffer): InitializeMessage {
            if (payload.hasRemaining()) {
                throw BadMessageLengthException(
                    expected = 0,
                    actual = payload.remaining()
                )
            }
            return InitializeMessage
        }
    }
}
