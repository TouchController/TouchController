/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.config.data


data class ControlConfig(
    val viewMovementSensitivity: Float = 495f,
    val viewHoldDetectThreshold: Int = 2,
    val viewHoldDetectTicks: Int = 5,
    val creativeBreakDetectTicks: Int = 7,
)
