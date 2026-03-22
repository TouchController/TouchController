package top.fifthlight.touchcontroller.common.control.action

import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import top.fifthlight.combine.data.Identifier
import top.fifthlight.touchcontroller.assets.Texts
import top.fifthlight.touchcontroller.common.control.action.provider.ChatScreenProvider
import top.fifthlight.touchcontroller.common.gal.player.PlayerHandle
import top.fifthlight.touchcontroller.common.gal.action.GameAction
import top.fifthlight.touchcontroller.common.gal.action.GameActionFactory
import top.fifthlight.touchcontroller.common.gal.key.KeyBindingHandler
import top.fifthlight.touchcontroller.common.gal.key.KeyBindingHandlerFactory
import top.fifthlight.touchcontroller.common.model.ControllerHudModel
import kotlin.uuid.Uuid

@Serializable
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

    @Serializable
    @SerialName("key")
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

        @Serializable
        @SerialName("click")
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

        @Serializable
        @SerialName("lock")
        data class Lock(
            override val keyBinding: String? = null,
            val lockType: LockActionType = LockActionType.INVERT,
        ) : Key() {
            @Serializable
            enum class LockActionType(
                val nameId: Identifier,
            ) {
                @SerialName("start")
                START(Texts.WIDGET_TRIGGER_KEY_LOCK_TYPE_START),

                @SerialName("stop")
                STOP(Texts.WIDGET_TRIGGER_KEY_LOCK_TYPE_STOP),

                @SerialName("invert")
                INVERT(Texts.WIDGET_TRIGGER_KEY_LOCK_TYPE_INVERT),
            }

            override fun refresh(uuid: Uuid, renderTick: Int) {
                keyBindingState?.refreshLock(uuid, renderTick)
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

    @Serializable
    @SerialName("game")
    sealed class Game : WidgetTriggerAction() {
        override val actionType
            get() = Type.GAME

        final override fun trigger(uuid: Uuid, tick: Int, player: PlayerHandle) = trigger(gameAction)
        abstract fun trigger(gameAction: GameAction)

        abstract val nameId: Identifier

        @Serializable
        @SerialName("vanilla_chat")
        data object VanillaChatScreen : Game() {
            override val nameId: Identifier
                get() = Texts.WIDGET_TRIGGER_GAME_ACTION_VANILLA_CHAT_SCREEN

            override fun trigger(gameAction: GameAction) {
                gameAction.openChatScreen()
            }
        }

        @Serializable
        @SerialName("chat")
        data object ChatScreen : Game() {
            override val nameId: Identifier
                get() = Texts.WIDGET_TRIGGER_GAME_ACTION_CHAT_SCREEN

            override fun trigger(gameAction: GameAction) {
                ChatScreenProvider.openChatScreen()
            }
        }

        @Serializable
        @SerialName("game_menu")
        data object GameMenu : Game() {
            override val nameId: Identifier
                get() = Texts.WIDGET_TRIGGER_GAME_ACTION_GAME_MENU

            override fun trigger(gameAction: GameAction) {
                gameAction.openGameMenu()
            }
        }

        @Serializable
        @SerialName("next_perspective")
        data object NextPerspective : Game() {
            override val nameId: Identifier
                get() = Texts.WIDGET_TRIGGER_GAME_ACTION_NEXT_PERSPECTIVE

            override fun trigger(gameAction: GameAction) {
                gameAction.nextPerspective()
            }
        }

        @Serializable
        @SerialName("take_screenshot")
        data object TakeScreenshot : Game() {
            override val nameId: Identifier
                get() = Texts.WIDGET_TRIGGER_GAME_ACTION_TAKE_SCREENSHOT

            override fun trigger(gameAction: GameAction) {
                gameAction.takeScreenshot()
            }
        }

        @Serializable
        @SerialName("take_panorama")
        data object TakePanorama : Game() {
            override val nameId: Identifier
                get() = Texts.WIDGET_TRIGGER_GAME_ACTION_TAKE_PANORAMA

            override fun trigger(gameAction: GameAction) {
                gameAction.takePanorama()
            }
        }

        @Serializable
        @SerialName("hide_hud")
        data object HideHud : Game() {
            override val nameId: Identifier
                get() = Texts.WIDGET_TRIGGER_GAME_ACTION_HIDE_HUD

            override fun trigger(gameAction: GameAction) {
                gameAction.hudHidden = !gameAction.hudHidden
            }
        }

        companion object {
            private val gameAction: GameAction = GameActionFactory.of()

            val all by lazy {
                persistentListOf(
                    VanillaChatScreen,
                    ChatScreen,
                    GameMenu,
                    NextPerspective,
                    TakeScreenshot,
                    TakePanorama,
                    HideHud,
                )
            }
        }
    }

    @Serializable
    @SerialName("player")
    sealed class Player : WidgetTriggerAction() {
        override val actionType
            get() = Type.PLAYER

        abstract val nameId: Identifier

        final override fun trigger(uuid: Uuid, tick: Int, player: PlayerHandle) = trigger(player)
        abstract fun trigger(player: PlayerHandle)

        @Serializable
        @SerialName("cancel_flying")
        data object CancelFlying : Player() {
            override val nameId: Identifier
                get() = Texts.WIDGET_TRIGGER_PLAYER_ACTION_CANCEL_FLYING

            override fun trigger(player: PlayerHandle) {
                player.isFlying = false
            }
        }

        @Serializable
        @SerialName("start_sprint")
        data object StartSprint : Player() {
            override val nameId: Identifier
                get() = Texts.WIDGET_TRIGGER_PLAYER_ACTION_START_SPRINT

            override fun trigger(player: PlayerHandle) {
                player.isSprinting = true
            }
        }

        @Serializable
        @SerialName("stop_sprint")
        data object StopSprint : Player() {
            override val nameId: Identifier
                get() = Texts.WIDGET_TRIGGER_PLAYER_ACTION_STOP_SPRINT

            override fun trigger(player: PlayerHandle) {
                player.isSprinting = false
            }
        }

        companion object {
            val all by lazy {
                persistentListOf(
                    CancelFlying,
                    StartSprint,
                    StopSprint,
                )
            }
        }
    }

    @Serializable
    @SerialName("layer_condition")
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

        @Serializable
        @SerialName("layer_toggle")
        data class Toggle(
            override val conditionUuid: Uuid? = null,
        ) : LayerCondition() {
            override val nameId
                get() = Texts.WIDGET_TRIGGER_LAYER_CONDITION_TOGGLE

            override fun transform(original: Boolean) = !original
            override fun clone(conditionUuid: Uuid?) = copy(conditionUuid = conditionUuid)
        }

        @Serializable
        @SerialName("layer_enable")
        data class Enable(
            override val conditionUuid: Uuid? = null,
        ) : LayerCondition() {
            override val nameId
                get() = Texts.WIDGET_TRIGGER_LAYER_CONDITION_ENABLE

            override fun transform(original: Boolean) = true
            override fun clone(conditionUuid: Uuid?) = copy(conditionUuid = conditionUuid)
        }

        @Serializable
        @SerialName("layer_disable")
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
