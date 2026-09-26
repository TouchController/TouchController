/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.config.data


data class TouchRingConfig(
    val radius: Int = 36,
    val outerRadius: Int = 2,
    val initialProgress: Float = .5f
)
