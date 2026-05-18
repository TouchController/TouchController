/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.control.builtin

import top.fifthlight.combine.core.data.Identifier
import top.fifthlight.data.IntOffset
import top.fifthlight.data.IntPadding
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.assets.texture.empty.EmptyTexture
import top.fifthlight.touchcontroller.assets.texture.set.BuiltInTextureItems
import top.fifthlight.touchcontroller.assets.texture.set.BuiltInTextureSets
import top.fifthlight.touchcontroller.common.assets.TextureItem
import top.fifthlight.touchcontroller.common.assets.TextureSet
import top.fifthlight.touchcontroller.common.control.ControllerWidget
import top.fifthlight.touchcontroller.common.control.action.ButtonTrigger
import top.fifthlight.touchcontroller.common.control.action.GameActions
import top.fifthlight.touchcontroller.common.control.action.PlayerActions
import top.fifthlight.touchcontroller.common.control.action.WidgetTriggerAction
import top.fifthlight.touchcontroller.common.control.property.TextureCoordinate
import top.fifthlight.touchcontroller.common.control.widget.boat.BoatButton
import top.fifthlight.touchcontroller.common.control.widget.custom.ButtonActiveTexture
import top.fifthlight.touchcontroller.common.control.widget.custom.ButtonTexture
import top.fifthlight.touchcontroller.common.control.widget.custom.CustomWidget
import top.fifthlight.touchcontroller.common.control.widget.dpad.DPad
import top.fifthlight.touchcontroller.common.control.widget.dpad.DPadExtraButton
import top.fifthlight.touchcontroller.common.control.widget.joystick.Joystick
import top.fifthlight.touchcontroller.common.gal.key.DefaultKeyBindingType
import top.fifthlight.touchcontroller.common.gal.key.KeyBindingHandlerFactory
import top.fifthlight.touchcontroller.common.layout.align.Align
import top.fifthlight.touchcontroller.common.util.registry.SimpleRegistry

object BuiltinWidgets {
    val registry = SimpleRegistry<BuiltInWidget>()

    private fun register(
        id: String,
        hero: Boolean = false,
        hidden: (TextureSet.() -> Boolean)? = null,
        getter: TextureSet.() -> ControllerWidget,
    ) = BuiltInWidget(
        getter = getter,
        hero = hero,
        hidden = hidden,
    ).also { registry.register(id, it) }

    private fun TextureSet.coordinate(key: TextureItem) = TextureCoordinate(
        textureSet = this,
        textureItem = key,
    )

    private fun TextureSet.fixed(
        key: TextureItem,
        scale: Float = 2f,
    ) = ButtonTexture.Fixed(
        texture = coordinate(key),
        scale = scale,
    )

    private fun key(type: DefaultKeyBindingType) = KeyBindingHandlerFactory.of()
        .mapDefaultType(type)

