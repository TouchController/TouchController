/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.platform.wayland

import org.slf4j.LoggerFactory
import top.fifthlight.combine.core.data.Text
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.common.gal.window.NativeWindow
import top.fifthlight.touchcontroller.common.platform.Platform
import top.fifthlight.touchcontroller.proxy.message.MessageDecodeException
import top.fifthlight.touchcontroller.proxy.message.ProxyMessage
import java.nio.ByteBuffer

class WaylandPlatform(window: NativeWindow.Wayland) : Platform {
    private val logger = LoggerFactory.getLogger(WaylandPlatform::class.java)

    init {
        Interface.init(window.displayPointer, window.surfacePointer)
    }

    override val name: Text
        get() = Text.translatable(Texts.PLATFORM_WAYLAND)

    override val useDefaultInputHandler: Boolean
        get() = true

    override fun resize(width: Int, height: Int) = Interface.resize(width, height)

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
