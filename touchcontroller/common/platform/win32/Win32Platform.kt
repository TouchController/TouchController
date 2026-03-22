package top.fifthlight.touchcontroller.common.platform.win32

import org.slf4j.LoggerFactory
import top.fifthlight.combine.data.Text
import top.fifthlight.touchcontroller.assets.Texts
import top.fifthlight.touchcontroller.common.gal.window.NativeWindow
import top.fifthlight.touchcontroller.common.platform.Platform
import top.fifthlight.touchcontroller.proxy.message.MessageDecodeException
import top.fifthlight.touchcontroller.proxy.message.ProxyMessage
import java.nio.ByteBuffer

class Win32Platform(window: NativeWindow.Win32) : Platform {
    private val logger = LoggerFactory.getLogger(Win32Platform::class.java)

    init {
        Interface.init(window.handle)
    }

    override val name: Text
        get() = Text.translatable(Texts.PLATFORM_WIN32)

    private val readBuffer = ByteArray(65536)
    override fun pollEvent(): ProxyMessage? {
        val length = Interface.pollEvent(readBuffer).takeIf { it != 0 } ?: return null
        val buffer = ByteBuffer.wrap(readBuffer)
        buffer.limit(length)
        if (buffer.remaining() < 4) {
            return null
        }
        val type = buffer.getInt()
        return try {
            ProxyMessage.decode(type, buffer)
        } catch (ex: MessageDecodeException) {
            logger.warn("Bad message from native side: $ex")
            null
        }
    }

    private val sendBuffer = ByteArray(65536)
    override fun sendEvent(message: ProxyMessage) {
        val buffer = ByteBuffer.wrap(sendBuffer)
        message.encode(buffer)
        Interface.pushEvent(buffer.array(), buffer.position())
    }
}
