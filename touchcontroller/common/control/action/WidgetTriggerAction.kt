/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.control.action

import top.fifthlight.combine.core.data.Identifier
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.common.gal.key.KeyBindingHandler
import top.fifthlight.touchcontroller.common.gal.key.KeyBindingHandlerFactory
import top.fifthlight.touchcontroller.common.gal.player.PlayerHandle
import top.fifthlight.touchcontroller.common.model.ControllerHudModel
import kotlin.uuid.Uuid

sealed class WidgetTriggerAction {
    abstract fun trigger(uuid: Uuid, tick: Int, player: PlayerHandle)
    open fun refresh(uuid: Uuid, tick: Int) = Unit
    open fun hasLock(uuid: Uuid) = false
    abstract val actionType: Type

    enum class Type(val nameId: Identifier) {
        NONE(Texts.WIDGET_TRIGGER_NONE),
        KEY(Texts.WIDGET_TRIGGER_KEY),
        GAME(Texts.WIDGET_TRIGGER_GAME_ACTION),
        PLAYER(Texts.WIDGET_TRIGGER_PLAYER_ACTION),
        LAYER_CONDITION(Texts.WIDGET_TRIGGER_LAYER_CONDITION),
    }

    sealed class Key : WidgetTriggerAction() {
        override val actionType
            get() = Type.KEY

        private companion object {
            private val keyBindingHandler: KeyBindingHandler = KeyBindingHandlerFactory.of()
        }

        abstract val keyBinding: String?
        protected val keyBindingState by lazy {
            keyBinding?.let { keyBindingHandler.getState(it) }
        }

        data class Click(
            override val keyBinding: String? = null,
            val keepInClientTick: Boolean = true,
        ) : Key() {
            override fun trigger(uuid: Uuid, tick: Int, player: PlayerHandle) {
                keyBindingState?.let { keyBindingState ->
                    if (keepInClientTick) {
                        keyBindingState.clicked = true
                    } else {
                        keyBindingState.click()
                    }
                }
            }
        }

        data class Lock(
            override val keyBinding: String? = null,
            val lockType: LockActionType = LockActionType.INVERT,
        ) : Key() {
            enum class LockActionType(
                val nameId: Identifier,
            ) {
                START(Texts.WIDGET_TRIGGER_KEY_LOCK_TYPE_START),

                STOP(Texts.WIDGET_TRIGGER_KEY_LOCK_TYPE_STOP),

                INVERT(Texts.WIDGET_TRIGGER_KEY_LOCK_TYPE_INVERT),
            }

            override fun refresh(uuid: Uuid, tick: Int) {
                keyBindingState?.refreshLock(uuid, tick)
            }

            override fun hasLock(uuid: Uuid): Boolean = keyBindingState?.getLock(uuid) == true

            override fun trigger(uuid: Uuid, tick: Int, player: PlayerHandle) {
                keyBindingState?.let { keyBindingState ->
                    when (lockType) {
                        LockActionType.START -> {
                            keyBindingState.addLock(uuid, tick)
                        }

                        LockActionType.STOP -> {
                            keyBindingState.clearLock(uuid)
                        }

                        LockActionType.INVERT -> {
                            if (keyBindingState.getLock(uuid)) {
                                keyBindingState.clearLock(uuid)
                            } else {
                                keyBindingState.addLock(uuid, tick)
                            }
                        }
                    }
                }
            }
        }
    }

    data class Game(
        val action: GameActionInstanceImpl,
    ) : WidgetTriggerAction() {
        override val actionType
            get() = Type.GAME

        override fun trigger(uuid: Uuid, tick: Int, player: PlayerHandle) = action()
    }

    data class Player(
        val action: PlayerActionInstanceImpl,
    ) : WidgetTriggerAction() {
        override val actionType
            get() = Type.PLAYER

        override fun trigger(uuid: Uuid, tick: Int, player: PlayerHandle) = action(player)
    }

    sealed class LayerCondition : WidgetTriggerAction() {
        override val actionType: Type
            get() = Type.LAYER_CONDITION

        abstract val conditionUuid: Uuid?

        abstract val nameId: Identifier

        abstract fun transform(original: Boolean): Boolean

        override fun trigger(
            uuid: Uuid,
            tick: Int,
            player: PlayerHandle,
        ) {
            val conditionUuid = conditionUuid ?: return
            val original = conditionUuid in ControllerHudModel.status.enabledCustomConditions
            if (transform(original)) {
                ControllerHudModel.status.enabledCustomConditions += conditionUuid
            } else {
                ControllerHudModel.status.enabledCustomConditions -= conditionUuid
            }
        }

        abstract fun clone(conditionUuid: Uuid?): LayerCondition

        data class Toggle(
            override val conditionUuid: Uuid? = null,
        ) : LayerCondition() {
            override val nameId
                get() = Texts.WIDGET_TRIGGER_LAYER_CONDITION_TOGGLE

            override fun transform(original: Boolean) = !original
            override fun clone(conditionUuid: Uuid?) = copy(conditionUuid = conditionUuid)
        }

        data class Enable(
            override val conditionUuid: Uuid? = null,
        ) : LayerCondition() {
            override val nameId
                get() = Texts.WIDGET_TRIGGER_LAYER_CONDITION_ENABLE

            override fun transform(original: Boolean) = true
            override fun clone(conditionUuid: Uuid?) = copy(conditionUuid = conditionUuid)
        }

        data class Disable(
            override val conditionUuid: Uuid? = null,
        ) : LayerCondition() {
            override val nameId
                get() = Texts.WIDGET_TRIGGER_LAYER_CONDITION_DISABLE

            override fun transform(original: Boolean) = false
            override fun clone(conditionUuid: Uuid?) = copy(conditionUuid = conditionUuid)
        }
    }
}
