/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.proxy.client

import top.fifthlight.touchcontroller.proxy.client.nativebridge.NativeInterface
import top.fifthlight.touchcontroller.proxy.message.LargeMessage
import top.fifthlight.touchcontroller.proxy.message.MessageDecodeException
import top.fifthlight.touchcontroller.proxy.message.ProxyMessage
import java.nio.ByteBuffer
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 与 TouchController 交互的底层接口。
 *
 * 这个类提供了对 TouchController 底层协议的使用，可以向 TouchController 发送自定义的数据包。
 * 如果只是想使用 TouchController 的高层功能，可以使用 LauncherProxyClient。
 * 调用 send() 方法可以发送数据包，需要调用 run() 方法来运行，否则消息会累积在消息队列中不会发送。
 *
 * @param transport 使用到的消息运输层
 * @param allowNative 允许使用原生代码运输层
 */
class LauncherProxyMessageClient @JvmOverloads constructor(
    private val transport: MessageTransport?,
    private val allowNative: Boolean = false,
) : AutoCloseable {
    init {
        require(transport != null || allowNative) {
            "You need a transport or allow native"
        }
    }

    internal sealed class MessageItem {
        data class Message(val message: ProxyMessage) : MessageItem()
        data object Close : MessageItem()
    }

    internal var useNative = AtomicBoolean(false)
    internal val sendQueue = LinkedBlockingQueue<MessageItem>(2048)
    internal val receiveQueue = LinkedBlockingQueue<MessageItem>(2048)
    private var running = AtomicBoolean(false)
    internal var closed = AtomicBoolean(false)

    /**
     * 开始处理消息，会新建线程用于处理，不会阻塞当前线程。
     */
    fun run() {
        if (!running.compareAndSet(false, true)) {
            return
        }
        if (allowNative) {
            NativeInterface.bindNativeClient(this)
        }
        Thread {
            // Send thread
            val encodeBuffer = ByteBuffer.allocate(65536)
            val sendBuffer = ByteBuffer.allocate(256)
            while (true) {
                when (val item = sendQueue.take()) {
                    is MessageItem.Close -> break
                    is MessageItem.Message -> {
                        val message = item.message
                        encodeBuffer.clear()
                        message.encode(encodeBuffer)
                        encodeBuffer.flip()

                        if (useNative.get()) {
                            val sendBuffer = ByteArray(encodeBuffer.remaining())
                            encodeBuffer.get(sendBuffer)
                            NativeInterface.sendEvent(sendBuffer)
                            continue
                        }

                        val transport = transport ?: continue

                        if (message.wrapInLargeMessage) {
                            // Split message to multiple LargeMessage
                            while (encodeBuffer.hasRemaining()) {
                                val length = encodeBuffer.remaining().coerceAtMost(LargeMessage.MAX_PAYLOAD_LENGTH)
                                val payload = ByteArray(length)
                                encodeBuffer.get(payload)
                                sendBuffer.clear()
                                val wrappedMessage = LargeMessage(
                                    payload = payload,
                                    end = !encodeBuffer.hasRemaining(),
                                )
                                wrappedMessage.encode(sendBuffer)
                                sendBuffer.flip()
                                transport.send(sendBuffer)
                            }
                        } else {
                            transport.send(encodeBuffer)
                        }
                    }
                }
            }
            transport?.close()
        }.start()
        transport?.let { transport ->
            Thread {
                // Receive thread
                val receiveBuffer = ByteBuffer.allocate(256)
                val decodeBuffer = ByteBuffer.allocate(65536)
                while (transport.receive(receiveBuffer)) {
                    receiveBuffer.flip()

                    if (receiveBuffer.remaining() < 4) {
                        // Message without type
                        receiveBuffer.clear()
                        continue
                    }
                    val type = receiveBuffer.getInt()
                    val message = try {
                        ProxyMessage.decode(type, receiveBuffer)
                    } catch (_: MessageDecodeException) {
                        // Ignore bad message
                        null
                    }
                    if (message != null) {
                        if (message is LargeMessage) {
                            decodeBuffer.put(message.payload)
                            if (message.end) {
                                decodeBuffer.flip()
                                try {
                                    if (decodeBuffer.remaining() >= 4) {
                                        val wrappedType = decodeBuffer.getInt()
                                        val wrappedMessage = ProxyMessage.decode(wrappedType, decodeBuffer)
                                        receiveQueue.offer(MessageItem.Message(wrappedMessage))
                                    }
                                } catch (_: MessageDecodeException) {
                                    // Ignore bad message
                                }
                                decodeBuffer.clear()
                            }
                        } else {
                            receiveQueue.offer(MessageItem.Message(message))
                        }
                    }

                    receiveBuffer.clear()
                }
            }.start()
        }
    }

    /**
     * 发送一个数据包。这个方法将数据包放到消息队列中，会立即返回，不会产生阻塞。
     *
     * @param message 要发送的数据包
     */
    fun send(message: ProxyMessage) {
        if (!closed.get()) {
            sendQueue.offer(MessageItem.Message(message))
        }
    }

    /**
     * 接收一个数据包。这个方法会阻塞直到接收到数据包或者被关闭为止。
     *
     * @return 接收到的数据包，如果当前  LauncherProxyMessageClient 被关闭则会返回 null。
     */
    fun receive(): ProxyMessage? {
        if (closed.get()) {
            return null
        }
        return when (val item = receiveQueue.take() ?: return null) {
            is MessageItem.Message -> item.message
            MessageItem.Close -> null
        }
    }

    /**
     * 关闭这个对象。关闭后消息队列中仍有的消息依然会尝试发送，但是新消息不会再被发送。
     */
    override fun close() {
        if (closed.compareAndSet(false, true)) {
            sendQueue.offer(MessageItem.Close)
            receiveQueue.offer(MessageItem.Close)
        }
    }
}
