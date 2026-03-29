/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.fabric.v1_21_10

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents
import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import org.slf4j.LoggerFactory
import top.fifthlight.combine.backend.minecraft.identifier.v1_21_10.toMinecraft
import top.fifthlight.combine.backend.minecraft.render.v1_21_10.CanvasImpl
import top.fifthlight.combine.data.Identifier
import top.fifthlight.touchcontroller.api.v1.fabric.TouchControllerApiEntrypoint
import top.fifthlight.touchcontroller.buildinfo.BuildInfo
import top.fifthlight.touchcontroller.common.api.TouchControllerApiImpl
import top.fifthlight.touchcontroller.common.config.data.StatusConfig
import top.fifthlight.touchcontroller.common.config.holder.GlobalConfigHolder
import top.fifthlight.touchcontroller.common.event.block.BlockBreakEvents
import top.fifthlight.touchcontroller.common.event.connection.ConnectionEvents
import top.fifthlight.touchcontroller.common.event.key.KeyEvents
import top.fifthlight.touchcontroller.common.event.render.RenderEvents
import top.fifthlight.touchcontroller.common.event.tick.TickEvents
import top.fifthlight.touchcontroller.common.event.window.WindowEvents
import top.fifthlight.touchcontroller.common.model.ControllerHudModel
import top.fifthlight.touchcontroller.common.model.TouchControllerLoadStatus
import top.fifthlight.touchcontroller.gal.gameconfig.v1_21_10.GameConfigEditorImpl
import top.fifthlight.touchcontroller.gal.key.v1_21_10.KeyBindingStateImpl

class TouchController : ClientModInitializer {
    private val logger = LoggerFactory.getLogger(TouchController::class.java)

    companion object {
        @JvmStatic
        var isInEmulatedSetDown = false
    }

    override fun onInitializeClient() {
        logger.info("Loading TouchController…")

        callEntrypoint()
        initialize()

        TouchControllerLoadStatus.isLoaded = true
    }

    private fun callEntrypoint() {
        FabricLoader.getInstance()
            .getEntrypoints("touchcontroller-v1", TouchControllerApiEntrypoint::class.java)
            .forEach {
                try {
                    it.preTouchControllerInitialize(TouchControllerApiImpl)
                } catch (e: Exception) {
                    logger.error("Failed to call TouchControllerApiEntrypoint for ${it.javaClass.canonicalName}", e)
                }
            }
    }

    private fun initialize() {
        HudElementRegistry.attachElementBefore(
            VanillaHudElements.MISC_OVERLAYS,
            Identifier.of(BuildInfo.MOD_ID, "hud").toMinecraft()
        ) { drawContext, partialTicks ->
            val client = Minecraft.getInstance()
            if (!client.options.hideGui) {
                val canvas = CanvasImpl(drawContext)
                RenderEvents.onHudRender(canvas)
            }
        }

        KeyEvents.addHandler { state ->
            val vanillaState = state as KeyBindingStateImpl
            val vanillaKeyBinding = vanillaState.keyBinding
            if (vanillaKeyBinding.javaClass != KeyMapping::class.java) {
                isInEmulatedSetDown = true
                vanillaState.keyBinding.isDown = true
                isInEmulatedSetDown = false
            }
        }

        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register { _, _ ->
            GlobalConfigHolder.config.value.status.status == StatusConfig.Status.DISABLED || ControllerHudModel.result.showBlockOutline
        }
        ClientTickEvents.END_CLIENT_TICK.register {
            TickEvents.clientTick()
        }
        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            ConnectionEvents.onJoinedWorld()
        }
        ClientLifecycleEvents.CLIENT_STARTED.register {
            GlobalConfigHolder.load()
            WindowEvents.onWindowCreated()
            GameConfigEditorImpl.executePendingCallback()
        }
        ClientPlayerBlockBreakEvents.AFTER.register { _, _, _, _ ->
            BlockBreakEvents.afterBlockBreak()
        }
    }
}
