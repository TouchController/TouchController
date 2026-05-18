/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.platform.sdl

import org.lwjgl.sdl.*
import org.lwjgl.sdl.SDLHaptic.SDL_InitHapticRumble
import org.lwjgl.sdl.SDLHaptic.SDL_OpenHaptic
import org.slf4j.LoggerFactory
import top.fifthlight.blazesdl.api.BlazeSDLAPI
import top.fifthlight.blazesdl.api.BlazeSDLEventHandler
import top.fifthlight.combine.core.data.Text
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.common.config.platform.PlatformConfigProvider
import top.fifthlight.touchcontroller.common.platform.Platform
import top.fifthlight.touchcontroller.proxy.message.AddPointerMessage
import top.fifthlight.touchcontroller.proxy.message.ProxyMessage
import top.fifthlight.touchcontroller.proxy.message.RemovePointerMessage
import top.fifthlight.touchcontroller.proxy.message.VibrateMessage

class BlazeSDLPlatform(api: BlazeSDLAPI) : BlazeSDLEventHandler, Platform {
    private val logger = LoggerFactory.getLogger(BlazeSDLPlatform::class.java)

    init {
        api.registerEventHandler(this)
        SDLInit.SDL_Init(SDLInit.SDL_INIT_HAPTIC)
        initHaptics()
    }

    private var haptic: Long? = null

    private fun initHaptics() {
        val haptics = SDLHaptic.SDL_GetHaptics() ?: return
        try {
            if (!haptics.hasRemaining()) {
                return
            }
            val haptic = SDL_OpenHaptic(haptics.get())
            if (haptic == 0L) {
                logger.warn("Failed to call SDL_OpenHaptic: {}", SDLError.SDL_GetError())
                return
            }
            if (!SDL_InitHapticRumble(haptic)) {
                logger.warn("Failed to call SDL_InitHapticRumble: {}", SDLError.SDL_GetError())
                SDLHaptic.SDL_CloseHaptic(haptic)
                return
            }
            this.haptic = haptic
        } finally {
            SDLStdinc.SDL_free(haptics)
        }
    }

    override fun getPriority() = 1000

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

    override fun handleEvent(event: SDL_Event): Boolean {
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
        get() = Text.translatable(Texts.PLATFORM_BLAZESDL)

    override val useDefaultInputHandler: Boolean
        get() = true

    override fun pollEvent(): ProxyMessage? = queue.removeFirstOrNull()

    override fun sendEvent(message: ProxyMessage) {
        when (message) {
            is VibrateMessage -> {
                val haptic = haptic ?: return
                val config = PlatformConfigProvider.platformConfig.value.blazesdl
                SDLHaptic.SDL_PlayHapticRumble(haptic, config.vibrationStrength, config.vibrationLength)
            }

            else -> {}
        }
    }
}
