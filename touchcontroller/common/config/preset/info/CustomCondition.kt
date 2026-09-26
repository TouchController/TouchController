/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.config.preset.info

import androidx.compose.runtime.Immutable
import kotlin.uuid.Uuid

@Immutable
data class CustomCondition(
    val uuid: Uuid = Uuid.random(),
    val name: String? = null,
)
