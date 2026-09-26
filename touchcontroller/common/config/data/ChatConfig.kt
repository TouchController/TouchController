/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.config.data

import top.fifthlight.combine.core.paint.Color
import top.fifthlight.combine.core.paint.Colors.WHITE

data class ChatConfig(
    val lineSpacing: Int = 0,
    val textColor: Color = WHITE,
)
