/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.platform.sdl

import org.lwjgl.sdl.SDLError
import org.lwjgl.sdl.SDLEvents
import org.lwjgl.sdl.SDLHaptic
import org.lwjgl.sdl.SDLInit
import org.lwjgl.sdl.SDLStdinc
import org.lwjgl.sdl.SDL_Event
import org.lwjgl.sdl.SDL_TouchFingerEvent
import org.slf4j.LoggerFactory
import top.fifthlight.combine.core.data.Text
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.common.config.platform.PlatformConfigProvider
import top.fifthlight.touchcontroller.common.platform.Platform
import top.fifthlight.touchcontroller.proxy.message.AddPointerMessage
import top.fifthlight.touchcontroller.proxy.message.ProxyMessage
import top.fifthlight.touchcontroller.proxy.message.RemovePointerMessage
import top.fifthlight.touchcontroller.proxy.message.VibrateMessage
import kotlin.collections.getOrPut

class SdlPlatform(private val eventRegister: (handler: (event: SDL_Event) -> Boolean) -> Unit) : Platform {
    private val logger = LoggerFactory.getLogger(SdlPlatform::class.java)
    private var haptic: Long? = null

    override fun init() {
        eventRegister(::handleEvent)
        SDLInit.SDL_InitSubSystem(SDLInit.SDL_INIT_HAPTIC)
        initHaptics()
    }

    private fun initHaptics() {
        val haptics = SDLHaptic.SDL_GetHaptics() ?: return
        try {
            if (haptics.hasRemaining()) {
                val haptic = SDLHaptic.SDL_OpenHaptic(haptics.get(0))
                if (haptic == 0L) {
                    logger.warn("Failed to call SDL_OpenHaptic: {}", SDLError.SDL_GetError())
                    return
                }
                if (!SDLHaptic.SDL_InitHapticRumble(haptic)) {
                    logger.warn("Failed to call SDL_InitHapticRumble: {}", SDLError.SDL_GetError())
                    SDLHaptic.SDL_CloseHaptic(haptic)
                    return
                }
                this.haptic = haptic
            }
        } finally {
            SDLStdinc.SDL_free(haptics)
        }
    }

    private val queue = ArrayDeque<ProxyMessage>()

    private data class PointerId(
        val touchId: Long,
        val fingerId: Long,
    ) {
        constructor(event: SDL_TouchFingerEvent) : this(
            touchId = event.touchID(),
            fingerId = event.fingerID(),
        )
    }

    private val pointerIdMap = HashMap<PointerId, Int>()
    private var nextPointerIndex = 0

    private fun handleEvent(event: SDL_Event): Boolean {
        when (event.type()) {
            SDLEvents.SDL_EVENT_FINGER_DOWN -> {
                val event = event.tfinger()
                val index = pointerIdMap.getOrPut(PointerId(event)) { nextPointerIndex++ }
                queue.addLast(
                    AddPointerMessage(
                        index = index,
                        x = event.x(),
                        y = event.y(),
                    )
                )
            }

            SDLEvents.SDL_EVENT_FINGER_UP, SDLEvents.SDL_EVENT_FINGER_CANCELED -> {
                val event = event.tfinger()
                val index = pointerIdMap.remove(PointerId(event)) ?: return true
                queue.addLast(RemovePointerMessage(index))
            }

            SDLEvents.SDL_EVENT_FINGER_MOTION -> {
                val event = event.tfinger()
                val index = pointerIdMap[PointerId(event)] ?: return true
                queue.addLast(
                    AddPointerMessage(
                        index = index,
                        x = event.x(),
                        y = event.y(),
                    )
                )
            }

            else -> return false
        }
        return true
    }

    override val name: Text
        get() = Text.translatable(Texts.PLATFORM_SDL)

    override val useDefaultInputHandler: Boolean
        get() = true

    override fun pollEvent(): ProxyMessage? = queue.removeFirstOrNull()

    override fun sendEvent(message: ProxyMessage) {
        when (message) {
            is VibrateMessage -> {
                val haptic = haptic ?: return
                val config = PlatformConfigProvider.platformConfig.value.sdl
                SDLHaptic.SDL_PlayHapticRumble(haptic, config.vibrationStrength, config.vibrationLength)
            }

            else -> {}
        }
    }

    override fun close() {
        haptic?.let { SDLHaptic.SDL_CloseHaptic(it) }
        SDLInit.SDL_QuitSubSystem(SDLInit.SDL_INIT_HAPTIC)
    }
}
