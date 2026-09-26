/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.config.preset.serialization

import com.ubertob.kondor.json.JFieldMaybe
import com.ubertob.kondor.json.JObj
import com.ubertob.kondor.json.JString
import com.ubertob.kondor.json.jsonnode.FieldsValues
import com.ubertob.kondor.json.jsonnode.NodePath
import top.fifthlight.touchcontroller.common.config.layout.ControllerLayout
import top.fifthlight.touchcontroller.common.config.layout.serialization.JControllerLayout
import top.fifthlight.touchcontroller.common.config.preset.LayoutPreset
import top.fifthlight.touchcontroller.common.config.preset.LayoutPreset.Companion.DEFAULT_PRESET_NAME
import top.fifthlight.touchcontroller.common.config.preset.info.PresetControlInfo
import top.fifthlight.touchcontroller.common.config.preset.info.serialization.JPresetControlInfo

object JLayoutPreset : JObj<LayoutPreset>() {
    val name by JFieldMaybe(LayoutPreset::name, JString)
    val controlInfo by JFieldMaybe(LayoutPreset::controlInfo, JPresetControlInfo)
    val layout by JFieldMaybe(LayoutPreset::layout, JControllerLayout)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = LayoutPreset(
        name = (+name) ?: DEFAULT_PRESET_NAME,
        controlInfo = (+controlInfo) ?: PresetControlInfo(),
        layout = +layout ?: ControllerLayout(),
    )
}
