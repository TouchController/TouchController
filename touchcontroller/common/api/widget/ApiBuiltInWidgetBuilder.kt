/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.api.widget

import top.fifthlight.touchcontroller.api.v1.widget.BuiltInWidgetBuilder
import top.fifthlight.touchcontroller.api.v1.widget.WidgetTexture
import top.fifthlight.touchcontroller.common.api.text.ApiText
import top.fifthlight.touchcontroller.common.api.texture.textureItem
import top.fifthlight.touchcontroller.common.api.trigger.action
import top.fifthlight.touchcontroller.common.control.ControllerWidget
import top.fifthlight.touchcontroller.common.control.action.ButtonTrigger
import top.fifthlight.touchcontroller.common.control.action.WidgetTriggerAction
import top.fifthlight.touchcontroller.common.control.builtin.BuiltinWidgets
import top.fifthlight.touchcontroller.common.control.property.TextureCoordinate
import top.fifthlight.touchcontroller.common.control.widget.custom.ButtonActiveTexture
import top.fifthlight.touchcontroller.common.control.widget.custom.ButtonTexture
import top.fifthlight.touchcontroller.common.control.widget.custom.CustomWidget
import top.fifthlight.touchcontroller.common.layout.align.Align
import top.fifthlight.touchcontroller.api.v1.text.Text as ApiTextInterface
import top.fifthlight.touchcontroller.api.v1.widget.WidgetTriggerAction as ApiWidgetTriggerAction

class ApiBuiltInWidgetBuilder : BuiltInWidgetBuilder {
    private var id: String? = null
    private var name: ApiText? = null
    private var normalTexture: WidgetTexture? = null
    private var activeTexture: WidgetTexture? = null
    private var activeGray: Boolean = false
    private var downAction: WidgetTriggerAction? = null
    private var pressKeyMapping: String? = null
    private var releaseAction: WidgetTriggerAction? = null
    private var doubleClickAction: WidgetTriggerAction? = null

    override fun id(id: String) = apply { this.id = id }

    override fun name(name: ApiTextInterface) = apply { this.name = name as ApiText }

    override fun normalTexture(texture: WidgetTexture) = apply {
        this.normalTexture = texture
    }

    override fun activeTexture(texture: WidgetTexture) = apply {
        this.activeTexture = texture
    }

    override fun activeGray() = apply {
        this.activeGray = true
    }

    override fun down(action: ApiWidgetTriggerAction) = apply {
        this.downAction = action.action
    }

    override fun press(keyMapping: String) = apply {
        this.pressKeyMapping = keyMapping
    }

    override fun release(action: ApiWidgetTriggerAction) = apply {
        this.releaseAction = action.action
    }

    override fun doubleClick(action: ApiWidgetTriggerAction) = apply {
        this.doubleClickAction = action.action
    }

    fun build() {
        val id = checkNotNull(id) { "id cannot be null" }
        val name = checkNotNull(name) { "name cannot be null" }
        val normalTexture = checkNotNull(normalTexture) { "normalTexture cannot be null" }
        val normalTextureItem = normalTexture.textureItem
        val activeTextureItem = activeTexture?.textureItem

        BuiltinWidgets.BuiltInWidget(
            getter = { textureSet ->
                val normalTexture = ButtonTexture.Fixed(
                    texture = TextureCoordinate(
                        textureSet = textureSet,
                        textureItem = normalTextureItem,
                    ),
                )
                val activeTexture = if (activeGray && textureSet.grayWhenActive) {
                    ButtonActiveTexture.Gray
                } else {
                    activeTextureItem?.let {
                        ButtonActiveTexture.Texture(ButtonTexture.Fixed(
                            texture = TextureCoordinate(
                                textureSet = textureSet,
                                textureItem = it,
                            ),
                        ))
                    } ?: ButtonActiveTexture.Same
                }
                val doubleClick = doubleClickAction?.let { apiAction ->
                    ButtonTrigger.DoubleClickTrigger(action = apiAction)
                }
                val trigger = ButtonTrigger(
                    down = downAction,
                    press = pressKeyMapping,
                    release = releaseAction,
                    doubleClick = doubleClick ?: ButtonTrigger.DoubleClickTrigger(),
                )
                CustomWidget(
                    normalTexture = normalTexture,
                    activeTexture = activeTexture,
                    action = trigger,
                    name = when (name) {
                        is ApiText.Translatable -> ControllerWidget.Name.TranslatableString(name.id)
                        is ApiText.Literal -> ControllerWidget.Name.Literal(name.string)
                        is ApiText.Raw -> ControllerWidget.Name.Literal(name.text.string)
                    },
                    align = Align.CENTER_CENTER,
                )
            },
        ).also { BuiltinWidgets.registry.register(id, it) }
    }
}
