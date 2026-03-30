/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.event.render

import kotlinx.collections.immutable.toPersistentSet
import org.slf4j.LoggerFactory
import top.fifthlight.combine.input.text.TextInputState
import top.fifthlight.combine.input.text.TextRange
import top.fifthlight.combine.paint.Canvas
import top.fifthlight.data.IntOffset
import top.fifthlight.data.Offset
import top.fifthlight.touchcontroller.common.config.condition.input.BuiltinLayerCondition
import top.fifthlight.touchcontroller.common.config.data.StatusConfig
import top.fifthlight.touchcontroller.common.config.holder.GlobalConfigHolder
import top.fifthlight.touchcontroller.common.config.preset.LayoutPreset
import top.fifthlight.touchcontroller.common.config.preset.info.PresetControlInfo
import top.fifthlight.touchcontroller.common.event.window.WindowEvents
import top.fifthlight.touchcontroller.common.gal.gamestate.GameState
import top.fifthlight.touchcontroller.common.gal.player.PlayerHandleFactory
import top.fifthlight.touchcontroller.common.gal.gamestate.GameStateProvider
import top.fifthlight.touchcontroller.common.gal.gamestate.GameStateProviderFactory
import top.fifthlight.touchcontroller.common.gal.key.DefaultKeyBindingType
import top.fifthlight.touchcontroller.common.gal.key.KeyBindingHandler
import top.fifthlight.touchcontroller.common.gal.key.KeyBindingHandlerFactory
import top.fifthlight.touchcontroller.common.gal.view.CrosshairTarget
import top.fifthlight.touchcontroller.common.gal.view.ViewActionProvider
import top.fifthlight.touchcontroller.common.gal.view.ViewActionProviderFactory
import top.fifthlight.touchcontroller.common.gal.window.WindowHandle
import top.fifthlight.touchcontroller.common.gal.window.WindowHandleFactory
import top.fifthlight.touchcontroller.common.offset.fixAspectRadio
import top.fifthlight.touchcontroller.common.input.InputManager
import top.fifthlight.touchcontroller.common.layout.Context
import top.fifthlight.touchcontroller.common.layout.config.GlobalContextConfig
import top.fifthlight.touchcontroller.common.layout.data.ContextInput
import top.fifthlight.touchcontroller.common.layout.queue.DrawQueue
import top.fifthlight.touchcontroller.common.layout.widget.game.Hud
import top.fifthlight.touchcontroller.common.model.ControllerHudModel
import top.fifthlight.touchcontroller.common.model.TouchStateModel
import top.fifthlight.touchcontroller.common.platform.capabilities.PlatformCapabilitiesHolder
import top.fifthlight.touchcontroller.common.platform.provider.PlatformProvider
import top.fifthlight.touchcontroller.proxy.client.PlatformCapability
import top.fifthlight.touchcontroller.proxy.message.*

object RenderEvents {
    private val logger = LoggerFactory.getLogger(RenderEvents::class.java)
    private val window: WindowHandle = WindowHandleFactory.of()
    private val keyBindingHandler: KeyBindingHandler = KeyBindingHandlerFactory.of()
    private val viewActionProvider: ViewActionProvider = ViewActionProviderFactory.of()
    private val touchStateModel: TouchStateModel = TouchStateModel()
    private var prevWidth = 0
    private var prevHeight = 0

