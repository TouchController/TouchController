/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.platform.ios

object Transport {
    @JvmStatic
    private external fun init()
    @JvmStatic
    external fun receive(buffer: ByteArray): Int
    @JvmStatic
    external fun send(buffer: ByteArray, off: Int, len: Int)

    init {
        // TODO: deal with NeoForge
        init()
    }
}
