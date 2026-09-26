/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.config.preset.info.serialization

import com.ubertob.kondor.json.*
import com.ubertob.kondor.json.jsonnode.ArrayNode
import com.ubertob.kondor.json.jsonnode.FieldsValues
import com.ubertob.kondor.json.jsonnode.NodePath
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import top.fifthlight.touchcontroller.common.config.preset.info.CustomCondition
import top.fifthlight.touchcontroller.common.config.preset.info.LayerCustomConditions
import top.fifthlight.touchcontroller.common.config.preset.info.PresetControlInfo
import top.fifthlight.touchcontroller.common.serialization.JUuid

object JCustomCondition : JObj<CustomCondition>() {
    private val uuid by str(JUuid, CustomCondition::uuid)
    private val name by JFieldMaybe(CustomCondition::name, JString)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = CustomCondition(
        uuid = +uuid,
        name = +name,
    )
}

object JLayerCustomConditions : JArray<CustomCondition, PersistentList<CustomCondition>> {
    override val converter: JConverter<CustomCondition> = JCustomCondition
    override val _nodeType = ArrayNode

    override fun convertToCollection(iterable: Iterable<CustomCondition?>) =
        iterable.filterNotNull().toPersistentList()

    override fun convertFromCollection(collection: PersistentList<CustomCondition>): Iterable<CustomCondition?> =
        collection
}

object JPresetControlInfo : JObj<PresetControlInfo>() {
    private val splitControls by JFieldMaybe(PresetControlInfo::splitControls, JBoolean)
    private val disableTouchGesture by JFieldMaybe(PresetControlInfo::disableTouchGesture, JBoolean)
    private val disableCrosshair by JFieldMaybe(PresetControlInfo::disableCrosshair, JBoolean)
    private val customConditions by JFieldMaybe(
        { info: PresetControlInfo -> info.customConditions.conditions },
        JList(JCustomCondition),
    )

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = PresetControlInfo(
        splitControls = (+splitControls) ?: false,
        disableTouchGesture = (+disableTouchGesture) ?: false,
        disableCrosshair = (+disableCrosshair) ?: true,
        customConditions = LayerCustomConditions((+customConditions)?.toPersistentList() ?: persistentListOf()),
    )
}