    @JvmStatic
    fun onRenderStart() {
        ControllerHudModel.timer.renderTick()
        keyBindingHandler.renderTick(ControllerHudModel.timer.renderTick)

        if (ControllerHudModel.status.vibrate) {
            PlatformProvider.platform?.sendEvent(VibrateMessage(VibrateMessage.Kind.BLOCK_BROKEN))
            ControllerHudModel.status.vibrate = false
        }

        val config = GlobalConfigHolder.config.value
        if (config.status.status == StatusConfig.Status.DISABLED) {
            touchStateModel.clearPointer()
            val platform = PlatformProvider.platform
            if (platform != null) {
                while (true) {
                    platform.pollEvent() ?: break
                }
            }
            return
        }

        val playerHandle = PlayerHandleFactory.current()
        val platform = PlatformProvider.platform
        if (platform != null) {
            while (true) {
                val width = WindowEvents.windowWidth
                val height = WindowEvents.windowHeight
                if (width != prevWidth || height != prevHeight) {
                    prevWidth = width
                    prevHeight = height
                    platform.resize(width, height)
                }
                val message = platform.pollEvent() ?: break
                when (message) {
                    is AddPointerMessage -> {
                        touchStateModel.addPointer(
                            index = message.index,
                            position = Offset(
                                x = message.x,
                                y = message.y,
                            )
                        )
                    }

                    is RemovePointerMessage -> {
                        touchStateModel.removePointer(message.index)
                    }

                    ClearPointerMessage -> touchStateModel.clearPointer()

                    is CapabilityMessage -> {
                        PlatformCapability.entries.firstOrNull { it.id == message.capability }?.let {
                            logger.info("TouchController capability ${message.capability} set to ${message.enabled}")
                            if (message.enabled) {
                                PlatformCapabilitiesHolder.addCapability(it)
                            } else {
                                PlatformCapabilitiesHolder.removeCapability(it)
                            }
                        } ?: run {
                            logger.warn("Unknown capability: ${message.capability}, maybe you should update TouchController?")
                        }
                    }

                    is InputStatusMessage -> {
                        message.status?.let { status ->
                            InputManager.updateNativeState(
                                TextInputState(
                                    text = status.text,
                                    composition = TextRange(status.composition.start, status.composition.length),
                                    selection = TextRange(status.selection.start, status.selection.length),
                                    selectionLeft = status.selectionLeft,
                                )
                            )
                        }
                    }

                    is MoveViewMessage -> {
                        playerHandle?.let {
                            val rawDelta = Offset(
                                x = message.deltaYaw,
                                y = message.deltaPitch,
                            )
                            val offset = if (message.screenBased) {
                                rawDelta.fixAspectRadio(window.size) * config.control.viewMovementSensitivity
                            } else {
                                rawDelta
                            }
                            playerHandle.changeLookDirection(
                                deltaYaw = offset.x.toDouble(),
                                deltaPitch = offset.y.toDouble(),
                            )
                        }
                    }

                    else -> {}
                }
            }
        }

        if (config.debug.enableTouchEmulation) {
            val mousePosition = window.mousePosition
            if (window.mouseLeftPressed && mousePosition != null) {
                touchStateModel.addPointer(
                    index = 0,
                    position = mousePosition / window.size.toSize()
                )
            } else {
                touchStateModel.clearPointer()
            }
        }

        if (!GameState.inGame) {
            return
        }
        if (GameState.inGui) {
            touchStateModel.clearPointer()
        }
        val player = playerHandle ?: return
        if (player.isFlying || player.isSubmergedInWater) {
            keyBindingHandler.getState(DefaultKeyBindingType.SNEAK).clearLock()
        }

        val preset = when (config.status.status) {
            StatusConfig.Status.ENABLED -> GlobalConfigHolder.currentPreset.value
            StatusConfig.Status.ONLY_VIEW_CLICK_TO_INTERACT, StatusConfig.Status.DISABLED -> LayoutPreset()
            StatusConfig.Status.ONLY_VIEW_AIMING_BY_CROSSHAIR -> LayoutPreset(
                controlInfo = PresetControlInfo(
                    splitControls = true,
                    disableCrosshair = false,
                )
            )
        }
        val currentPresetUuid = GlobalConfigHolder.currentPresetUuid.value
        if (ControllerHudModel.status.previousPresetUuid != currentPresetUuid) {
            ControllerHudModel.status.previousPresetUuid = currentPresetUuid
            ControllerHudModel.status.enabledCustomConditions.clear()
        }
        val ridingType = player.ridingEntityType
        val crosshairTarget = viewActionProvider.getCrosshairTarget()
        val condition = buildSet {
            fun put(key: BuiltinLayerCondition, condition: Boolean) {
                if (condition) {
                    add(key)
                }
            }
            put(BuiltinLayerCondition.FLYING, player.isFlying)
            put(BuiltinLayerCondition.CAN_FLY, player.canFly)
            put(BuiltinLayerCondition.SWIMMING, player.isTouchingWater)
            put(BuiltinLayerCondition.UNDERWATER, player.isSubmergedInWater)
            put(BuiltinLayerCondition.SPRINTING, player.isSprinting)
            put(BuiltinLayerCondition.SNEAKING, player.isSneaking)
            put(BuiltinLayerCondition.ON_GROUND, player.onGround)
            put(BuiltinLayerCondition.NOT_ON_GROUND, !player.onGround)
            put(BuiltinLayerCondition.USING_ITEM, player.isUsingItem)
            put(BuiltinLayerCondition.RIDING, ridingType != null)
            put(BuiltinLayerCondition.ENTITY_SELECTED, crosshairTarget is CrosshairTarget.Entity)
            put(BuiltinLayerCondition.BLOCK_SELECTED, crosshairTarget == CrosshairTarget.Block)
        }.toPersistentSet()

        val drawQueue = DrawQueue()
        val result = Context(
            windowSize = window.size,
            windowScaledSize = window.scaledSize,
            drawQueue = drawQueue,
            size = window.scaledSize,
            screenOffset = IntOffset.ZERO,
            pointers = touchStateModel.pointers,
            input = ContextInput(
                inGui = GameState.inGui,
                builtinCondition = condition,
                customCondition = ControllerHudModel.status.enabledCustomConditions.toPersistentSet(),
                crosshairTarget = crosshairTarget,
                ridingEntity = ridingType,
                perspective = GameState.perspective,
                playerHandle = playerHandle,
            ),
            status = ControllerHudModel.status,
            timer = ControllerHudModel.timer,
            keyBindingHandler = keyBindingHandler,
            config = GlobalContextConfig(GlobalConfigHolder.config.value),
            presetControlInfo = preset.controlInfo,
        ).run {
            Hud(layers = preset.layout)
            result
        }
        ControllerHudModel.result = result
        ControllerHudModel.pendingDrawQueue = drawQueue

        val status = ControllerHudModel.status
        status.doubleClickCounter.clean(ControllerHudModel.timer.clientTick)
        result.pendingAction.forEach { it.invoke(ControllerHudModel.timer, player) }
        result.lookDirection?.let { (x, y) ->
            player.changeLookDirection(x.toDouble(), y.toDouble())
        }
        result.inventory.slots.forEachIndexed { index, slot ->
            if (slot.select) {
                player.currentSelectedSlot = index
            }
            if (slot.drop) {
                val stack = player.getInventorySlot(index)
                if (stack.isEmpty) {
                    player.currentSelectedSlot = index
                } else {
                    player.dropSlot(index)
                }
            }
        }
    }

    fun onHudRender(canvas: Canvas) {
        val queue = ControllerHudModel.pendingDrawQueue
        queue?.let {
            queue.execute(canvas)
            ControllerHudModel.pendingDrawQueue = null
        }
    }

    fun shouldRenderCrosshair(): Boolean {
        val config = GlobalConfigHolder.config.value
        if (config.status.status == StatusConfig.Status.ONLY_VIEW_AIMING_BY_CROSSHAIR) {
            return true
        }
        val preset = GlobalConfigHolder.currentPreset.value
        if (!preset.controlInfo.disableCrosshair) {
            return true
        }
        val player = PlayerHandleFactory.current() ?: return false
        return player.hasItemsOnHand(config.item.showCrosshairItems)
    }
}
