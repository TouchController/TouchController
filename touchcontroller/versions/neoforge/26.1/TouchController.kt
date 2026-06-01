/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.neoforge.v1_21_11

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.neoforged.bus.api.IEventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent
import net.neoforged.neoforge.client.event.ClientTickEvent
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent
import net.neoforged.neoforge.client.gui.IConfigScreenFactory
import net.neoforged.neoforge.client.gui.VanillaGuiLayers
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.level.BlockEvent
import org.slf4j.LoggerFactory
import top.fifthlight.combine.backend.minecraft.render.v26_1.CanvasImpl
import top.fifthlight.touchcontroller.common.config.data.StatusConfig
import top.fifthlight.touchcontroller.common.config.holder.GlobalConfigHolder
import top.fifthlight.touchcontroller.common.event.block.BlockBreakEvents
import top.fifthlight.touchcontroller.common.event.connection.ConnectionEvents
import top.fifthlight.touchcontroller.common.event.render.RenderEvents
import top.fifthlight.touchcontroller.common.event.tick.TickEvents
import top.fifthlight.touchcontroller.common.event.window.WindowEvents
import top.fifthlight.touchcontroller.common.model.ControllerHudModel
import top.fifthlight.touchcontroller.common.model.TouchControllerLoadStatus
import top.fifthlight.touchcontroller.common.ui.config.screen.getConfigScreen
import top.fifthlight.touchcontroller.gal.gameconfig.v26_1.GameConfigEditorImpl

@Mod("touchcontroller_26_1")
class TouchController(modEventBus: IEventBus, private val container: ModContainer) {
    private val logger = LoggerFactory.getLogger(TouchController::class.java)

    init {
        modEventBus.addListener(::onClientSetup)
    }

    private fun onClientSetup(event: FMLClientSetupEvent) {
        logger.info("Loading TouchController…")

        initialize()

        TouchControllerLoadStatus.isLoaded = true
    }

    private fun initialize() {
        val client = Minecraft.getInstance()

        container.registerExtensionPoint(IConfigScreenFactory::class.java, IConfigScreenFactory { _, parent ->
            getConfigScreen(parent) as Screen
        })

        client.execute {
            GlobalConfigHolder.load()
            WindowEvents.onWindowCreated()
            GameConfigEditorImpl.executePendingCallback()
        }

        NeoForge.EVENT_BUS.register(object {
            @SubscribeEvent
            fun hudRender(event: RenderGuiLayerEvent.Pre) {
                if (event.name != VanillaGuiLayers.BOSS_OVERLAY) {
                    return
                }
                val client = Minecraft.getInstance()
                if (!client.options.hideGui) {
                    val canvas = CanvasImpl(event.guiGraphics)
                    RenderEvents.onHudRender(canvas)
                }
            }

            @SubscribeEvent
            fun blockOutlineEvent(event: ExtractBlockOutlineRenderStateEvent) {
                event.isCanceled =
                    GlobalConfigHolder.config.value.status.status != StatusConfig.Status.DISABLED && !ControllerHudModel.result.showBlockOutline
            }

            @SubscribeEvent
            fun clientTick(event: ClientTickEvent.Post) {
                TickEvents.clientTick()
            }

            @SubscribeEvent
            fun joinWorld(event: ClientPlayerNetworkEvent.LoggingIn) {
                ConnectionEvents.onJoinedWorld()
            }

            @SubscribeEvent
            fun blockBroken(event: BlockEvent.BreakEvent) {
                BlockBreakEvents.afterBlockBreak()
            }
        })
    }
}
