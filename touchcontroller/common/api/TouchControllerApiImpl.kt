/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.api

import top.fifthlight.touchcontroller.api.v1.TouchControllerApi
import top.fifthlight.touchcontroller.api.v1.action.GameAction
import top.fifthlight.touchcontroller.api.v1.action.GameActionInstance
import top.fifthlight.touchcontroller.api.v1.action.PlayerAction
import top.fifthlight.touchcontroller.api.v1.action.PlayerActionInstance
import top.fifthlight.touchcontroller.api.v1.text.Text
import top.fifthlight.touchcontroller.api.v1.widget.BuiltInWidget
import top.fifthlight.touchcontroller.api.v1.widget.BuiltInWidgetBuilder
import top.fifthlight.touchcontroller.api.v1.widget.WidgetTextureBuilder
import top.fifthlight.touchcontroller.common.api.text.ApiTextFactory
import top.fifthlight.touchcontroller.common.api.text.text
import top.fifthlight.touchcontroller.common.api.texture.ApiBuiltInWidgetTextureProvider
import top.fifthlight.touchcontroller.common.api.texture.ApiWidgetTextureBuilder
import top.fifthlight.touchcontroller.common.api.trigger.ApiWidgetTriggerActionProvider
import top.fifthlight.touchcontroller.common.api.widget.ApiBuiltInWidgetBuilder
import top.fifthlight.touchcontroller.common.config.preset.topbar.TopBarWidgets
import top.fifthlight.touchcontroller.common.control.action.GameActionInstanceImpl
import top.fifthlight.touchcontroller.common.control.action.GameActions
import top.fifthlight.touchcontroller.common.control.action.PlayerActionInstanceImpl
import top.fifthlight.touchcontroller.common.control.action.PlayerActions
import top.fifthlight.touchcontroller.common.control.builtin.BuiltinWidgets
import java.util.function.Consumer
import top.fifthlight.touchcontroller.common.control.builtin.BuiltInWidget as BuiltInWidgetImpl

class TouchControllerApiImpl : TouchControllerApi {
    override fun getApiLevel() = 2

    override fun getTextFactory() = ApiTextFactory

    override fun registerGameAction(
        id: String,
        name: Text,
        action: GameAction
    ): GameActionInstance = GameActionInstanceImpl(
        name = name.text,
        action = action
    ).also { GameActions.registry.register(id, it) }

    override fun registerPlayerAction(
        id: String,
        name: Text,
        action: PlayerAction,
    ): PlayerActionInstance = PlayerActionInstanceImpl(
        name = name.text,
        action = { action.action(it) },
    ).also { PlayerActions.registry.register(id, it) }

    override fun getBuiltInWidgetTextureProvider() = ApiBuiltInWidgetTextureProvider

    override fun registerWidgetTexture(textureBuilder: Consumer<WidgetTextureBuilder>) =
        ApiWidgetTextureBuilder().also { textureBuilder.accept(it) }.build()

    override fun getWidgetTriggerActionProvider() = ApiWidgetTriggerActionProvider

    override fun registerBuiltInWidget(widgetBuilder: Consumer<BuiltInWidgetBuilder>) =
        ApiBuiltInWidgetBuilder().also { widgetBuilder.accept(it) }.build()

    override fun registerTopBarWidget(widget: BuiltInWidget, defaultAdded: Boolean) {
        val widget = widget as BuiltInWidgetImpl
        val id = BuiltinWidgets.registry.getId(widget) ?: error("Widget not registered")
        TopBarWidgets.registry.register(id, widget)
        if (defaultAdded) {
            TopBarWidgets.defaultAdded.add(widget)
        }
    }

    companion object : TouchControllerApi by TouchControllerApi.getInstance()
        ?: error("Failed to load TouchController API!")
}
