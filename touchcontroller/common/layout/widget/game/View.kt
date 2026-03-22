package top.fifthlight.touchcontroller.common.layout.widget.game

import top.fifthlight.data.Offset
import top.fifthlight.touchcontroller.common.util.uuid.fastRandomUuid
import top.fifthlight.touchcontroller.common.gal.player.PlayerHandleFactory
import top.fifthlight.touchcontroller.common.gal.key.DefaultKeyBindingType
import top.fifthlight.touchcontroller.common.gal.view.CrosshairTarget
import top.fifthlight.touchcontroller.common.gal.view.ViewActionProvider
import top.fifthlight.touchcontroller.common.gal.view.ViewActionProviderFactory
import top.fifthlight.touchcontroller.common.offset.fixAspectRadio
import top.fifthlight.touchcontroller.common.layout.Context
import top.fifthlight.touchcontroller.common.layout.data.CrosshairStatus
import top.fifthlight.touchcontroller.common.state.PointerState

private val viewUuid = fastRandomUuid()

fun Context.View() {
    val viewActionProvider: ViewActionProvider = ViewActionProviderFactory.of()

    val attackKeyState = keyBindingHandler.getState(DefaultKeyBindingType.ATTACK)
    val useKeyState = keyBindingHandler.getState(DefaultKeyBindingType.USE)

    attackKeyState.refreshLock(viewUuid, timer.renderTick)
    useKeyState.refreshLock(viewUuid, timer.renderTick)

    val crosshairTarget = viewActionProvider.getCrosshairTarget()

    var releasedView = false
    for (key in pointers.keys.toList()) {
        val state = pointers[key]!!.state
        if (state is PointerState.Released) {
            val previousState = state.previousState
            if (previousState is PointerState.View) {
                when (previousState.viewState) {
                    PointerState.View.ViewPointerState.CONSUMED -> {}
                    PointerState.View.ViewPointerState.BREAKING -> {
                        attackKeyState.clearLock(viewUuid)
                    }

                    PointerState.View.ViewPointerState.USING -> {
                        useKeyState.clearLock(viewUuid)
                    }

                    PointerState.View.ViewPointerState.INITIAL -> {
                        if (!releasedView) {
                            val pressTime = timer.clientTick - previousState.pressTime
                            // Pressed less than time threshold and not moving, recognized as short click
                            if (pressTime < config.controlConfig.viewHoldDetectTicks && !previousState.moving) {
                                when (crosshairTarget) {
                                    CrosshairTarget.Block, CrosshairTarget.Miss, null -> {
                                        // Short click on block or air: use item
                                        useKeyState.click()
                                    }

                                    is CrosshairTarget.Entity -> {
                                        // Short click on entity: attack the entity
                                        attackKeyState.click()
                                    }
                                }
                            }
                        }
                    }
                }
                releasedView = true
            }
            // Remove all released pointers, because View is the last layout
            pointers.remove(key)
        }
    }

    var currentViewPointer = pointers.values.firstOrNull {
        it.state is PointerState.View
    }

    currentViewPointer?.let { pointer ->
        val state = pointer.state as PointerState.View

        // Drop all unhandled pointers
        pointers.values.forEach {
            when (it.state) {
                PointerState.New -> it.state = PointerState.Invalid
                else -> {}
            }
        }

        var moving = state.moving
        if (!state.moving) {
            // Move detect
            val delta = (pointer.position - state.initialPosition).fixAspectRadio(windowSize).squaredLength
            val threshold = config.controlConfig.viewHoldDetectThreshold * 0.01f
            if (delta > threshold * threshold) {
                moving = true
            }
        }

        val movement = (pointer.position - state.lastPosition).fixAspectRadio(windowSize)
        result.lookDirection =
            (result.lookDirection ?: Offset.ZERO) + movement * config.controlConfig.viewMovementSensitivity

        val player = PlayerHandleFactory.current()
        // Consume the pointer if player is null or touch gesture is disabled
        if (player == null || presetControlInfo.disableTouchGesture) {
            pointer.state = state.copy(
                lastPosition = pointer.position,
                moving = moving,
                viewState = PointerState.View.ViewPointerState.CONSUMED
            )
            return@let
        }

        // Early exit for consumed pointer
        if (state.viewState == PointerState.View.ViewPointerState.CONSUMED) {
            pointer.state = state.copy(lastPosition = pointer.position, moving = moving)
            return@let
        }

        val pressTime = timer.clientTick - state.pressTime
        var viewState = state.viewState
        val crosshairTarget = viewActionProvider.getCrosshairTarget()
        val itemUsable = config.isHandItemUsable(player)

        val holdDetectTicks = if (player.instantBuild && crosshairTarget == CrosshairTarget.Block) {
            config.controlConfig.creativeBreakDetectTicks
        } else {
            config.controlConfig.viewHoldDetectTicks
        }

        // If pointer kept still and held for hold-detecting ticks in config
        if (viewState == PointerState.View.ViewPointerState.INITIAL && pressTime >= holdDetectTicks && !moving) {
            viewState = if (itemUsable) {
                // Trigger item long click
                useKeyState.addLock(viewUuid, timer.renderTick)
                PointerState.View.ViewPointerState.USING
            } else {
                when (crosshairTarget) {
                    CrosshairTarget.Block -> {
                        // Trigger block breaking
                        attackKeyState.addLock(viewUuid, timer.renderTick)
                        PointerState.View.ViewPointerState.BREAKING
                    }

                    is CrosshairTarget.Entity -> {
                        // Trigger item use once and consume
                        useKeyState.click()
                        PointerState.View.ViewPointerState.CONSUMED
                    }

                    CrosshairTarget.Miss, null -> PointerState.View.ViewPointerState.CONSUMED
                }
            }
        }

        pointer.state = state.copy(
            lastPosition = pointer.position,
            moving = moving,
            viewState = viewState
        )
    } ?: run {
        pointers.values.forEach {
            when (it.state) {
                PointerState.New -> {
                    if (currentViewPointer != null) {
                        it.state = PointerState.Invalid
                    } else {
                        it.state = PointerState.View(
                            initialPosition = it.position,
                            lastPosition = it.position,
                            pressTime = timer.clientTick,
                            viewState = PointerState.View.ViewPointerState.INITIAL
                        )
                        currentViewPointer = it
                    }
                }

                else -> {}
            }
        }
    }

    currentViewPointer?.let { pointer ->
        result.showBlockOutline = true
        // Update current view pointer
        if (!presetControlInfo.splitControls) {
            result.crosshairStatus = CrosshairStatus(
                position = pointer.position,
                breakPercent = viewActionProvider.getCurrentBreakingProgress(),
            )
        }
    } ?: run {
        if (attackKeyState.haveClickCount() || useKeyState.haveClickCount()) {
            // Keep last crosshair status for key handling
            result.crosshairStatus = status.lastCrosshairStatus
        }
    }

    if (presetControlInfo.splitControls) {
        result.showBlockOutline = true
    }

    status.lastCrosshairStatus = result.crosshairStatus
}
