/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.config.preset.topbar

import top.fifthlight.touchcontroller.common.control.builtin.BuiltInWidget
import top.fifthlight.touchcontroller.common.control.builtin.BuiltinWidgets
import top.fifthlight.touchcontroller.common.util.registry.SimpleRegistry

object TopBarWidgets {
    val registry = SimpleRegistry<BuiltInWidget>()

    private fun register(
        id: String,
        widget: BuiltInWidget,
    ) = widget.also {
        registry.register(id, widget)
    }

    val chat = register("chat", BuiltinWidgets.chat)
    val vanillaChat = register("vanillaChat", BuiltinWidgets.vanillaChat)
    val pause = register("pause", BuiltinWidgets.pause)
    val hideHud = register("hideHud", BuiltinWidgets.hideHud)
    val switchPerspective = register("switchPerspective", BuiltinWidgets.switchPerspective)
    val playerList = register("playerList", BuiltinWidgets.playerList)
    val screenshot = register("screenshot", BuiltinWidgets.screenshot)

    val defaultAdded = mutableListOf(
        chat,
        pause,
    )
}
