package top.fifthlight.touchcontroller.proxy.message

import java.nio.ByteBuffer

data class AddPointerMessage(
    val index: Int,
    val x: Float,
    val y: Float,
): ProxyMessage() {
    override val type: Int = 1

    override fun encode(buffer: ByteBuffer) {
        super.encode(buffer)
        buffer.putInt(index)
        buffer.putFloat(x)
        buffer.putFloat(y)
    }

    object Decoder : ProxyMessageDecoder<AddPointerMessage>() {
        override fun decode(payload: ByteBuffer): AddPointerMessage {
            if (payload.remaining() != 12) {
                throw BadMessageLengthException(
                    expected = 12,
                    actual = payload.remaining()
                )
            }
            return AddPointerMessage(
                index = payload.getInt(),
                x = payload.getFloat(),
                y = payload.getFloat(),
            )
        }
    }
}
