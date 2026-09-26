/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.control.action

import top.fifthlight.touchcontroller.common.layout.Context
import top.fifthlight.touchcontroller.common.layout.widget.button.ButtonResult
import kotlin.uuid.Uuid

data class ButtonTrigger(
    val down: WidgetTriggerAction? = null,
    val press: String? = null,
    val release: WidgetTriggerAction? = null,
    val doubleClick: DoubleClickTrigger = DoubleClickTrigger(),
) {
    data class DoubleClickTrigger(
        val interval: Int = 7,
        val action: WidgetTriggerAction? = null,
    )

    fun refresh(context: Context, id: Uuid) {
        down?.refresh(id, context.timer.renderTick)
        release?.refresh(id, context.timer.renderTick)
        doubleClick.action?.refresh(id, context.timer.renderTick)
    }

    fun hasLock(id: Uuid) =
        down?.hasLock(id) == true || release?.hasLock(id) == true || doubleClick.action?.hasLock(id) == true

    fun trigger(context: Context, buttonResult: ButtonResult, id: Uuid) {
        val (newPointer, clicked, release) = buttonResult
        if (newPointer) {
            if (down != null) {
                context.result.pendingAction.add { tick, player ->
                    down.trigger(id, tick.renderTick, player)
                }
            }
            if (doubleClick.action != null) {
                if (context.status.doubleClickCounter.click(context.timer.clientTick, id, doubleClick.interval)) {
                    context.result.pendingAction.add { tick, player ->
                        doubleClick.action.trigger(id, tick.renderTick, player)
                    }
                }
            }
        }
        if (clicked && press != null) {
            context.keyBindingHandler.getState(press)?.clicked = true
        }
        if (release && this.release != null) {
            context.result.pendingAction.add { tick, player ->
                this.release.trigger(id, tick.renderTick, player)
            }
        }
    }
}
