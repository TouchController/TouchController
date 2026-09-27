/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.api.trigger

import top.fifthlight.touchcontroller.api.v1.action.GameActionInstance
import top.fifthlight.touchcontroller.api.v1.action.PlayerActionInstance
import top.fifthlight.touchcontroller.api.v1.widget.WidgetTriggerActionProvider
import top.fifthlight.touchcontroller.common.control.action.GameActionInstanceImpl
import top.fifthlight.touchcontroller.common.control.action.PlayerActionInstanceImpl
import top.fifthlight.touchcontroller.common.control.action.WidgetTriggerAction

object ApiWidgetTriggerActionProvider: WidgetTriggerActionProvider {
    override fun gameAction(gameAction: GameActionInstance) = ApiWidgetTriggerAction(
        WidgetTriggerAction.Game(gameAction as GameActionInstanceImpl)
    )

    override fun playerAction(playerAction: PlayerActionInstance) = ApiWidgetTriggerAction(
        WidgetTriggerAction.Player(playerAction as PlayerActionInstanceImpl)
    )

    override fun click(keyMapping: String) = ApiWidgetTriggerAction(
        WidgetTriggerAction.Key.Click(keyMapping)
    )

    override fun startLock(keyMapping: String) = ApiWidgetTriggerAction(
        WidgetTriggerAction.Key.Lock(
            keyBinding = keyMapping,
            lockType = WidgetTriggerAction.Key.Lock.LockActionType.START,
        )
    )

    override fun stopLock(keyMapping: String) = ApiWidgetTriggerAction(
        WidgetTriggerAction.Key.Lock(
            keyBinding = keyMapping,
            lockType = WidgetTriggerAction.Key.Lock.LockActionType.STOP,
        )
    )

    override fun invertLock(keyMapping: String) = ApiWidgetTriggerAction(
        WidgetTriggerAction.Key.Lock(
            keyBinding = keyMapping,
            lockType = WidgetTriggerAction.Key.Lock.LockActionType.INVERT,
        )
    )
}
