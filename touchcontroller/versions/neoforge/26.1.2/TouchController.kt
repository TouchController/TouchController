/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.neoforge.v26_1_2

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.resources.Identifier
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.client.event.*
import net.neoforged.neoforge.client.event.lifecycle.ClientStartedEvent
import net.neoforged.neoforge.client.event.lifecycle.ClientStoppingEvent
import net.neoforged.neoforge.client.gui.IConfigScreenFactory
import net.neoforged.neoforge.client.gui.VanillaGuiLayers
import net.neoforged.neoforge.client.settings.KeyModifier
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.level.block.BreakBlockEvent
import org.slf4j.LoggerFactory
import top.fifthlight.combine.backend.minecraft.render.v26_1.CanvasImpl
import top.fifthlight.touchcontroller.buildinfo.BuildInfo
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
import top.fifthlight.touchcontroller.gal.gameconfig.v26_1.GameConfigEditorImpl
import top.fifthlight.touchcontroller.gal.key.v26_1.KeyBindingStateImpl

@Mod("touchcontroller_26_1_2_neoforge", dist = [Dist.CLIENT])
@EventBusSubscriber(modid = "touchcontroller_26_1_2_neoforge", value = [Dist.CLIENT])
class TouchController(modEventBus: IEventBus, private val container: ModContainer) {
    private val logger = LoggerFactory.getLogger(TouchController::class.java)

    init {
        modEventBus.addListener(::onClientSetup)
        modEventBus.addListener(::onRegisterHudHandler)
    }

    private fun onClientSetup(event: FMLClientSetupEvent) {
        logger.info("Loading TouchController…")

        initialize()

        TouchControllerLoadStatus.isLoaded = true
    }

    private fun onRegisterHudHandler(event: RegisterGuiLayersEvent) {
        event.registerAbove(
            VanillaGuiLayers.BOSS_OVERLAY,
            Identifier.fromNamespaceAndPath(BuildInfo.MOD_ID, "hud")
        ) { guiGraphics, _ ->
            val client = Minecraft.getInstance()
            if (!client.options.hideGui) {
                val canvas = CanvasImpl(guiGraphics)
                RenderEvents.onHudRender(canvas)
            }
        }
    }

    private fun initialize() {
        container.registerExtensionPoint(IConfigScreenFactory::class.java, IConfigScreenFactory { _, parent ->
            getConfigScreen(parent) as Screen
        })

        PlatformProvider.loadNative()

        KeyEvents.addClickHandler { state ->
            val keyBinding = state as KeyBindingStateImpl
            val vanillaBinding = keyBinding.keyBinding

            // Many mods compare the modifier with KeyMapping, so we must emulate them by hacky mixin.
            try {
                currentModifier = vanillaBinding.keyModifier
                @Suppress("UnstableApiUsage")
                NeoForge.EVENT_BUS.post(InputEvent.Key(KeyEvent(vanillaBinding.key.value, 0, 0), 0))
            } finally {
                currentModifier = null
            }
        }
    }

    companion object {
        @JvmStatic
        var currentModifier: KeyModifier? = null

        @JvmStatic
        @SubscribeEvent
        private fun onClientStarted(event: ClientStartedEvent) {
            GlobalConfigHolder.load()
            WindowEvents.loadPlatformWindow()
            GameConfigEditorImpl.executePendingCallback()
        }

        @JvmStatic
        @SubscribeEvent
        private fun blockOutlineEvent(event: ExtractBlockOutlineRenderStateEvent) {
            event.isCanceled =
                GlobalConfigHolder.config.value.status.status != StatusConfig.Status.DISABLED && !ControllerHudModel.result.showBlockOutline
        }

        @JvmStatic
        @SubscribeEvent
        private fun clientTick(event: ClientTickEvent.Post) {
            TickEvents.clientTick()
        }

        @JvmStatic
        @SubscribeEvent
        private fun joinWorld(event: ClientPlayerNetworkEvent.LoggingIn) {
            ConnectionEvents.onJoinedWorld()
        }

        @JvmStatic
        @SubscribeEvent
        private fun blockBroken(event: BreakBlockEvent) {
            BlockBreakEvents.afterBlockBreak()
        }

        @JvmStatic
        @SubscribeEvent
        private fun clientClose(event: ClientStoppingEvent) {
            PlatformProvider.platform?.close()
        }
    }
}
