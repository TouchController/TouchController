/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.control.builtin

import top.fifthlight.combine.data.Identifier
import top.fifthlight.data.IntOffset
import top.fifthlight.data.IntPadding
import top.fifthlight.touchcontroller.assets.EmptyTexture
import top.fifthlight.touchcontroller.assets.Texts
import top.fifthlight.touchcontroller.assets.TextureSet
import top.fifthlight.touchcontroller.common.control.ControllerWidget
import top.fifthlight.touchcontroller.common.control.action.ButtonTrigger
import top.fifthlight.touchcontroller.common.control.action.GameActions
import top.fifthlight.touchcontroller.common.control.action.WidgetTriggerAction
import top.fifthlight.touchcontroller.common.control.property.TextureCoordinate
import top.fifthlight.touchcontroller.common.control.widget.custom.ButtonActiveTexture
import top.fifthlight.touchcontroller.common.control.widget.custom.ButtonTexture
import top.fifthlight.touchcontroller.common.control.widget.custom.CustomWidget
import top.fifthlight.touchcontroller.common.control.widget.dpad.DPad
import top.fifthlight.touchcontroller.common.control.widget.dpad.DPadExtraButton
import top.fifthlight.touchcontroller.common.gal.key.DefaultKeyBindingType
import top.fifthlight.touchcontroller.common.gal.key.KeyBindingHandler
import top.fifthlight.touchcontroller.common.gal.key.KeyBindingHandlerFactory
import top.fifthlight.touchcontroller.common.layout.align.Align
import java.util.concurrent.ConcurrentHashMap

