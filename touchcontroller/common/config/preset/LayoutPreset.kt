/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.config.preset

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.toPersistentList
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.common.config.layout.ControllerLayout
import top.fifthlight.touchcontroller.common.config.layout.LayoutLayer
import top.fifthlight.touchcontroller.common.config.preset.info.PresetControlInfo
import top.fifthlight.touchcontroller.common.control.ControllerWidget
import top.fifthlight.touchcontroller.common.util.name.TranslatableName

@Immutable
data class LayoutPreset(
    val name: TranslatableName = DEFAULT_PRESET_NAME,
    val controlInfo: PresetControlInfo = PresetControlInfo(),
    val layout: ControllerLayout = ControllerLayout(),
) {
    fun mapWidgets(transform: (ControllerWidget) -> ControllerWidget) = copy(
        layout = ControllerLayout(layout.layers.map { layer ->
            layer.copy(widgets = layer.widgets.map(transform).toPersistentList())
        }.toPersistentList())
    )

    fun mapLayers(transform: (LayoutLayer) -> LayoutLayer) = copy(
        layout = ControllerLayout(layers = layout.layers.map(transform).toPersistentList()),
    )

    companion object {
        val DEFAULT_PRESET_NAME = TranslatableName.Translatable(Texts.PRESET_NAME_UNNAMED)
    }
}
