/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.serialization

import com.ubertob.kondor.json.JIntRepresentable
import com.ubertob.kondor.json.JObj
import com.ubertob.kondor.json.JStringRepresentable
import com.ubertob.kondor.json.jsonnode.FieldsValues
import com.ubertob.kondor.json.jsonnode.NodePath
import com.ubertob.kondor.json.obj
import top.fifthlight.combine.core.data.Identifier
import top.fifthlight.combine.core.paint.Color
import top.fifthlight.combine.item.data.Item
import top.fifthlight.combine.item.data.ItemFactory
import kotlin.uuid.Uuid

object JUuid : JStringRepresentable<Uuid>() {
    override val cons: (String) -> Uuid = Uuid::parse
    override val render: (Uuid) -> String = Uuid::toString
}

object JIdentifier : JStringRepresentable<Identifier>() {
    override val cons: (String) -> Identifier = ::Identifier
    override val render: (Identifier) -> String = Identifier::toString
}

object JColor : JIntRepresentable<Color>() {
    override val cons: (Int) -> Color = ::Color
    override val render: (Color) -> Int = Color::value
}

object JItem : JObj<Item>() {
    val id by obj(JIdentifier, Item::id)

    override fun FieldsValues.deserializeOrThrow(path: NodePath): Item =
        ItemFactory.create(+id) ?: error("Bad item identifier: $id")
}
