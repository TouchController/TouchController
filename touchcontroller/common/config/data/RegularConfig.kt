/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.config.data


data class RegularConfig(
    val disableMouseMove: Boolean = true,
    val disableMouseClick: Boolean = true,
    val disableMouseLock: Boolean = false,
    val disableHotBarKey: Boolean = false,
    val vibration: Boolean = true,
    val quickHandSwap: Boolean = false,
)
