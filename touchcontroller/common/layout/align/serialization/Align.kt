/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.layout.align.serialization

import top.fifthlight.touchcontroller.common.layout.align.Align
import top.fifthlight.touchcontroller.common.serialization.enumByName

val JAlign = enumByName<Align> {
    "left_top" encodes Align.LEFT_TOP
    "center_top" encodes Align.CENTER_TOP
    "right_top" encodes Align.RIGHT_TOP
    "left_center" encodes Align.LEFT_CENTER
    "center_center" encodes Align.CENTER_CENTER
    "right_center" encodes Align.RIGHT_CENTER
    "left_bottom" encodes Align.LEFT_BOTTOM
    "center_bottom" encodes Align.CENTER_BOTTOM
    "right_bottom" encodes Align.RIGHT_BOTTOM
}