@ConsistentCopyVisibility
data class BuiltinWidgets private constructor(
    private val textureSet: TextureSet.TextureSetKey,
) {
    companion object {
        private val keyBindingHandler: KeyBindingHandler = KeyBindingHandlerFactory.of()

        private val cache = ConcurrentHashMap<TextureSet.TextureSetKey, BuiltinWidgets>()
        operator fun get(textureSet: TextureSet.TextureSetKey): BuiltinWidgets =
            cache.computeIfAbsent(textureSet, ::BuiltinWidgets)
    }

    private fun coordinate(key: TextureSet.TextureKey) = TextureCoordinate(
        textureSet = textureSet,
        textureItem = key,
    )

    private fun fixed(
        key: TextureSet.TextureKey,
        scale: Float = 2f,
    ) = ButtonTexture.Fixed(
        texture = coordinate(key),
        scale = scale,
    )

    private fun key(type: DefaultKeyBindingType) = keyBindingHandler.mapDefaultType(type)

    private val classic: Boolean = textureSet == TextureSet.TextureSetKey.CLASSIC

    private fun customWidget(
        texture: ButtonTexture,
        activeTexture: ButtonTexture?,
        grayOnClassic: Boolean,
        swipeTrigger: Boolean = false,
        grabTrigger: Boolean = false,
        moveView: Boolean = false,
        action: ButtonTrigger = ButtonTrigger(),
        name: Identifier,
        align: Align,
        offset: IntOffset = IntOffset.ZERO,
    ) = CustomWidget(
        normalTexture = texture,
        activeTexture = if (grayOnClassic && classic) {
            ButtonActiveTexture.Gray
        } else {
            activeTexture?.let(ButtonActiveTexture::Texture) ?: ButtonActiveTexture.Same
        },
        swipeTrigger = swipeTrigger,
        grabTrigger = grabTrigger,
        moveView = moveView,
        action = action,
        name = ControllerWidget.Name.Translatable(name),
        align = align,
        offset = offset,
    )

    private fun dpadButtonInfo(
        texture: TextureCoordinate,
        activeTexture: TextureCoordinate?,
        grayOnClassic: Boolean,
    ) = DPadExtraButton.ButtonInfo(
        texture = texture,
        activeTexture = if (grayOnClassic && classic) {
            DPadExtraButton.ActiveTexture.Gray
        } else {
            activeTexture?.let(DPadExtraButton.ActiveTexture::Texture) ?: DPadExtraButton.ActiveTexture.Same
        },
    )

    val jump = customWidget(
        texture = fixed(TextureSet.TextureKey.Jump),
        activeTexture = fixed(TextureSet.TextureKey.JumpActive),
        grayOnClassic = true,
        swipeTrigger = true,
        action = ButtonTrigger(
            press = key(DefaultKeyBindingType.JUMP),
        ),
        name = Texts.WIDGET_JUMP_BUTTON_NAME,
        align = Align.RIGHT_BOTTOM,
    )

    val dpadJumpButton = DPadExtraButton.SwipeLocking(
        press = key(DefaultKeyBindingType.JUMP),
        info = dpadButtonInfo(
            texture = coordinate(TextureSet.TextureKey.Jump),
            activeTexture = coordinate(TextureSet.TextureKey.JumpActive),
            grayOnClassic = true,
        ),
    )

    val jumpHorse = customWidget(
        texture = fixed(TextureSet.TextureKey.JumpHorse),
        activeTexture = fixed(TextureSet.TextureKey.JumpHorseActive),
        grayOnClassic = true,
        swipeTrigger = false,
        action = ButtonTrigger(
            press = key(DefaultKeyBindingType.JUMP),
        ),
        name = Texts.WIDGET_JUMP_BUTTON_NAME,
        align = Align.RIGHT_BOTTOM,
    )

    val dpadJumpButtonWithoutLocking = DPadExtraButton.Swipe(
        trigger = ButtonTrigger(
            press = key(DefaultKeyBindingType.JUMP),
        ),
        info = dpadButtonInfo(
            texture = coordinate(TextureSet.TextureKey.Jump),
            activeTexture = coordinate(TextureSet.TextureKey.JumpActive),
            grayOnClassic = true,
        ),
    )

    private val sneakTrigger = if (classic) {
        ButtonTrigger(
            doubleClick = ButtonTrigger.DoubleClickTrigger(
                action = WidgetTriggerAction.Key.Lock(
                    keyBinding = key(DefaultKeyBindingType.SNEAK),
                )
            )
        )
    } else {
        ButtonTrigger(
            down = WidgetTriggerAction.Key.Lock(
                keyBinding = key(DefaultKeyBindingType.SNEAK),
            )
        )
    }

    val dpad = DPad.create(
        textureSet = textureSet,
        extraButton = DPadExtraButton.None,
    )

    val sneak = customWidget(
        texture = fixed(TextureSet.TextureKey.Sneak),
        activeTexture = fixed(TextureSet.TextureKey.SneakActive),
        grayOnClassic = false,
        swipeTrigger = false,
        action = sneakTrigger,
        name = Texts.WIDGET_SNEAK_BUTTON_NAME,
        align = Align.RIGHT_BOTTOM,
    )

    val dpadSneakButton = DPadExtraButton.Normal(
        trigger = sneakTrigger,
        info = dpadButtonInfo(
            texture = coordinate(TextureSet.TextureKey.Sneak),
            activeTexture = coordinate(TextureSet.TextureKey.SneakActive),
            grayOnClassic = false,
        ),
    )

    val forward = customWidget(
        texture = fixed(TextureSet.TextureKey.Up),
        activeTexture = fixed(TextureSet.TextureKey.UpActive),
        grayOnClassic = true,
        swipeTrigger = false,
        action = ButtonTrigger(
            press = key(DefaultKeyBindingType.UP),
        ),
        name = Texts.WIDGET_SNEAK_BUTTON_NAME,
        align = Align.RIGHT_BOTTOM,
    )

    private val dismountTrigger = if (classic) {
        ButtonTrigger(
            doubleClick = ButtonTrigger.DoubleClickTrigger(
                action = WidgetTriggerAction.Key.Click(
                    keyBinding = key(DefaultKeyBindingType.SNEAK),
                    keepInClientTick = true,
                )
            )
        )
    } else {
        ButtonTrigger(
            down = WidgetTriggerAction.Key.Click(
                keyBinding = key(DefaultKeyBindingType.SNEAK),
                keepInClientTick = true,
            )
        )
    }

    val dismount = customWidget(
        texture = fixed(TextureSet.TextureKey.SneakHorse),
        activeTexture = fixed(TextureSet.TextureKey.SneakHorseActive),
        grayOnClassic = true,
        swipeTrigger = false,
        action = dismountTrigger,
        name = Texts.WIDGET_SNEAK_BUTTON_NAME,
        align = Align.RIGHT_BOTTOM,
    )

    val dpadDismountButton = DPadExtraButton.Normal(
        trigger = dismountTrigger,
        info = dpadButtonInfo(
            texture = coordinate(TextureSet.TextureKey.Sneak),
            activeTexture = coordinate(TextureSet.TextureKey.SneakActive),
            grayOnClassic = false,
        ),
    )

    val ascendFlying = customWidget(
        texture = fixed(TextureSet.TextureKey.Ascend),
        activeTexture = fixed(TextureSet.TextureKey.AscendActive),
        grayOnClassic = true,
        swipeTrigger = true,
        action = ButtonTrigger(
            press = key(DefaultKeyBindingType.JUMP),
        ),
        name = Texts.WIDGET_ASCEND_BUTTON_NAME,
        align = Align.RIGHT_BOTTOM,
    )

    val descendFlying = customWidget(
        texture = fixed(TextureSet.TextureKey.Descend),
        activeTexture = fixed(TextureSet.TextureKey.DescendActive),
        grayOnClassic = true,
        swipeTrigger = true,
        action = ButtonTrigger(
            press = key(DefaultKeyBindingType.SNEAK),
        ),
        name = Texts.WIDGET_DESCEND_BUTTON_NAME,
        align = Align.RIGHT_BOTTOM,
    )

    val ascendSwimming = customWidget(
        texture = fixed(TextureSet.TextureKey.AscendSwimming),
        activeTexture = fixed(TextureSet.TextureKey.AscendSwimmingActive),
        grayOnClassic = true,
        swipeTrigger = true,
        action = ButtonTrigger(
            press = key(DefaultKeyBindingType.JUMP),
        ),
        name = Texts.WIDGET_ASCEND_BUTTON_NAME,
        align = Align.RIGHT_BOTTOM,
    )

    val descendSwimming = customWidget(
        texture = fixed(TextureSet.TextureKey.DescendSwimming),
        activeTexture = fixed(TextureSet.TextureKey.DescendSwimmingActive),
        grayOnClassic = true,
        swipeTrigger = true,
        action = ButtonTrigger(
            press = key(DefaultKeyBindingType.SNEAK),
        ),
        name = Texts.WIDGET_DESCEND_BUTTON_NAME,
        align = Align.RIGHT_BOTTOM,
    )

    val sprint = customWidget(
        texture = fixed(TextureSet.TextureKey.Sprint),
        activeTexture = fixed(TextureSet.TextureKey.SprintActive),
        grayOnClassic = true,
        swipeTrigger = false,
        action = ButtonTrigger(
            down = WidgetTriggerAction.Player.StartSprint,
            release = WidgetTriggerAction.Player.StopSprint,
        ),
        name = Texts.WIDGET_SPRINT_BUTTON_NAME,
        align = Align.RIGHT_BOTTOM,
    )

    val attack = customWidget(
        texture = fixed(TextureSet.TextureKey.Attack),
        activeTexture = fixed(TextureSet.TextureKey.AttackActive),
        grayOnClassic = true,
        swipeTrigger = false,
        grabTrigger = true,
        moveView = true,
        action = ButtonTrigger(
            press = key(DefaultKeyBindingType.ATTACK),
        ),
        name = Texts.WIDGET_ATTACK_BUTTON_NAME,
        align = Align.RIGHT_BOTTOM,
    )

    val use = customWidget(
        texture = fixed(TextureSet.TextureKey.Use),
        activeTexture = fixed(TextureSet.TextureKey.UseActive),
        grayOnClassic = true,
        swipeTrigger = false,
        grabTrigger = true,
        moveView = true,
        action = ButtonTrigger(
            press = key(DefaultKeyBindingType.USE),
        ),
        name = Texts.WIDGET_USE_BUTTON_NAME,
        align = Align.RIGHT_BOTTOM,
    )

    val inventory = customWidget(
        texture = fixed(TextureSet.TextureKey.Inventory, scale = 1f),
        activeTexture = fixed(TextureSet.TextureKey.InventoryActive, scale = 1f),
        grayOnClassic = true,
        swipeTrigger = false,
        action = ButtonTrigger(
            release = WidgetTriggerAction.Key.Click(
                keyBinding = key(DefaultKeyBindingType.INVENTORY),
                keepInClientTick = false,
            )
        ),
        name = Texts.WIDGET_INVENTORY_BUTTON_NAME,
        align = Align.CENTER_BOTTOM,
        offset = IntOffset(101, 0),
    )

    val chat = customWidget(
        texture = fixed(TextureSet.TextureKey.Chat, scale = 1f),
        activeTexture = fixed(TextureSet.TextureKey.ChatActive, scale = 1f),
        grayOnClassic = true,
        swipeTrigger = false,
        action = ButtonTrigger(
            down = WidgetTriggerAction.Game(GameActions.chatScreen),
        ),
        name = Texts.WIDGET_CHAT_BUTTON_NAME,
        align = Align.CENTER_TOP,
    )

    val vanillaChat = customWidget(
        texture = fixed(TextureSet.TextureKey.Chat, scale = 1f),
        activeTexture = fixed(TextureSet.TextureKey.ChatActive, scale = 1f),
        grayOnClassic = true,
        swipeTrigger = false,
        action = ButtonTrigger(
            down = WidgetTriggerAction.Game(GameActions.vanillaChatScreen),
        ),
        name = Texts.WIDGET_CHAT_BUTTON_NAME,
        align = Align.CENTER_TOP,
    )

    val pause = customWidget(
        texture = fixed(TextureSet.TextureKey.Pause, scale = 1f),
        activeTexture = fixed(TextureSet.TextureKey.PauseActive, scale = 1f),
        grayOnClassic = true,
        swipeTrigger = false,
        action = ButtonTrigger(
            down = WidgetTriggerAction.Game(GameActions.gameMenu),
        ),
        name = Texts.WIDGET_PAUSE_BUTTON_NAME,
        align = Align.CENTER_TOP,
    )

    val hideHud = customWidget(
        texture = fixed(TextureSet.TextureKey.HideHud, scale = 1f),
        activeTexture = fixed(TextureSet.TextureKey.HideHudActive, scale = 1f),
        grayOnClassic = true,
        swipeTrigger = false,
        action = ButtonTrigger(
            down = WidgetTriggerAction.Game(GameActions.hideHud),
        ),
        name = Texts.WIDGET_HIDE_HUD_BUTTON_NAME,
        align = Align.CENTER_TOP,
    )

    val switchPerspective = customWidget(
        texture = fixed(TextureSet.TextureKey.Perspective, scale = 1f),
        activeTexture = fixed(TextureSet.TextureKey.PerspectiveActive, scale = 1f),
        grayOnClassic = true,
        swipeTrigger = false,
        action = ButtonTrigger(
            down = WidgetTriggerAction.Game(GameActions.nextPerspective),
        ),
        name = Texts.WIDGET_PERSPECTIVE_SWITCH_BUTTON_NAME,
        align = Align.CENTER_TOP,
    )

    val playerList = customWidget(
        texture = fixed(TextureSet.TextureKey.PlayerList, scale = 1f),
        activeTexture = fixed(TextureSet.TextureKey.PlayerListActive, scale = 1f),
        grayOnClassic = true,
        swipeTrigger = false,
        action = ButtonTrigger(
            press = key(DefaultKeyBindingType.PLAYER_LIST),
        ),
        name = Texts.WIDGET_PLAYER_LIST_BUTTON_NAME,
        align = Align.CENTER_TOP,
    )

    val screenshot = customWidget(
        texture = fixed(TextureSet.TextureKey.Screenshot, scale = 1f),
        activeTexture = fixed(TextureSet.TextureKey.ScreenshotActive, scale = 1f),
        grayOnClassic = true,
        swipeTrigger = false,
        action = ButtonTrigger(
            down = WidgetTriggerAction.Game(GameActions.takeScreenshot),
        ),
        name = Texts.WIDGET_SCREENSHOT_BUTTON_NAME,
        align = Align.CENTER_TOP,
    )

    val custom = customWidget(
        texture = ButtonTexture.NinePatch(
            texture = EmptyTexture.EMPTY_1,
            extraPadding = IntPadding(4),
        ),
        activeTexture = ButtonTexture.NinePatch(
            texture = EmptyTexture.EMPTY_1_ACTIVE,
            extraPadding = IntPadding(4),
        ),
        grayOnClassic = true,
        name = Texts.WIDGET_CUSTOM_BUTTON_NAME,
        align = Align.CENTER_CENTER,
    )
}
