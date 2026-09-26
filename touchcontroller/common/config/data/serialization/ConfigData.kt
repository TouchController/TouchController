/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.config.data.serialization

import com.ubertob.kondor.json.*
import com.ubertob.kondor.json.jsonnode.FieldsValues
import com.ubertob.kondor.json.jsonnode.NodePath
import top.fifthlight.combine.core.paint.Colors.WHITE
import top.fifthlight.touchcontroller.common.config.data.*
import top.fifthlight.touchcontroller.common.serialization.JColor
import top.fifthlight.touchcontroller.common.serialization.enumByName

object JChatConfig : JObj<ChatConfig>() {
    val lineSpacing by JFieldMaybe(ChatConfig::lineSpacing, JInt)
    val textColor by JFieldMaybe(ChatConfig::textColor, JColor)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = ChatConfig(
        lineSpacing = (+lineSpacing) ?: 0,
        textColor = (+textColor) ?: WHITE,
    )
}

object JControlConfig : JObj<ControlConfig>() {
    val viewMovementSensitivity by JFieldMaybe(ControlConfig::viewMovementSensitivity, JFloat)
    val viewHoldDetectThreshold by JFieldMaybe(ControlConfig::viewHoldDetectThreshold, JInt)
    val viewHoldDetectTicks by JFieldMaybe(ControlConfig::viewHoldDetectTicks, JInt)
    val creativeBreakDetectTicks by JFieldMaybe(ControlConfig::creativeBreakDetectTicks, JInt)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = ControlConfig(
        viewMovementSensitivity = (+viewMovementSensitivity) ?: 495f,
        viewHoldDetectThreshold = (+viewHoldDetectThreshold) ?: 2,
        viewHoldDetectTicks = (+viewHoldDetectTicks) ?: 5,
        creativeBreakDetectTicks = (+creativeBreakDetectTicks) ?: 7,
    )
}

object JDebugConfig : JObj<DebugConfig>() {
    val showPointers by JFieldMaybe(DebugConfig::showPointers, JBoolean)
    val enableTouchEmulation by JFieldMaybe(DebugConfig::enableTouchEmulation, JBoolean)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = DebugConfig(
        showPointers = (+showPointers) ?: false,
        enableTouchEmulation = (+enableTouchEmulation) ?: false,
    )
}

object JRegularConfig : JObj<RegularConfig>() {
    val disableMouseMove by JFieldMaybe(RegularConfig::disableMouseMove, JBoolean)
    val disableMouseClick by JFieldMaybe(RegularConfig::disableMouseClick, JBoolean)
    val disableMouseLock by JFieldMaybe(RegularConfig::disableMouseLock, JBoolean)
    val disableHotBarKey by JFieldMaybe(RegularConfig::disableHotBarKey, JBoolean)
    val vibration by JFieldMaybe(RegularConfig::vibration, JBoolean)
    val quickHandSwap by JFieldMaybe(RegularConfig::quickHandSwap, JBoolean)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = RegularConfig(
        disableMouseMove = (+disableMouseMove) ?: true,
        disableMouseClick = (+disableMouseClick) ?: true,
        disableMouseLock = (+disableMouseLock) ?: false,
        disableHotBarKey = (+disableHotBarKey) ?: false,
        vibration = (+vibration) ?: true,
        quickHandSwap = (+quickHandSwap) ?: false,
    )
}

val JStatusConfigStatus = enumByName<StatusConfig.Status> {
    "DISABLED" encodes StatusConfig.Status.DISABLED
    "ONLY_VIEW_CLICK_TO_INTERACT" encodes StatusConfig.Status.ONLY_VIEW_CLICK_TO_INTERACT
    "ONLY_VIEW_AIMING_BY_CROSSHAIR" encodes StatusConfig.Status.ONLY_VIEW_AIMING_BY_CROSSHAIR
    "ENABLED" encodes StatusConfig.Status.ENABLED
}

object JStatusConfig : JObj<StatusConfig>() {
    val status by JFieldMaybe(StatusConfig::status, JStatusConfigStatus)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = StatusConfig(
        status = (+status) ?: StatusConfig.Status.ENABLED,
    )
}

object JTouchRingConfig : JObj<TouchRingConfig>() {
    val radius by JFieldMaybe(TouchRingConfig::radius, JInt)
    val outerRadius by JFieldMaybe(TouchRingConfig::outerRadius, JInt)
    val initialProgress by JFieldMaybe(TouchRingConfig::initialProgress, JFloat)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = TouchRingConfig(
        radius = (+radius) ?: 36,
        outerRadius = (+outerRadius) ?: 2,
        initialProgress = (+initialProgress) ?: .5f,
    )
}
