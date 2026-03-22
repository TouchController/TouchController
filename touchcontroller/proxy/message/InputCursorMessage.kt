package top.fifthlight.touchcontroller.proxy.message

import java.nio.ByteBuffer

data class InputCursorMessage(
    val cursorRect: FloatRect? = null,
): ProxyMessage() {
    override val type: Int = 9

    override fun encode(buffer: ByteBuffer) {
        super.encode(buffer)
        if (cursorRect != null) {
            buffer.put(1)
            buffer.putFloat(cursorRect.left)
            buffer.putFloat(cursorRect.top)
            buffer.putFloat(cursorRect.width)
            buffer.putFloat(cursorRect.height)
        } else {
            buffer.put(0)
        }
    }

    object Decoder : ProxyMessageDecoder<InputCursorMessage>() {
        override fun decode(payload: ByteBuffer): InputCursorMessage {
            if (payload.remaining() < 1) {
                throw BadMessageLengthException(
                    expected = 1,
                    actual = payload.remaining()
                )
            }
            val hasData = payload.get() != 0.toByte()
            if (!hasData) {
                return InputCursorMessage(null)
            }

            if (payload.remaining() != 16) {
                throw BadMessageLengthException(
                    expected = 16,
                    actual = payload.remaining()
                )
            }
            return InputCursorMessage(
                FloatRect(
                    left = payload.getFloat(),
                    top = payload.getFloat(),
                    width = payload.getFloat(),
                    height = payload.getFloat(),
                )
            )
        }
    }
}
