package top.fifthlight.touchcontroller.common.platform

import org.slf4j.LoggerFactory
import top.fifthlight.touchcontroller.proxy.message.LargeMessage
import top.fifthlight.touchcontroller.proxy.message.MessageDecodeException
import top.fifthlight.touchcontroller.proxy.message.ProxyMessage
import java.nio.ByteBuffer

abstract class LargeMessageWrappedPlatform: Platform {
    private val logger = LoggerFactory.getLogger(LargeMessageWrappedPlatform::class.java)
    private var largeMessageBuffer = ByteBuffer.allocate(65536)
    private val encodeBuffer = ByteBuffer.allocate(65536)
    
    protected abstract fun pollSmallEvent(): ProxyMessage?
    final override fun pollEvent(): ProxyMessage? {
        while (true) {
            val message = pollSmallEvent() ?: return null

            if (message is LargeMessage) {
                largeMessageBuffer.put(message.payload)

                if (message.end) {
                    largeMessageBuffer.flip()
                    try {
                        if (largeMessageBuffer.remaining() >= 4) {
                            val type = largeMessageBuffer!!.getInt()
                            val decoded = ProxyMessage.decode(type, largeMessageBuffer!!)
                            largeMessageBuffer.clear()
                            return decoded
                        } else {
                            logger.warn("Large message less than 4 bytes?")
                        }
                    } catch (ex: MessageDecodeException) {
                        logger.warn("Bad message: $ex")
                    }
                }
            } else {
                return message
            }
        }
    }

    protected abstract fun sendSmallEvent(message: ProxyMessage)
    final override fun sendEvent(message: ProxyMessage) {
        if (!message.wrapInLargeMessage) {
            sendSmallEvent(message)
            return
        }

        encodeBuffer.clear()
        message.encode(encodeBuffer)
        encodeBuffer.flip()

        while (encodeBuffer.hasRemaining()) {
            val length = encodeBuffer.remaining().coerceAtMost(LargeMessage.MAX_PAYLOAD_LENGTH)
            val payload = ByteArray(length)
            encodeBuffer.get(payload)
            
            sendSmallEvent(
                LargeMessage(
                    payload = payload,
                    end = !encodeBuffer.hasRemaining(),
                )
            )
        }
    }
}
