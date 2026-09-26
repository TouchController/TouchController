/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.serialization

import com.ubertob.kondor.json.*
import com.ubertob.kondor.json.jsonnode.FieldsValues
import com.ubertob.kondor.json.jsonnode.NodePath
import top.fifthlight.data.*

object JIntOffset : JObj<IntOffset>() {
    val x by num(IntOffset::x)
    val y by num(IntOffset::y)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = IntOffset(
        x = +x,
        y = +y,
    )
}

object JIntSize : JObj<IntSize>() {
    val width by num(IntSize::width)
    val height by num(IntSize::height)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = IntSize(
        width = +width,
        height = +height,
    )
}

object JIntPadding : JObj<IntPadding>() {
    val left by JFieldMaybe(IntPadding::left, JInt)
    val top by JFieldMaybe(IntPadding::top, JInt)
    val right by JFieldMaybe(IntPadding::right, JInt)
    val bottom by JFieldMaybe(IntPadding::bottom, JInt)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = IntPadding(
        left = (+left) ?: 0,
        top = (+top) ?: 0,
        right = (+right) ?: 0,
        bottom = (+bottom) ?: 0,
    )
}

object JIntRect : JObj<IntRect>() {
    val offset by JFieldMaybe(IntRect::offset, JIntOffset)
    val size by JFieldMaybe(IntRect::size, JIntSize)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = IntRect(
        offset = (+offset) ?: IntOffset.ZERO,
        size = (+size) ?: IntSize.ZERO,
    )
}

object JOffset : JObj<Offset>() {
    val x by num(Offset::x)
    val y by num(Offset::y)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = Offset(
        x = +x,
        y = +y,
    )
}

object JSize : JObj<Size>() {
    val width by num(Size::width)
    val height by num(Size::height)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = Size(
        width = +width,
        height = +height,
    )
}

object JRect : JObj<Rect>() {
    val offset by obj(JOffset, Rect::offset)
    val size by obj(JSize, Rect::size)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = Rect(
        offset = +offset,
        size = +size,
    )
}
