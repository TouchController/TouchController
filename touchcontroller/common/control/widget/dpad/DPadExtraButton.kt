package top.fifthlight.touchcontroller.common.control.widget.dpad

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import top.fifthlight.combine.data.Identifier
import top.fifthlight.touchcontroller.assets.Texts
import top.fifthlight.touchcontroller.assets.TextureSet
import top.fifthlight.touchcontroller.common.control.action.ButtonTrigger
import top.fifthlight.touchcontroller.common.control.property.TextureCoordinate

@Serializable
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

    @Serializable
    sealed class ActiveTexture {
        abstract val type: Type

        enum class Type(
            val nameId: Identifier,
        ) {
            SAME(Texts.WIDGET_DPAD_PROPERTY_EXTRA_BUTTON_ACTIVE_TEXTURE_SAME),
            GRAY(Texts.WIDGET_DPAD_PROPERTY_EXTRA_BUTTON_ACTIVE_TEXTURE_GRAY),
            TEXTURE(Texts.WIDGET_DPAD_PROPERTY_EXTRA_BUTTON_ACTIVE_TEXTURE_TEXTURE)
        }

        @Serializable
        @SerialName("same")
        data object Same : ActiveTexture() {
            override val type: Type
                get() = Type.SAME
        }

        @Serializable
        @SerialName("gray")
        data object Gray : ActiveTexture() {
            override val type: Type
                get() = Type.GRAY
        }

        @Serializable
        @SerialName("texture")
        data class Texture(
            val texture: TextureCoordinate = TextureCoordinate(
                textureItem = TextureSet.TextureKey.Sneak,
            ),
        ) : ActiveTexture() {
            override val type: Type
                get() = Type.TEXTURE
        }
    }

    @Serializable
    data class ButtonInfo(
        val size: Int = 22,
        val texture: TextureCoordinate = TextureCoordinate(
            textureItem = TextureSet.TextureKey.Sneak,
        ),
        val activeTexture: ActiveTexture = ActiveTexture.Gray,
    )

    @SerialName("none")
    @Serializable
    data object None : DPadExtraButton() {
        override val type: Type
            get() = Type.NONE
        override val info: ButtonInfo?
            get() = null
    }

    @Serializable
    @SerialName("normal")
    data class Normal(
        val trigger: ButtonTrigger = ButtonTrigger(),
        override val info: ButtonInfo = ButtonInfo(),
    ) : DPadExtraButton() {
        override val type: Type
            get() = Type.NORMAL
    }

    @Serializable
    @SerialName("swipe")
    data class Swipe(
        val trigger: ButtonTrigger = ButtonTrigger(),
        override val info: ButtonInfo = ButtonInfo(),
    ) : DPadExtraButton() {
        override val type: Type
            get() = Type.SWIPE
    }

    @Serializable
    @SerialName("swipe_locking")
    data class SwipeLocking(
        val press: String? = null,
        override val info: ButtonInfo = ButtonInfo(),
    ) : DPadExtraButton() {
        override val type: Type
            get() = Type.SWIPE_LOCKING
    }
}
