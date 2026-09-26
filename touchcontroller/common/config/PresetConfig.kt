/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.config

import top.fifthlight.touchcontroller.common.config.preset.builtin.key.BuiltinPresetKey

sealed class PresetConfig {
    data class BuiltIn(
        val key: BuiltinPresetKey = BuiltinPresetKey(),
    ) : PresetConfig()

    data class Custom(
        val uuid: kotlin.uuid.Uuid? = null,
    ) : PresetConfig()
}
