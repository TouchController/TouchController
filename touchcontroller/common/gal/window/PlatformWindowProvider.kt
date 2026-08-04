/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.gal.window

import org.lwjgl.sdl.SDL_Event
import top.fifthlight.mergetools.api.ExpectFactory

sealed class NativeWindow {
    data class Win32(
        /**
         * Win32 handle
         */
        val handle: Long,
    ) : NativeWindow()

    data class Wayland(
        /**
         * Pointer to wl_display
         */
        val displayPointer: Long,
        /**
         * Pointer to wl_surface
         */
        val surfacePointer: Long,
    ) : NativeWindow()
}

sealed interface PlatformWindow<Window : NativeWindow> {
    val nativeWindow: Window?

    class Win32(nativeWindowFactory: () -> NativeWindow.Win32) : PlatformWindow<NativeWindow.Win32> {
        override val nativeWindow: NativeWindow.Win32 by lazy(nativeWindowFactory)
    }

    class Wayland(nativeWindowFactory: () -> NativeWindow.Wayland) : PlatformWindow<NativeWindow.Wayland> {
        override val nativeWindow: NativeWindow.Wayland by lazy(nativeWindowFactory)
    }

    data object Cocoa : PlatformWindow<NativeWindow> {
        override val nativeWindow: NativeWindow
            get() = error("Not yet implemented")
    }

    data object X11 : PlatformWindow<NativeWindow> {
        override val nativeWindow: NativeWindow
            get() = error("Not yet implemented")
    }

    fun interface Sdl : PlatformWindow<NativeWindow> {
        override val nativeWindow: NativeWindow?
            get() = null

        fun registerEventHandler(handler: (event: SDL_Event) -> Boolean)
    }

    data object Unknown : PlatformWindow<NativeWindow> {
        override val nativeWindow: NativeWindow
            get() = error("Unsupported platform!")
    }
}

interface PlatformWindowProvider {
    val platform: PlatformWindow<*>
    val windowWidth: Int
    val windowHeight: Int

    @ExpectFactory
    interface Factory {
        fun of(): PlatformWindowProvider
    }

    companion object : PlatformWindowProvider by PlatformWindowProviderFactory.of()
}
