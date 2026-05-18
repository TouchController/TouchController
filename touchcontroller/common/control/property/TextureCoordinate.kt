/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.control.property

import kotlinx.serialization.Serializable
import top.fifthlight.combine.core.paint.Texture
import top.fifthlight.touchcontroller.assets.texture.set.BuiltInTextureItems
import top.fifthlight.touchcontroller.assets.texture.set.BuiltInTextureSets
import top.fifthlight.touchcontroller.common.assets.TextureItem
import top.fifthlight.touchcontroller.common.assets.TextureSet

@Serializable
data class TextureCoordinate(
    val textureSet: TextureSet = BuiltInTextureSets.classic,
    val textureItem: TextureItem = BuiltInTextureItems.up,
) {
    val texture: Texture
        get() = textureItem.get(textureSet)
}
