package top.fifthlight.touchcontroller.common.control.action

import top.fifthlight.combine.core.data.Identifier
import top.fifthlight.combine.core.data.Text
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.common.gal.player.PlayerHandle
import top.fifthlight.touchcontroller.common.util.registry.SimpleRegistry

object PlayerActions {
    val registry = SimpleRegistry<PlayerActionInstanceImpl>()

    private fun SimpleRegistry<PlayerActionInstanceImpl>.register(
        id: String,
        hidden: Boolean = false,
        name: Identifier,
        action: PlayerHandle.() -> Unit,
    ) = PlayerActionInstanceImpl(
        hidden = hidden,
        name = Text.translatable(name),
        action = action,
    ).also { instance ->
        register(id, instance)
    }

    val unknown = registry.register(
        id = "unknown",
        hidden = true,
        name = Texts.WIDGET_TRIGGER_PLAYER_ACTION_UNKNOWN,
        action = {},
    )

    val cancelFlying = registry.register(
        id = "cancel_flying",
        name = Texts.WIDGET_TRIGGER_PLAYER_ACTION_CANCEL_FLYING,
    ) {
        isFlying = false
    }

    val startSprint = registry.register(
        id = "start_sprint",
        name = Texts.WIDGET_TRIGGER_PLAYER_ACTION_START_SPRINT,
    ) {
        isSprinting = true
    }

    val stopSprint = registry.register(
        id = "stop_sprint",
        name = Texts.WIDGET_TRIGGER_PLAYER_ACTION_STOP_SPRINT,
    ) {
        isSprinting = false
    }
}
