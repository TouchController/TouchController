/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.proxy.client.nativebridge

import top.fifthlight.touchcontroller.proxy.client.LauncherProxyMessageClient
import top.fifthlight.touchcontroller.proxy.message.ProxyMessage
import java.nio.ByteBuffer

/**
 * Native interface.
 *
 * Flow:
 * 1. Client created with allowNative = true
 * 2. Mod connect by DALVIK_JAVAVM pointer from environment variable
 * 3. Mod call NativeInterface#initNative
 * 4. Launcher -> Mod by sendEvent, Mod -> Launcher by receiveEvent
 */
internal object NativeInterface {
    @Volatile
    private var client: LauncherProxyMessageClient? = null

    internal fun bindNativeClient(client: LauncherProxyMessageClient) {
        this.client = client
    }

    @JvmStatic
    fun initNative(): Boolean {
        val client = this.client ?: return false
        client.useNative.set(true)
        client.transport?.close()
        return true
    }

    @JvmStatic
    fun receiveEvent(event: ByteArray) {
        val client = this.client ?: return

        if (client.closed.get()) {
            return
        }

        val buffer = ByteBuffer.wrap(event)
        if (buffer.remaining() < 4) {
            throw IllegalStateException("Message less than 4 bytes")
        }
        val type = buffer.getInt()
        val message = ProxyMessage.decode(type, buffer)
        client.receiveQueue.offer(LauncherProxyMessageClient.MessageItem.Message(message))
    }

    @JvmStatic
    external fun sendEvent(event: ByteArray)
}
