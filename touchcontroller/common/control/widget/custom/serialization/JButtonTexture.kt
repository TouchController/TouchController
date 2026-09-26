/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.control.widget.custom.serialization

import com.ubertob.kondor.json.JFieldMaybe
import com.ubertob.kondor.json.JFloat
import com.ubertob.kondor.json.JInt
import com.ubertob.kondor.json.JObj
import com.ubertob.kondor.json.jsonnode.FieldsValues
import com.ubertob.kondor.json.jsonnode.NodePath
import top.fifthlight.combine.core.paint.Colors
import top.fifthlight.data.IntPadding
import top.fifthlight.touchcontroller.assets.texture.empty.EmptyTexture
import top.fifthlight.touchcontroller.common.control.property.serialization.JTextureCoordinate
import top.fifthlight.touchcontroller.common.control.widget.custom.ButtonActiveTexture
import top.fifthlight.touchcontroller.common.control.widget.custom.ButtonTexture
import top.fifthlight.touchcontroller.common.serialization.*

// The Json form of a generated empty texture is its texture identifier (e.g. "empty_1").
val JEmptyTexture = enumByName<EmptyTexture> {
    "empty_1" encodes EmptyTexture.EMPTY_1
    "empty_1_active" encodes EmptyTexture.EMPTY_1_ACTIVE
    "empty_2" encodes EmptyTexture.EMPTY_2
    "empty_2_active" encodes EmptyTexture.EMPTY_2_ACTIVE
    "empty_3" encodes EmptyTexture.EMPTY_3
    "empty_3_active" encodes EmptyTexture.EMPTY_3_ACTIVE
    "empty_4" encodes EmptyTexture.EMPTY_4
    "empty_4_active" encodes EmptyTexture.EMPTY_4_ACTIVE
    "empty_5" encodes EmptyTexture.EMPTY_5
    "empty_5_active" encodes EmptyTexture.EMPTY_5_ACTIVE
}

object JEmptyButtonTexture : JObj<ButtonTexture.Empty>() {
    val extraPadding by JFieldMaybe(ButtonTexture.Empty::extraPadding, JIntPadding)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = ButtonTexture.Empty(
        extraPadding = (+extraPadding) ?: IntPadding(4),
    )
}

object JFillButtonTexture : JObj<ButtonTexture.Fill>() {
    val borderWidth by JFieldMaybe(ButtonTexture.Fill::borderWidth, JInt)
    val extraPadding by JFieldMaybe(ButtonTexture.Fill::extraPadding, JIntPadding)
    val borderColor by JFieldMaybe(ButtonTexture.Fill::borderColor, JColor)
    val backgroundColor by JFieldMaybe(ButtonTexture.Fill::backgroundColor, JColor)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = ButtonTexture.Fill(
        borderWidth = (+borderWidth) ?: 0,
        extraPadding = (+extraPadding) ?: IntPadding(4),
        borderColor = (+borderColor) ?: Colors.WHITE,
        backgroundColor = (+backgroundColor) ?: Colors.BLACK,
    )
}

object JFixedButtonTexture : JObj<ButtonTexture.Fixed>() {
    val texture by JFieldMaybe(ButtonTexture.Fixed::texture, JTextureCoordinate)
    val scale by JFieldMaybe(ButtonTexture.Fixed::scale, JFloat)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = ButtonTexture.Fixed(
        texture = (+texture) ?: ButtonTexture.Fixed().texture,
        scale = (+scale) ?: 2f,
    )
}

object JNinePatchButtonTexture : JObj<ButtonTexture.NinePatch>() {
    val texture by JFieldMaybe(ButtonTexture.NinePatch::texture, JEmptyTexture)
    val extraPadding by JFieldMaybe(ButtonTexture.NinePatch::extraPadding, JIntPadding)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = ButtonTexture.NinePatch(
        texture = (+texture) ?: EmptyTexture.EMPTY_1,
        extraPadding = (+extraPadding) ?: IntPadding(4),
    )
}

val JButtonTexture = sealedByName<ButtonTexture> {
    "empty" encodes subtype<ButtonTexture.Empty>(JEmptyButtonTexture)
    "color" encodes subtype<ButtonTexture.Fill>(JFillButtonTexture)
    "fixed" encodes subtype<ButtonTexture.Fixed>(JFixedButtonTexture)
    "nine-patch" encodes subtype<ButtonTexture.NinePatch>(JNinePatchButtonTexture)
}

val JSameButtonActiveTexture = objectConverter(ButtonActiveTexture.Same)

val JGrayButtonActiveTexture = objectConverter(ButtonActiveTexture.Gray)

object JTextureButtonActiveTexture : JObj<ButtonActiveTexture.Texture>() {
    val texture by JFieldMaybe(ButtonActiveTexture.Texture::texture, JButtonTexture)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = ButtonActiveTexture.Texture(
        texture = (+texture) ?: ButtonTexture.Empty(),
    )
}

val JButtonActiveTexture = sealedByName<ButtonActiveTexture> {
    "same" encodes subtype<ButtonActiveTexture.Same>(JSameButtonActiveTexture)
    "gray" encodes subtype<ButtonActiveTexture.Gray>(JGrayButtonActiveTexture)
    "texture" encodes subtype<ButtonActiveTexture.Texture>(JTextureButtonActiveTexture)
}
