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
import top.fifthlight.touchcontroller.api.v1.widget.BuiltInWidgetBuilder
import top.fifthlight.touchcontroller.api.v1.widget.TopBarWidgetBuilder
import top.fifthlight.touchcontroller.api.v1.widget.WidgetTextureBuilder
import top.fifthlight.touchcontroller.common.api.text.ApiTextFactory
import top.fifthlight.touchcontroller.common.api.text.text
import top.fifthlight.touchcontroller.common.api.texture.ApiBuiltInWidgetTextureProvider
import top.fifthlight.touchcontroller.common.api.texture.ApiWidgetTextureBuilder
import top.fifthlight.touchcontroller.common.api.trigger.ApiWidgetTriggerActionProvider
import top.fifthlight.touchcontroller.common.api.widget.ApiBuiltInWidgetBuilder
import top.fifthlight.touchcontroller.common.control.action.GameActionInstanceImpl
import top.fifthlight.touchcontroller.common.control.action.GameActions
import top.fifthlight.touchcontroller.common.control.action.PlayerActionInstanceImpl
import top.fifthlight.touchcontroller.common.control.action.PlayerActions
import java.util.function.Consumer

class TouchControllerApiImpl : TouchControllerApi {
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

    override fun registerTopBarWidget(widgetBuilder: Consumer<TopBarWidgetBuilder>) {
        TODO("Not yet implemented")
    }

    companion object : TouchControllerApi by TouchControllerApi.getInstance()
        ?: error("Failed to load TouchController API!")
}
