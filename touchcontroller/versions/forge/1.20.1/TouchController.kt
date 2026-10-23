/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.forge.v1_20_1

import com.mojang.blaze3d.platform.InputConstants
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gui.screens.Screen
import net.minecraftforge.client.ConfigScreenHandler
import net.minecraftforge.client.event.ClientPlayerNetworkEvent
import net.minecraftforge.client.event.InputEvent
import net.minecraftforge.client.event.RenderGuiEvent
import net.minecraftforge.client.event.RenderHighlightEvent
import net.minecraftforge.client.settings.KeyModifier
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.level.BlockEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import org.slf4j.LoggerFactory
import top.fifthlight.combine.backend.minecraft.render.v1_20_1.CanvasImpl
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
import top.fifthlight.touchcontroller.common.platform.provider.PlatformProvider
import top.fifthlight.touchcontroller.common.ui.config.screen.getConfigScreen
import top.fifthlight.touchcontroller.gal.gameconfig.v1_20_1.GameConfigEditorImpl
import top.fifthlight.touchcontroller.gal.key.v1_20_1.KeyBindingStateImpl

@Mod("touchcontroller_1_20_1")
class TouchController(val fmlContext: FMLJavaModLoadingContext) {
    private val logger = LoggerFactory.getLogger(TouchController::class.java)

    init {
        fmlContext.modEventBus.addListener(::onClientSetup)
    }

    private fun onClientSetup(event: FMLClientSetupEvent) {
        logger.info("Loading TouchController…")

        initialize(event)

        TouchControllerLoadStatus.isLoaded = true
    }

    private fun initialize(event: FMLClientSetupEvent) {
        fmlContext.registerExtensionPoint(
            ConfigScreenHandler.ConfigScreenFactory::class.java
        ) {
            ConfigScreenHandler.ConfigScreenFactory { _, parent ->
                getConfigScreen(parent) as Screen
            }
        }

        PlatformProvider.loadNative()

        KeyEvents.addClickHandler { state ->
            val keyBinding = state as KeyBindingStateImpl
            val vanillaBinding = keyBinding.keyBinding

            // Many mods compare the modifier with KeyMapping, so we must emulate them by hacky mixin.
            try {
                currentModifier = vanillaBinding.keyModifier
                @Suppress("UnstableApiUsage")
                MinecraftForge.EVENT_BUS.post(InputEvent.Key(
                    vanillaBinding.key.value, 0, InputConstants.PRESS, 0,
                ))
            } finally {
                currentModifier = null
            }
        }

        event.enqueueWork {
            GlobalConfigHolder.load()
            WindowEvents.loadPlatformWindow()
            GameConfigEditorImpl.executePendingCallback()
        }

        MinecraftForge.EVENT_BUS.register(EVENT_HANDLER)
    }

    companion object {
        @JvmStatic
        var currentModifier: KeyModifier? = null

        private val EVENT_HANDLER = object {
            @SubscribeEvent
            fun hudRender(event: RenderGuiEvent.Post) {
                val canvas = CanvasImpl(event.guiGraphics)
                RenderSystem.enableBlend()
                RenderEvents.onHudRender(canvas)
            }

            @SubscribeEvent
            fun blockOutlineEvent(event: RenderHighlightEvent.Block) {
                event.isCanceled =
                    GlobalConfigHolder.config.value.status.status != StatusConfig.Status.DISABLED && !ControllerHudModel.result.showBlockOutline
            }

            @SubscribeEvent
            fun blockBroken(event: BlockEvent.BreakEvent) {
                BlockBreakEvents.afterBlockBreak()
            }

            @SubscribeEvent
            fun clientTick(event: TickEvent.ClientTickEvent) {
                if (event.phase == TickEvent.Phase.END) {
                    TickEvents.clientTick()
                }
            }

            @SubscribeEvent
            fun joinWorld(event: ClientPlayerNetworkEvent.LoggingIn) {
                ConnectionEvents.onJoinedWorld()
            }
        }

        @JvmStatic
        fun clientClose() {
            PlatformProvider.platform?.close()
        }
    }
}
