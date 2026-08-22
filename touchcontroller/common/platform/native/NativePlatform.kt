package top.fifthlight.touchcontroller.common.platform.nativebridge

import org.slf4j.LoggerFactory
import top.fifthlight.combine.core.data.Text
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.common.platform.Platform
import top.fifthlight.touchcontroller.proxy.message.MessageDecodeException
import top.fifthlight.touchcontroller.proxy.message.ProxyMessage
import java.nio.ByteBuffer

class NativePlatform(
    private val artJvmPtr: Long,
    private val artApplication: Long,
) : Platform {
    private val logger = LoggerFactory.getLogger(NativePlatform::class.java)

    override val name: Text
        get() = Text.translatable(Texts.PLATFORM_ANDROID_NATIVE)

    override val useDefaultInputHandler: Boolean
        get() = true

    override fun init() {
        Interface.init(artJvmPtr, artApplication)
    }

    private val readBuffer = ByteBuffer.allocate(65536)
    override fun pollEvent(): ProxyMessage? {
        readBuffer.clear()
        val length = Interface.poll(readBuffer.array()).takeIf { it > 0 } ?: return null
        readBuffer.limit(length)
        if (readBuffer.remaining() < 4) {
            logger.warn("Bad message length: ${readBuffer.remaining()}")
            return null
        }
        val type = readBuffer.getInt()
        return try {
            ProxyMessage.decode(type, readBuffer)
        } catch (ex: MessageDecodeException) {
            logger.warn("Bad message: $ex")
            null
        }
    }

    private val sendBuffer = ByteBuffer.allocate(65536)
    override fun sendEvent(message: ProxyMessage) {
        sendBuffer.clear()
        message.encode(sendBuffer)
        Interface.send(sendBuffer.array(), sendBuffer.position())
    }

    override fun close() {
        Interface.stop()
    }
}
