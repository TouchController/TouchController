/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.control.widget.custom

import top.fifthlight.combine.core.data.Identifier
import top.fifthlight.combine.core.paint.Color
import top.fifthlight.combine.core.paint.Colors
import top.fifthlight.data.IntPadding
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.assets.texture.empty.EmptyTexture
import top.fifthlight.touchcontroller.assets.texture.set.BuiltInTextureItems
import top.fifthlight.touchcontroller.assets.texture.set.BuiltInTextureSets
import top.fifthlight.touchcontroller.common.control.texture.TextureCoordinate

sealed class ButtonTexture {
    abstract val type: Type

    data class Empty(
        val extraPadding: IntPadding = IntPadding(4),
    ) : ButtonTexture() {
        override val type: Type
            get() = Type.EMPTY
    }

    data class Fill(
        val borderWidth: Int = 0,
        val extraPadding: IntPadding = IntPadding(4),
        val borderColor: Color = Colors.WHITE,
        val backgroundColor: Color = Colors.BLACK,
    ) : ButtonTexture() {
        override val type: Type
            get() = Type.FILL
    }

    data class Fixed(
        val texture: TextureCoordinate = TextureCoordinate(
            textureSet = BuiltInTextureSets.classic,
            textureItem = BuiltInTextureItems.up,
        ),
        val scale: Float = 2f,
    ) : ButtonTexture() {
        override val type: Type
            get() = Type.FIXED
    }

    data class NinePatch(
        val texture: EmptyTexture = EmptyTexture.EMPTY_1,
        val extraPadding: IntPadding = IntPadding(4),
    ) : ButtonTexture() {
        override val type: Type
            get() = Type.NINE_PATCH
    }

    enum class Type(val nameId: Identifier) {
        EMPTY(Texts.WIDGET_TEXTURE_TYPE_EMPTY),
        FILL(Texts.WIDGET_TEXTURE_TYPE_FILL),
        FIXED(Texts.WIDGET_TEXTURE_TYPE_FIXED),
        NINE_PATCH(Texts.WIDGET_TEXTURE_TYPE_NINE_PATCH),
    }
}

sealed class ButtonActiveTexture {
    abstract val type: Type

    data object Same : ButtonActiveTexture() {
        override val type = Type.SAME
    }

    data object Gray : ButtonActiveTexture() {
        override val type = Type.GRAY
    }

    data class Texture(
        val texture: ButtonTexture = ButtonTexture.Empty()
    ) : ButtonActiveTexture() {
        override val type
            get() = Type.TEXTURE
    }

    enum class Type(val nameId: Identifier) {
        SAME(Texts.WIDGET_ACTIVE_TEXTURE_SAME),
        GRAY(Texts.WIDGET_ACTIVE_TEXTURE_GRAY),
        TEXTURE(Texts.WIDGET_ACTIVE_TEXTURE_TEXTURE),
    }
}