    private fun TextureSet.customWidget(
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
        activeTexture = if (grayOnClassic && grayWhenActive) {
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

    private fun TextureSet.dpadButtonInfo(
        texture: TextureCoordinate,
        activeTexture: TextureCoordinate?,
        grayOnClassic: Boolean,
    ) = DPadExtraButton.ButtonInfo(
        texture = texture,
        activeTexture = if (grayOnClassic && grayWhenActive) {
            DPadExtraButton.ActiveTexture.Gray
        } else {
            activeTexture?.let(DPadExtraButton.ActiveTexture::Texture) ?: DPadExtraButton.ActiveTexture.Same
        },
    )

    val jump = register("jump") {
        customWidget(
            texture = fixed(BuiltInTextureItems.jump),
            activeTexture = fixed(BuiltInTextureItems.jumpActive),
            grayOnClassic = true,
            swipeTrigger = true,
            action = ButtonTrigger(
                press = key(DefaultKeyBindingType.JUMP),
            ),
            name = Texts.WIDGET_JUMP_BUTTON_NAME,
            align = Align.RIGHT_BOTTOM,
        )
    }

    fun TextureSet.dpadJumpButton() = DPadExtraButton.SwipeLocking(
        press = key(DefaultKeyBindingType.JUMP),
        info = dpadButtonInfo(
            texture = coordinate(BuiltInTextureItems.jump),
            activeTexture = coordinate(BuiltInTextureItems.jumpActive),
            grayOnClassic = true,
        ),
    )

    val jumpHorse = register(
        id = "jumpHorse",
        hidden = { this == BuiltInTextureSets.classic },
    ) {
        customWidget(
            texture = fixed(BuiltInTextureItems.jumpHorse),
            activeTexture = fixed(BuiltInTextureItems.jumpHorseActive),
            grayOnClassic = true,
            swipeTrigger = false,
            action = ButtonTrigger(
                press = key(DefaultKeyBindingType.JUMP),
            ),
            name = Texts.WIDGET_JUMP_BUTTON_NAME,
            align = Align.RIGHT_BOTTOM,
        )
    }

    fun TextureSet.dpadJumpButtonWithoutLocking() = DPadExtraButton.Swipe(
        trigger = ButtonTrigger(
            press = key(DefaultKeyBindingType.JUMP),
        ),
        info = dpadButtonInfo(
            texture = coordinate(BuiltInTextureItems.jump),
            activeTexture = coordinate(BuiltInTextureItems.jumpActive),
            grayOnClassic = true,
        ),
    )

    private fun TextureSet.sneakTrigger() = if (classic) {
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

    val dpad = register("dpad", hero = true) {
        DPad.create(
            textureSet = this,
            extraButton = DPadExtraButton.None,
        )
    }

    val joystick = register("joystick", hero = true) {
        Joystick(textureSet = this)
    }

    val sneak = register("sneak") {
        customWidget(
            texture = fixed(BuiltInTextureItems.sneak),
            activeTexture = fixed(BuiltInTextureItems.sneakActive),
            grayOnClassic = false,
            swipeTrigger = false,
            action = sneakTrigger(),
            name = Texts.WIDGET_SNEAK_BUTTON_NAME,
            align = Align.RIGHT_BOTTOM,
        )
    }

    fun TextureSet.dpadSneakButton() = DPadExtraButton.Normal(
        trigger = sneakTrigger(),
        info = dpadButtonInfo(
            texture = coordinate(BuiltInTextureItems.sneak),
            activeTexture = coordinate(BuiltInTextureItems.sneakActive),
            grayOnClassic = false,
        ),
    )

    val forward = register("forward") {
        customWidget(
            texture = fixed(BuiltInTextureItems.up),
            activeTexture = fixed(BuiltInTextureItems.upActive),
            grayOnClassic = true,
            swipeTrigger = false,
            action = ButtonTrigger(
                press = key(DefaultKeyBindingType.UP),
            ),
            name = Texts.WIDGET_FORWARD_BUTTON_NAME,
            align = Align.RIGHT_BOTTOM,
        )
    }

    private fun TextureSet.dismountTrigger() = if (classic) {
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

    val dismount = register("dismount") {
        customWidget(
            texture = fixed(BuiltInTextureItems.sneakHorse),
            activeTexture = fixed(BuiltInTextureItems.sneakHorseActive),
            grayOnClassic = true,
            swipeTrigger = false,
            action = dismountTrigger(),
            name = Texts.WIDGET_SNEAK_BUTTON_NAME,
            align = Align.RIGHT_BOTTOM,
        )
    }

    fun TextureSet.dpadDismountButton() = DPadExtraButton.Normal(
        trigger = dismountTrigger(),
        info = dpadButtonInfo(
            texture = coordinate(BuiltInTextureItems.sneak),
            activeTexture = coordinate(BuiltInTextureItems.sneakActive),
            grayOnClassic = false,
        ),
    )

    val ascendFlying = register("ascendFlying") {
        customWidget(
            texture = fixed(BuiltInTextureItems.ascend),
            activeTexture = fixed(BuiltInTextureItems.ascendActive),
            grayOnClassic = true,
            swipeTrigger = true,
            action = ButtonTrigger(
                press = key(DefaultKeyBindingType.JUMP),
            ),
            name = Texts.WIDGET_ASCEND_BUTTON_NAME,
            align = Align.RIGHT_BOTTOM,
        )
    }

    val descendFlying = register("descendFlying") {
        customWidget(
            texture = fixed(BuiltInTextureItems.descend),
            activeTexture = fixed(BuiltInTextureItems.descendActive),
            grayOnClassic = true,
            swipeTrigger = true,
            action = ButtonTrigger(
                press = key(DefaultKeyBindingType.SNEAK),
            ),
            name = Texts.WIDGET_DESCEND_BUTTON_NAME,
            align = Align.RIGHT_BOTTOM,
        )
    }

    val ascendSwimming = register(
        id = "ascendSwimming",
        hidden = { this == BuiltInTextureSets.classic },
    ) {
        customWidget(
            texture = fixed(BuiltInTextureItems.ascendSwimming),
            activeTexture = fixed(BuiltInTextureItems.ascendSwimmingActive),
            grayOnClassic = true,
            swipeTrigger = true,
            action = ButtonTrigger(
                press = key(DefaultKeyBindingType.JUMP),
            ),
            name = Texts.WIDGET_ASCEND_BUTTON_NAME,
            align = Align.RIGHT_BOTTOM,
        )
    }

    val descendSwimming = register(
        id = "descendSwimming",
        hidden = { this == BuiltInTextureSets.classic },
    ) {
        customWidget(
            texture = fixed(BuiltInTextureItems.descendSwimming),
            activeTexture = fixed(BuiltInTextureItems.descendSwimmingActive),
            grayOnClassic = true,
            swipeTrigger = true,
            action = ButtonTrigger(
                press = key(DefaultKeyBindingType.SNEAK),
            ),
            name = Texts.WIDGET_DESCEND_BUTTON_NAME,
            align = Align.RIGHT_BOTTOM,
        )
    }

    val sprint = register("sprint") {
        customWidget(
            texture = fixed(BuiltInTextureItems.sprint),
            activeTexture = fixed(BuiltInTextureItems.sprintActive),
            grayOnClassic = true,
            swipeTrigger = false,
            action = ButtonTrigger(
                down = WidgetTriggerAction.Player(PlayerActions.startSprint),
                release = WidgetTriggerAction.Player(PlayerActions.stopSprint),
            ),
            name = Texts.WIDGET_SPRINT_BUTTON_NAME,
            align = Align.RIGHT_BOTTOM,
        )
    }

    val attack = register("attack") {
        customWidget(
            texture = fixed(BuiltInTextureItems.attack),
            activeTexture = fixed(BuiltInTextureItems.attackActive),
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
    }

    val use = register("use") {
        customWidget(
            texture = fixed(BuiltInTextureItems.use),
            activeTexture = fixed(BuiltInTextureItems.useActive),
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
    }

    val boat = register("boat") {
        BoatButton(textureSet = this)
    }

    val inventory = register("inventory") {
        customWidget(
            texture = fixed(BuiltInTextureItems.inventory, scale = 1f),
            activeTexture = fixed(BuiltInTextureItems.inventoryActive, scale = 1f),
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
    }

    val chat = register("chat") {
        customWidget(
            texture = fixed(BuiltInTextureItems.chat, scale = 1f),
            activeTexture = fixed(BuiltInTextureItems.chatActive, scale = 1f),
            grayOnClassic = true,
            swipeTrigger = false,
            action = ButtonTrigger(
                down = WidgetTriggerAction.Game(GameActions.chatScreen),
            ),
            name = Texts.WIDGET_CHAT_BUTTON_NAME,
            align = Align.CENTER_TOP,
        )
    }

    val vanillaChat = register("vanillaChat") {
        customWidget(
            texture = fixed(BuiltInTextureItems.chat, scale = 1f),
            activeTexture = fixed(BuiltInTextureItems.chatActive, scale = 1f),
            grayOnClassic = true,
            swipeTrigger = false,
            action = ButtonTrigger(
                down = WidgetTriggerAction.Game(GameActions.vanillaChatScreen),
            ),
            name = Texts.WIDGET_VANILLA_CHAT_BUTTON_NAME,
            align = Align.CENTER_TOP,
        )
    }

    val pause = register("pause") {
        customWidget(
            texture = fixed(BuiltInTextureItems.pause, scale = 1f),
            activeTexture = fixed(BuiltInTextureItems.pauseActive, scale = 1f),
            grayOnClassic = true,
            swipeTrigger = false,
            action = ButtonTrigger(
                down = WidgetTriggerAction.Game(GameActions.gameMenu),
            ),
            name = Texts.WIDGET_PAUSE_BUTTON_NAME,
            align = Align.CENTER_TOP,
        )
    }

    val hideHud = register("hideHud") {
        customWidget(
            texture = fixed(BuiltInTextureItems.hideHud, scale = 1f),
            activeTexture = fixed(BuiltInTextureItems.hideHudActive, scale = 1f),
            grayOnClassic = true,
            swipeTrigger = false,
            action = ButtonTrigger(
                down = WidgetTriggerAction.Game(GameActions.hideHud),
            ),
            name = Texts.WIDGET_HIDE_HUD_BUTTON_NAME,
            align = Align.CENTER_TOP,
        )
    }

    val switchPerspective = register("switchPerspective") {
        customWidget(
            texture = fixed(BuiltInTextureItems.perspective, scale = 1f),
            activeTexture = fixed(BuiltInTextureItems.perspectiveActive, scale = 1f),
            grayOnClassic = true,
            swipeTrigger = false,
            action = ButtonTrigger(
                down = WidgetTriggerAction.Game(GameActions.nextPerspective),
            ),
            name = Texts.WIDGET_PERSPECTIVE_SWITCH_BUTTON_NAME,
            align = Align.CENTER_TOP,
        )
    }

    val playerList = register("playerList") {
        customWidget(
            texture = fixed(BuiltInTextureItems.playerList, scale = 1f),
            activeTexture = fixed(BuiltInTextureItems.playerListActive, scale = 1f),
            grayOnClassic = true,
            swipeTrigger = false,
            action = ButtonTrigger(
                press = key(DefaultKeyBindingType.PLAYER_LIST),
            ),
            name = Texts.WIDGET_PLAYER_LIST_BUTTON_NAME,
            align = Align.CENTER_TOP,
        )
    }

    val screenshot = register("screenshot") {
        customWidget(
            texture = fixed(BuiltInTextureItems.screenshot, scale = 1f),
            activeTexture = fixed(BuiltInTextureItems.screenshotActive, scale = 1f),
            grayOnClassic = true,
            swipeTrigger = false,
            action = ButtonTrigger(
                down = WidgetTriggerAction.Game(GameActions.takeScreenshot),
            ),
            name = Texts.WIDGET_SCREENSHOT_BUTTON_NAME,
            align = Align.CENTER_TOP,
        )
    }

    val custom = register("custom") {
        customWidget(
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
}
