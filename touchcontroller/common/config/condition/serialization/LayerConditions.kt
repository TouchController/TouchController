/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.config.condition.serialization

import com.ubertob.kondor.json.*
import com.ubertob.kondor.json.jsonnode.ArrayNode
import com.ubertob.kondor.json.jsonnode.FieldsValues
import com.ubertob.kondor.json.jsonnode.NodePath
import kotlinx.collections.immutable.toPersistentList
import top.fifthlight.touchcontroller.common.config.condition.*
import top.fifthlight.touchcontroller.common.config.condition.input.serialization.JBuiltinLayerCondition
import top.fifthlight.touchcontroller.common.gal.entity.serialization.JEntityType
import top.fifthlight.touchcontroller.common.serialization.JItem
import top.fifthlight.touchcontroller.common.serialization.JUuid
import top.fifthlight.touchcontroller.common.serialization.enumByName
import top.fifthlight.touchcontroller.common.serialization.sealedByName

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

val JLayerConditionKey = sealedByName<LayerConditions.Key> {
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

object JLayerConditions : JArray<LayerConditions.Item, LayerConditions> {
    override val converter: JConverter<LayerConditions.Item> = JLayerConditionsItem
    override val _nodeType = ArrayNode

    override fun convertToCollection(iterable: Iterable<LayerConditions.Item?>) =
        LayerConditions(iterable.filterNotNull().toPersistentList())

    override fun convertFromCollection(collection: LayerConditions): Iterable<LayerConditions.Item?> =
        collection.conditions
}
