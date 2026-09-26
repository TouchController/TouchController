/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.control.action.serialization

import com.ubertob.kondor.json.*
import com.ubertob.kondor.json.jsonnode.FieldsValues
import com.ubertob.kondor.json.jsonnode.NodePath
import top.fifthlight.touchcontroller.common.control.action.WidgetTriggerAction
import top.fifthlight.touchcontroller.common.serialization.JUuid
import top.fifthlight.touchcontroller.common.serialization.enumByName
import top.fifthlight.touchcontroller.common.serialization.sealedByName

val JLockActionType = enumByName<WidgetTriggerAction.Key.Lock.LockActionType> {
    "start" encodes WidgetTriggerAction.Key.Lock.LockActionType.START
    "stop" encodes WidgetTriggerAction.Key.Lock.LockActionType.STOP
    "invert" encodes WidgetTriggerAction.Key.Lock.LockActionType.INVERT
}

object JKeyClick : JObj<WidgetTriggerAction.Key.Click>() {
    val keyBinding by JFieldMaybe(WidgetTriggerAction.Key.Click::keyBinding, JString)
    val keepInClientTick by JFieldMaybe(WidgetTriggerAction.Key.Click::keepInClientTick, JBoolean)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = WidgetTriggerAction.Key.Click(
        keyBinding = +keyBinding,
        keepInClientTick = (+keepInClientTick) ?: true,
    )
}

object JKeyLock : JObj<WidgetTriggerAction.Key.Lock>() {
    val keyBinding by JFieldMaybe(WidgetTriggerAction.Key.Lock::keyBinding, JString)
    val lockType by JFieldMaybe(WidgetTriggerAction.Key.Lock::lockType, JLockActionType)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = WidgetTriggerAction.Key.Lock(
        keyBinding = +keyBinding,
        lockType = (+lockType) ?: WidgetTriggerAction.Key.Lock.LockActionType.INVERT,
    )
}

object JGameTriggerAction : JObj<WidgetTriggerAction.Game>() {
    val action by str(JGameActionInstance, WidgetTriggerAction.Game::action)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = WidgetTriggerAction.Game(+action)
}

object JPlayerTriggerAction : JObj<WidgetTriggerAction.Player>() {
    val action by str(JPlayerActionInstance, WidgetTriggerAction.Player::action)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = WidgetTriggerAction.Player(+action)
}

object JLayerToggle : JObj<WidgetTriggerAction.LayerCondition.Toggle>() {
    val conditionUuid by JFieldMaybe(WidgetTriggerAction.LayerCondition.Toggle::conditionUuid, JUuid)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = WidgetTriggerAction.LayerCondition.Toggle(
        conditionUuid = +conditionUuid,
    )
}

object JLayerEnable : JObj<WidgetTriggerAction.LayerCondition.Enable>() {
    val conditionUuid by JFieldMaybe(WidgetTriggerAction.LayerCondition.Enable::conditionUuid, JUuid)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = WidgetTriggerAction.LayerCondition.Enable(
        conditionUuid = +conditionUuid,
    )
}

object JLayerDisable : JObj<WidgetTriggerAction.LayerCondition.Disable>() {
    val conditionUuid by JFieldMaybe(WidgetTriggerAction.LayerCondition.Disable::conditionUuid, JUuid)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = WidgetTriggerAction.LayerCondition.Disable(
        conditionUuid = +conditionUuid,
    )
}

val JWidgetTriggerAction = sealedByName {
    "click" encodes subtype<WidgetTriggerAction.Key.Click>(JKeyClick)
    "lock" encodes subtype<WidgetTriggerAction.Key.Lock>(JKeyLock)
    "game" encodes subtype<WidgetTriggerAction.Game>(JGameTriggerAction)
    "player" encodes subtype<WidgetTriggerAction.Player>(JPlayerTriggerAction)
    "layer_toggle" encodes subtype<WidgetTriggerAction.LayerCondition.Toggle>(JLayerToggle)
    "layer_enable" encodes subtype<WidgetTriggerAction.LayerCondition.Enable>(JLayerEnable)
    "layer_disable" encodes subtype<WidgetTriggerAction.LayerCondition.Disable>(JLayerDisable)
}
