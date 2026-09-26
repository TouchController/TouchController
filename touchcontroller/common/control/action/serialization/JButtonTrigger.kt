/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.control.action.serialization

import com.ubertob.kondor.json.JFieldMaybe
import com.ubertob.kondor.json.JInt
import com.ubertob.kondor.json.JObj
import com.ubertob.kondor.json.JString
import com.ubertob.kondor.json.jsonnode.FieldsValues
import com.ubertob.kondor.json.jsonnode.NodePath
import top.fifthlight.touchcontroller.common.control.action.ButtonTrigger

object JButtonTrigger : JObj<ButtonTrigger>() {
    val down by JFieldMaybe(ButtonTrigger::down, JWidgetTriggerAction)
    val press by JFieldMaybe(ButtonTrigger::press, JString)
    val release by JFieldMaybe(ButtonTrigger::release, JWidgetTriggerAction)
    val doubleClick by JFieldMaybe(ButtonTrigger::doubleClick, JDoubleClickTrigger)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = ButtonTrigger(
        down = +down,
        press = +press,
        release = +release,
        doubleClick = (+doubleClick) ?: ButtonTrigger.DoubleClickTrigger(),
    )
}

object JDoubleClickTrigger : JObj<ButtonTrigger.DoubleClickTrigger>() {
    val interval by JFieldMaybe(ButtonTrigger.DoubleClickTrigger::interval, JInt)
    val action by JFieldMaybe(ButtonTrigger.DoubleClickTrigger::action, JWidgetTriggerAction)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = ButtonTrigger.DoubleClickTrigger(
        interval = (+interval) ?: 7,
        action = +action,
    )
}
