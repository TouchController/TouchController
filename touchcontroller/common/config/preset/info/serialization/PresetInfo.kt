/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.config.preset.info.serialization

import com.ubertob.kondor.json.*
import com.ubertob.kondor.json.jsonnode.FieldsValues
import com.ubertob.kondor.json.jsonnode.NodePath
import top.fifthlight.touchcontroller.common.config.preset.info.CustomCondition
import top.fifthlight.touchcontroller.common.config.preset.info.LayerCustomConditions
import top.fifthlight.touchcontroller.common.config.preset.info.PresetControlInfo
import top.fifthlight.touchcontroller.common.serialization.JPersistentList
import top.fifthlight.touchcontroller.common.serialization.JUuid
import top.fifthlight.touchcontroller.common.serialization.JValueClass

object JCustomCondition : JObj<CustomCondition>() {
    private val uuid by str(JUuid, CustomCondition::uuid)
    private val name by JFieldMaybe(CustomCondition::name, JString)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = CustomCondition(
        uuid = +uuid,
        name = +name,
    )
}

val JLayerCustomConditions = JValueClass(
    getter = LayerCustomConditions::conditions,
    factory = ::LayerCustomConditions,
    converter = JPersistentList(JCustomCondition),
)

object JPresetControlInfo : JObj<PresetControlInfo>() {
    private val splitControls by JFieldMaybe(PresetControlInfo::splitControls, JBoolean)
    private val disableTouchGesture by JFieldMaybe(PresetControlInfo::disableTouchGesture, JBoolean)
    private val disableCrosshair by JFieldMaybe(PresetControlInfo::disableCrosshair, JBoolean)
    private val customConditions by JFieldMaybe(PresetControlInfo::customConditions, JLayerCustomConditions)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = PresetControlInfo(
        splitControls = (+splitControls) ?: false,
        disableTouchGesture = (+disableTouchGesture) ?: false,
        disableCrosshair = (+disableCrosshair) ?: true,
        customConditions = (+customConditions) ?: LayerCustomConditions(),
    )
}
