/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.util.offset

import top.fifthlight.data.IntSize
import top.fifthlight.data.Offset

fun Offset.fixAspectRadio(windowSize: IntSize): Offset = Offset(
    x = x,
    y = y * windowSize.height / windowSize.width
)
