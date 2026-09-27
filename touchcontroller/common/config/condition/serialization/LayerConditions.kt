/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.config.condition.serialization

import com.ubertob.kondor.json.JObj
import com.ubertob.kondor.json.jsonnode.FieldsValues
import com.ubertob.kondor.json.jsonnode.NodePath
import com.ubertob.kondor.json.obj
import com.ubertob.kondor.json.str
import top.fifthlight.touchcontroller.common.config.condition.*
import top.fifthlight.touchcontroller.common.config.condition.input.serialization.JBuiltinLayerCondition
import top.fifthlight.touchcontroller.common.gal.entity.serialization.JEntityType
import top.fifthlight.touchcontroller.common.serialization.*

val JLayerConditionValue = enumByName<LayerConditions.Value> {
    "never" encodes LayerConditions.Value.NEVER
    "want" encodes LayerConditions.Value.WANT
    "require" encodes LayerConditions.Value.REQUIRE
}

object JBuiltinLayerConditionKey : JObj<BuiltinLayerConditionKey>() {
    val condition by str(JBuiltinLayerCondition, BuiltinLayerConditionKey::condition)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = BuiltinLayerConditionKey(+condition)
}

object JCustomLayerConditionKey : JObj<CustomLayerConditionKey>() {
    val key by str(JUuid, CustomLayerConditionKey::key)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = CustomLayerConditionKey(+key)
}

object JHoldingItemLayerConditionKey : JObj<HoldingItemLayerConditionKey>() {
    val item by obj(JItem, HoldingItemLayerConditionKey::item)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = HoldingItemLayerConditionKey(+item)
}

object JRidingEntityLayerConditionKey : JObj<RidingEntityLayerConditionKey>() {
    val entityType by str(JEntityType, RidingEntityLayerConditionKey::entityType)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = RidingEntityLayerConditionKey(+entityType)
}

object JSelectEntityLayerConditionKey : JObj<SelectEntityLayerConditionKey>() {
    val entityType by str(JEntityType, SelectEntityLayerConditionKey::entityType)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = SelectEntityLayerConditionKey(+entityType)
}

val JLayerConditionKey = sealedByName {
    "builtin" encodes subtype<BuiltinLayerConditionKey>(JBuiltinLayerConditionKey)
    "custom" encodes subtype<CustomLayerConditionKey>(JCustomLayerConditionKey)
    "holding_item" encodes subtype<HoldingItemLayerConditionKey>(JHoldingItemLayerConditionKey)
    "riding_entity" encodes subtype<RidingEntityLayerConditionKey>(JRidingEntityLayerConditionKey)
    "select_entity" encodes subtype<SelectEntityLayerConditionKey>(JSelectEntityLayerConditionKey)
}

object JLayerConditionsItem : JObj<LayerConditions.Item>() {
    val key by obj(JLayerConditionKey, LayerConditions.Item::key)
    val value by str(JLayerConditionValue, LayerConditions.Item::value)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = LayerConditions.Item(+key, +value)
}

val JLayerConditions = JValueClass(
    getter = LayerConditions::conditions,
    factory = ::LayerConditions,
    converter = JPersistentList(JLayerConditionsItem),
)
