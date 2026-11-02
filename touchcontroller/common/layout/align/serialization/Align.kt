/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.layout.align.serialization

import top.fifthlight.touchcontroller.common.layout.align.Align
import top.fifthlight.touchcontroller.common.serialization.enumByName

val JAlign = enumByName<Align> {
    listOf("left_top", "LEFT_TOP") encodes Align.LEFT_TOP
    listOf("center_top", "CENTER_TOP") encodes Align.CENTER_TOP
    listOf("right_top", "RIGHT_TOP") encodes Align.RIGHT_TOP
    listOf("left_center", "LEFT_CENTER") encodes Align.LEFT_CENTER
    listOf("center_center", "CENTER_CENTER", "center", "CENTER") encodes Align.CENTER_CENTER
    listOf("right_center", "RIGHT_CENTER") encodes Align.RIGHT_CENTER
    listOf("left_bottom", "LEFT_BOTTOM") encodes Align.LEFT_BOTTOM
    listOf("center_bottom", "CENTER_BOTTOM") encodes Align.CENTER_BOTTOM
    listOf("right_bottom", "RIGHT_BOTTOM") encodes Align.RIGHT_BOTTOM
}
