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
import top.fifthlight.touchcontroller.api.v1.widget.BuiltInWidgetTextureProvider
import top.fifthlight.touchcontroller.api.v1.widget.TopBarWidgetBuilder
import top.fifthlight.touchcontroller.api.v1.widget.WidgetTexture
import top.fifthlight.touchcontroller.api.v1.widget.WidgetTextureBuilder
import top.fifthlight.touchcontroller.api.v1.widget.WidgetTriggerActionProvider
import top.fifthlight.touchcontroller.common.api.text.ApiTextFactory
import java.util.function.Consumer

class TouchControllerApiImpl : TouchControllerApi {
    override fun getTextFactory() = ApiTextFactory

    override fun registerGameAction(
        id: String,
        name: Text,
        action: GameAction
    ): GameActionInstance {
        TODO("Not yet implemented")
    }

    override fun registerPlayerAction(
        id: String,
        name: Text,
        action: PlayerAction
    ): PlayerActionInstance {
        TODO("Not yet implemented")
    }

    override fun getBuiltInWidgetTextureProvider(): BuiltInWidgetTextureProvider {
        TODO("Not yet implemented")
    }

    override fun registerWidgetTexture(textureBuilder: Consumer<WidgetTextureBuilder>): WidgetTexture {
        TODO("Not yet implemented")
    }

    override fun getWidgetTriggerActionProvider(): WidgetTriggerActionProvider {
        TODO("Not yet implemented")
    }

    override fun registerBuiltInWidget(widgetBuilder: Consumer<BuiltInWidgetBuilder>) {
        TODO("Not yet implemented")
    }

    override fun registerTopBarWidget(widgetBuilder: Consumer<TopBarWidgetBuilder>) {
        TODO("Not yet implemented")
    }

    companion object : TouchControllerApi by TouchControllerApi.getInstance()
        ?: error("Failed to load TouchController API!")
}
