/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.presets.state

import top.fifthlight.touchcontroller.common.config.preset.LayoutPreset
import top.fifthlight.touchcontroller.common.config.preset.info.PresetControlInfo
import top.fifthlight.touchcontroller.common.util.name.TranslatableName
import kotlin.uuid.Uuid

sealed class PresetsTabState {
    data object Empty : PresetsTabState()

    data object CreateChoose : PresetsTabState()

    data class CreateEmpty(
        val name: TranslatableName = LayoutPreset.DEFAULT_PRESET_NAME,
        val controlInfo: PresetControlInfo = PresetControlInfo(),
    ) : PresetsTabState() {
        fun toPreset() = LayoutPreset(
            name = name,
            controlInfo = controlInfo,
        )
    }

    data class Edit(
        val uuid: Uuid,
        val name: TranslatableName,
        val controlInfo: PresetControlInfo,
    ) : PresetsTabState() {
        fun edit(preset: LayoutPreset) = preset.copy(
            name = name,
            controlInfo = controlInfo,
        )
    }

    data class Delete(
        val uuid: Uuid,
    ) : PresetsTabState()

    data class Path(
        val path: String? = null,
    ) : PresetsTabState()
}
