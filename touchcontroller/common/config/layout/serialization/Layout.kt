/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.config.layout.serialization

import com.ubertob.kondor.json.JFieldMaybe
import com.ubertob.kondor.json.JList
import com.ubertob.kondor.json.JObj
import com.ubertob.kondor.json.JString
import com.ubertob.kondor.json.jsonnode.FieldsValues
import com.ubertob.kondor.json.jsonnode.NodePath
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import top.fifthlight.touchcontroller.common.config.condition.LayerConditions
import top.fifthlight.touchcontroller.common.config.condition.serialization.JLayerConditions
import top.fifthlight.touchcontroller.common.config.layout.ControllerLayout
import top.fifthlight.touchcontroller.common.config.layout.LayoutLayer
import top.fifthlight.touchcontroller.common.config.layout.LayoutLayer.Companion.DEFAULT_LAYER_NAME
import top.fifthlight.touchcontroller.common.control.serialization.JControllerWidget
import top.fifthlight.touchcontroller.common.serialization.JPersistentList
import top.fifthlight.touchcontroller.common.serialization.JValueClass

object JLayoutLayer : JObj<LayoutLayer>() {
    val name by JFieldMaybe(LayoutLayer::name, JString)
    val widgets by JFieldMaybe(LayoutLayer::widgets, JList(JControllerWidget))
    val conditions by JFieldMaybe(LayoutLayer::conditions, JLayerConditions)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = LayoutLayer(
        name = (+name) ?: DEFAULT_LAYER_NAME,
        widgets = (+widgets)?.toPersistentList() ?: persistentListOf(),
        conditions = +conditions ?: LayerConditions(),
    )
}

val JControllerLayout = JValueClass(
    getter = ControllerLayout::layers,
    factory = ::ControllerLayout,
    converter = JPersistentList(JLayoutLayer),
)
