/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.control.action

import top.fifthlight.combine.core.data.Text
import top.fifthlight.touchcontroller.api.v1.action.GameAction
import top.fifthlight.touchcontroller.api.v1.action.GameActionInstance

class GameActionInstanceImpl(
    val hidden: Boolean = false,
    val name: Text,
    val action: GameAction,
) : GameActionInstance {
    operator fun invoke() = action.action()
}
