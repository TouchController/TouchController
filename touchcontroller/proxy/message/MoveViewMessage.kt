package top.fifthlight.touchcontroller.proxy.message

import java.nio.ByteBuffer

data class MoveViewMessage(
    val screenBased: Boolean,
    val deltaPitch: Float,
    val deltaYaw: Float,
) : ProxyMessage() {
    override val type: Int = 12

    override fun encode(buffer: ByteBuffer) {
        super.encode(buffer)
        buffer.put(
            if (screenBased) {
                1
            } else {
                0
            }
        )
        buffer.putFloat(deltaPitch)
        buffer.putFloat(deltaYaw)
    }

    object Decoder : ProxyMessageDecoder<MoveViewMessage>() {
        override fun decode(payload: ByteBuffer): MoveViewMessage {
            if (payload.remaining() < 5) {
                throw BadMessageLengthException(5, payload.remaining())
            }
            val screenBased = payload.get() != 0.toByte()
            val deltaPitch = payload.getFloat()
            val deltaYaw = payload.getFloat()
            return MoveViewMessage(screenBased, deltaPitch, deltaYaw)
        }
    }
}
