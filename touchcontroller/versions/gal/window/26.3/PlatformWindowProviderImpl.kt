/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.gal.window.v26_3

import net.minecraft.client.Minecraft
import org.lwjgl.sdl.SDL_Event
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.common.gal.window.PlatformWindow
import top.fifthlight.touchcontroller.common.gal.window.PlatformWindowProvider

@ActualImpl(PlatformWindowProvider::class)
object PlatformWindowProviderImpl : PlatformWindowProvider {
    @Volatile var eventHandler: ((event: SDL_Event) -> Boolean)? = null
        @JvmStatic get

    @JvmStatic
    @ActualConstructor("of")
    fun of(): PlatformWindowProvider = PlatformWindowProviderImpl

    private val inner by lazy {
        Minecraft.getInstance().window
    }

    override val windowWidth: Int
        get() = inner.screenWidth
    override val windowHeight: Int
        get() = inner.screenHeight

    override val platform: PlatformWindow<*> = PlatformWindow.Sdl(this::eventHandler::set)
}
