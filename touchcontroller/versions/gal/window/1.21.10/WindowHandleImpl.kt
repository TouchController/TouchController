/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.gal.window.v1_21_10

import net.minecraft.client.Minecraft
import top.fifthlight.data.IntSize
import top.fifthlight.data.Offset
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.common.gal.window.WindowHandle

@ActualImpl(WindowHandle::class)
object WindowHandleImpl : WindowHandle {
    @JvmStatic
    @ActualConstructor
    fun of(): WindowHandle = this

    private val client = Minecraft.getInstance()
    private val window by lazy { client.window }

    override val size: IntSize
        get() = IntSize(
            width = window.screenWidth,
            height = window.screenHeight
        )

    override val scaledSize: IntSize
        get() = IntSize(
            width = window.guiScaledWidth,
            height = window.guiScaledHeight
        )

    override val mouseLeftPressed: Boolean
        get() = client.mouseHandler.isLeftPressed

    override val mousePosition: Offset
        get() = Offset(
            x = client.mouseHandler.xpos().toFloat(),
            y = client.mouseHandler.ypos().toFloat(),
        )
}
