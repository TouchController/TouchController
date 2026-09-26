/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.config.preset.info

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

@JvmInline
value class LayerCustomConditions(
    val conditions: PersistentList<CustomCondition> = persistentListOf(),
)
