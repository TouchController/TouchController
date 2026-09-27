/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.api.trigger

import top.fifthlight.touchcontroller.common.control.action.WidgetTriggerAction

data class ApiWidgetTriggerAction(
    val action: WidgetTriggerAction,
): top.fifthlight.touchcontroller.api.v1.widget.WidgetTriggerAction

val top.fifthlight.touchcontroller.api.v1.widget.WidgetTriggerAction.action
    get() = (this as ApiWidgetTriggerAction).action
