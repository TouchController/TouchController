/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.control.widget.dpad

import top.fifthlight.combine.core.data.Identifier
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.assets.texture.set.BuiltInTextureItems
import top.fifthlight.touchcontroller.common.control.action.ButtonTrigger
import top.fifthlight.touchcontroller.common.control.texture.TextureCoordinate

sealed class DPadExtraButton {
    abstract val type: Type
    abstract val info: ButtonInfo?

    enum class Type(
        val nameId: Identifier,
    ) {
        NONE(Texts.WIDGET_DPAD_PROPERTY_EXTRA_BUTTON_TYPE_NONE),
        NORMAL(Texts.WIDGET_DPAD_PROPERTY_EXTRA_BUTTON_TYPE_NORMAL),
        SWIPE(Texts.WIDGET_DPAD_PROPERTY_EXTRA_BUTTON_TYPE_SWIPE),
        SWIPE_LOCKING(Texts.WIDGET_DPAD_PROPERTY_EXTRA_BUTTON_TYPE_SWIPE_LOCKING),
    }

    sealed class ActiveTexture {
        abstract val type: Type

        enum class Type(
            val nameId: Identifier,
        ) {
            SAME(Texts.WIDGET_DPAD_PROPERTY_EXTRA_BUTTON_ACTIVE_TEXTURE_SAME),
            GRAY(Texts.WIDGET_DPAD_PROPERTY_EXTRA_BUTTON_ACTIVE_TEXTURE_GRAY),
            TEXTURE(Texts.WIDGET_DPAD_PROPERTY_EXTRA_BUTTON_ACTIVE_TEXTURE_TEXTURE)
        }

        data object Same : ActiveTexture() {
            override val type: Type
                get() = Type.SAME
        }

        data object Gray : ActiveTexture() {
            override val type: Type
                get() = Type.GRAY
        }

        data class Texture(
            val texture: TextureCoordinate = TextureCoordinate(
                textureItem = BuiltInTextureItems.sneak,
            ),
        ) : ActiveTexture() {
            override val type: Type
                get() = Type.TEXTURE
        }
    }

    data class ButtonInfo(
        val size: Int = 22,
        val texture: TextureCoordinate = TextureCoordinate(
            textureItem = BuiltInTextureItems.sneak,
        ),
        val activeTexture: ActiveTexture = ActiveTexture.Gray,
    )

    data object None : DPadExtraButton() {
        override val type: Type
            get() = Type.NONE
        override val info: ButtonInfo?
            get() = null
    }

    data class Normal(
        val trigger: ButtonTrigger = ButtonTrigger(),
        override val info: ButtonInfo = ButtonInfo(),
    ) : DPadExtraButton() {
        override val type: Type
            get() = Type.NORMAL
    }

    data class Swipe(
        val trigger: ButtonTrigger = ButtonTrigger(),
        override val info: ButtonInfo = ButtonInfo(),
    ) : DPadExtraButton() {
        override val type: Type
            get() = Type.SWIPE
    }

    data class SwipeLocking(
        val press: String? = null,
        override val info: ButtonInfo = ButtonInfo(),
    ) : DPadExtraButton() {
        override val type: Type
            get() = Type.SWIPE_LOCKING
    }
}
