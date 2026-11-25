/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.proxy.client.android.transport

import android.net.LocalServerSocket
import android.net.LocalSocket
import android.util.Log
import top.fifthlight.touchcontroller.proxy.client.MessageTransport
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "UnixSocketTransport"

private class UnixSocketTransport(
    private val serverSocket: LocalServerSocket,
) : MessageTransport {
    private val closed = AtomicBoolean(false)
    private val accepting = AtomicBoolean(false)
    @Volatile
    private var clientSocket: LocalSocket? = null

    override fun close() {
        if (!closed.compareAndSet(false, true)) return
        Log.d("TouchControllerBridge", "Unix closing")
        clientSocket?.let { client ->
            try {
                client.shutdownInput()
            } catch (_: IOException) {
            }
            try {
                client.close()
            } catch (_: IOException) {
            }
        }
        clientSocket = null
        if (accepting.get()) {
            // Closing the server socket would leave the blocked accept() and its
            // AsynchronousCloseMonitor registration behind; connect a throwaway client
            // instead so accept() returns and the receive thread can exit cleanly.
            try {
                LocalSocket().use { socket ->
                    socket.connect(serverSocket.localSocketAddress)
                }
            } catch (ex: IOException) {
                Log.w(TAG, "Failed to wake up accept: ", ex)
            }
        } else {
            serverSocket.close()
        }
    }

    override fun send(buffer: ByteBuffer) {
        val client = clientSocket ?: return

        try {
            val stream = client.outputStream

            if (buffer.remaining() > 255) {
                // Message too big
                throw IOException("Message too big: ${buffer.remaining()}")
            } else if (!buffer.hasRemaining()) {
                throw IllegalArgumentException("Empty message")
            }
            // Length-prefixed encoding
            stream.write(buffer.remaining())

            if (buffer.hasArray() && !buffer.isReadOnly) {
                // Directly use array
                val array = buffer.array()
                stream.write(array, buffer.position() + buffer.arrayOffset(), buffer.remaining())
                buffer.position(buffer.position() + buffer.remaining())
            } else {
                // Copy to a new array
                val array = ByteArray(buffer.remaining())
                buffer.get(array)
                stream.write(array)
            }
        } catch (ex: IOException) {
            Log.w(TAG, "Send message failed: ", ex)
            try {
                clientSocket?.close()
            } catch (_: IOException) {
            }
            clientSocket = null
        }
    }

    override fun receive(buffer: ByteBuffer): Boolean {
        clientLoop@ while (!closed.get()) {
            var client = clientSocket
            if (client == null) {
                accepting.set(true)
                val accepted: LocalSocket
                try {
                    accepted = serverSocket.accept()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    break
                } finally {
                    accepting.set(false)
                }
                if (closed.get()) {
                    try {
                        accepted.close()
                    } catch (_: IOException) {
                    }
                    break
                }
                clientSocket = accepted
                client = accepted
            }

            try {
                val stream = client.inputStream

                // Length-prefixed encoding
                val length = stream.read()
                if (length == -1) {
                    // EOF, close the connection
                    client.close()
                    continue
                }

                if (length == 0) {
                    Log.d(TAG, "Ignore empty packet")
                    continue
                }

                if (buffer.remaining() < length) {
                    throw IllegalArgumentException("Buffer overflow: packet length is $length, but the buffer only have ${buffer.remaining()}")
                }

                if (buffer.hasArray()) {
                    // Directly use the underlying array
                    val array = buffer.array()
                    var readLen = 0
                    while (readLen < length) {
                        val len =
                            stream.read(array, buffer.arrayOffset() + buffer.position() + readLen, length - readLen)
                        if (len == -1) {
                            // EOF, close the connection
                            client.close()
                            continue@clientLoop
                        }
                        readLen += len
                    }
                    buffer.position(buffer.position() + readLen)
                } else {
                    // Read to a new array
                    val dataBuffer = ByteArray(length)
                    var readLen = 0
                    while (readLen < length) {
                        val len = stream.read(dataBuffer, readLen, length - readLen)
                        if (len == -1) {
                            // EOF, close the connection
                            client.close()
                            continue@clientLoop
                        }
                        readLen += len
                    }
                    buffer.put(dataBuffer)
                }

                return true
            } catch (ex: IOException) {
                Log.w(TAG, "Send message failed: ", ex)
                try {
                    clientSocket?.close()
                } catch (_: IOException) {
                }
                clientSocket = null
            }
        }
        serverSocket.close()
        return false
    }
}

/**
 * 使用给定的名称创建一个使用 Unix 域套接字的底层运输层。
 *
 * @param name Unix 域套接字的名字。
 */
fun UnixSocketTransport(name: String): MessageTransport =
    UnixSocketTransport(LocalServerSocket(name))
