/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.control.property.serialization

import com.ubertob.kondor.json.JFieldMaybe
import com.ubertob.kondor.json.JObj
import com.ubertob.kondor.json.jsonnode.FieldsValues
import com.ubertob.kondor.json.jsonnode.NodePath
import top.fifthlight.touchcontroller.assets.texture.set.BuiltInTextureItems
import top.fifthlight.touchcontroller.assets.texture.set.BuiltInTextureSets
import top.fifthlight.touchcontroller.common.assets.serialization.JTextureItem
import top.fifthlight.touchcontroller.common.assets.serialization.JTextureSet
import top.fifthlight.touchcontroller.common.control.property.TextureCoordinate

object JTextureCoordinate : JObj<TextureCoordinate>() {
    val textureSet by JFieldMaybe(TextureCoordinate::textureSet, JTextureSet)
    val textureItem by JFieldMaybe(TextureCoordinate::textureItem, JTextureItem)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = TextureCoordinate(
        textureSet = (+textureSet) ?: BuiltInTextureSets.classic,
        textureItem = (+textureItem) ?: BuiltInTextureItems.up,
    )
}
