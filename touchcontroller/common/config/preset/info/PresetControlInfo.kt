/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.config.preset.info

import androidx.compose.runtime.Immutable

@Immutable
data class PresetControlInfo(
    val splitControls: Boolean = false,
    val disableTouchGesture: Boolean = false,
    val disableCrosshair: Boolean = true,
    val customConditions: LayerCustomConditions = LayerCustomConditions(),
)
