/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.platform.ios

import org.slf4j.LoggerFactory
import top.fifthlight.combine.core.data.Text
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.common.platform.LargeMessageWrappedPlatform
import top.fifthlight.touchcontroller.proxy.message.MessageDecodeException
import top.fifthlight.touchcontroller.proxy.message.ProxyMessage
import java.nio.ByteBuffer

class IosPlatform(socketPath: String) : LargeMessageWrappedPlatform() {
    private val logger = LoggerFactory.getLogger(IosPlatform::class.java)

    override val name: Text
        get() = Text.translatable(Texts.PLATFORM_IOS)

    override val useDefaultInputHandler: Boolean
        get() = true

    private val handle = Transport.new(socketPath)
    // 缓冲区大小必须为 256 字节，与 AndroidPlatform.kt 一致：
    // LargeMessage 编码后最大为 4B type + 1B length + 1B end + 240B payload = 246 字节，
    // 128 字节缓冲区会触发 ios_transport_receive_core 的"缓冲区不足"分支，
    // 导致 LargeMessage 分片被截断、剩余字节永久丢失，文本输入功能损坏。
    private val readBuffer = ByteArray(256)

    override fun pollSmallEvent(): ProxyMessage? {
        val receivedLength = Transport.receive(handle, readBuffer)
        val length = receivedLength.takeIf { it > 0 } ?: return null
        val buffer = ByteBuffer.wrap(readBuffer)
        buffer.limit(length)
        if (buffer.remaining() < 4) {
            return null
        }
        val type = buffer.getInt()
        return try {
            ProxyMessage.decode(type, buffer)
        } catch (ex: MessageDecodeException) {
            logger.warn("Bad message: $ex")
            null
        }
    }

    override fun sendSmallEvent(message: ProxyMessage) {
        val buffer = ByteBuffer.allocate(256)
        message.encode(buffer)
        buffer.flip()
        Transport.send(handle, buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining())
    }
}
