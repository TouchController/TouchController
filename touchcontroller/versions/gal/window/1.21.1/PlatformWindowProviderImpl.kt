/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.gal.window.v1_21_1

import net.minecraft.client.Minecraft
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWNativeWayland
import org.lwjgl.glfw.GLFWNativeWin32
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.common.gal.window.PlatformWindow
import top.fifthlight.touchcontroller.common.gal.window.NativeWindow
import top.fifthlight.touchcontroller.common.gal.window.PlatformWindowProvider

@ActualImpl(PlatformWindowProvider::class)
object PlatformWindowProviderImpl : PlatformWindowProvider {
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

    override val platform: PlatformWindow<*> by lazy {
        when (GLFW.glfwGetPlatform()) {
            GLFW.GLFW_PLATFORM_WIN32 -> PlatformWindow.Win32 {
                NativeWindow.Win32(GLFWNativeWin32.glfwGetWin32Window(inner.window))
            }

            GLFW.GLFW_PLATFORM_WAYLAND -> PlatformWindow.Wayland {
                NativeWindow.Wayland(
                    displayPointer = GLFWNativeWayland.glfwGetWaylandDisplay(),
                    surfacePointer = GLFWNativeWayland.glfwGetWaylandWindow(inner.window),
                )
            }

            GLFW.GLFW_PLATFORM_COCOA -> PlatformWindow.Cocoa
            GLFW.GLFW_PLATFORM_X11 -> PlatformWindow.X11
            else -> PlatformWindow.Unknown
        }
    }
}
