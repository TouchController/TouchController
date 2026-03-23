package top.fifthlight.touchcontroller.common.control.action

import top.fifthlight.combine.data.Identifier
import top.fifthlight.combine.data.Text
import top.fifthlight.touchcontroller.assets.Texts
import top.fifthlight.touchcontroller.common.control.action.provider.ChatScreenProvider
import top.fifthlight.touchcontroller.common.gal.action.GameAction
import top.fifthlight.touchcontroller.common.util.registry.SimpleRegistry

object GameActions {
    val registry = SimpleRegistry<GameActionInstanceImpl>()

    private fun SimpleRegistry<GameActionInstanceImpl>.register(
        id: String,
        hidden: Boolean = false,
        name: Identifier,
        action: GameAction.() -> Unit
    ) = GameActionInstanceImpl(
        hidden = hidden,
        name = Text.translatable(name),
        action = { action(GameAction) },
    ).also {
        register(id, it)
    }

    val unknown = registry.register(
        id = "unknown",
        hidden = true,
        name = Texts.WIDGET_TRIGGER_GAME_ACTION_UNKNOWN,
        action = {},
    )

    val vanillaChatScreen = registry.register(
        id = "vanilla_chat",
        name = Texts.WIDGET_TRIGGER_GAME_ACTION_VANILLA_CHAT_SCREEN,
        action = GameAction::openChatScreen
    )

    val chatScreen = registry.register(
        id = "chat",
        name = Texts.WIDGET_TRIGGER_GAME_ACTION_CHAT_SCREEN
    ) {
        ChatScreenProvider.openChatScreen()
    }

    val gameMenu = registry.register(
        id = "game_menu",
        name = Texts.WIDGET_TRIGGER_GAME_ACTION_GAME_MENU,
        action = GameAction::openGameMenu,
    )

    val nextPerspective = registry.register(
        id = "next_perspective",
        name = Texts.WIDGET_TRIGGER_GAME_ACTION_NEXT_PERSPECTIVE,
        action = GameAction::nextPerspective,
    )

    val takeScreenshot = registry.register(
        id = "take_screenshot",
        name = Texts.WIDGET_TRIGGER_GAME_ACTION_TAKE_SCREENSHOT,
        action = GameAction::takeScreenshot,
    )

    val takePanorama = registry.register(
        id = "take_panorama",
        name = Texts.WIDGET_TRIGGER_GAME_ACTION_TAKE_PANORAMA,
        action = GameAction::takePanorama,
    )

    val hideHud = registry.register(
        id = "hide_hud",
        name = Texts.WIDGET_TRIGGER_GAME_ACTION_HIDE_HUD
    ) {
        GameAction.hudHidden = !GameAction.hudHidden
    }
}
