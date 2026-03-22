package top.fifthlight.touchcontroller.common.control.widget.custom

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import top.fifthlight.combine.data.Identifier
import top.fifthlight.combine.paint.Color
import top.fifthlight.combine.paint.Colors
import top.fifthlight.data.IntPadding
import top.fifthlight.touchcontroller.assets.EmptyTexture
import top.fifthlight.touchcontroller.assets.Texts
import top.fifthlight.touchcontroller.assets.TextureSet
import top.fifthlight.touchcontroller.common.control.property.TextureCoordinate

@Serializable
sealed class ButtonTexture {
    abstract val type: Type

    @Serializable
    @SerialName("empty")
    data class Empty(
        val extraPadding: IntPadding = IntPadding(4),
    ) : ButtonTexture() {
        override val type: Type
            get() = Type.EMPTY
    }

    @Serializable
    @SerialName("color")
    data class Fill(
        val borderWidth: Int = 0,
        val extraPadding: IntPadding = IntPadding(4),
        val borderColor: Color = Colors.WHITE,
        val backgroundColor: Color = Colors.BLACK,
    ) : ButtonTexture() {
        override val type: Type
            get() = Type.FILL
    }

    @Serializable
    @SerialName("fixed")
    data class Fixed(
        val texture: TextureCoordinate = TextureCoordinate(
            textureSet = TextureSet.TextureSetKey.CLASSIC,
            textureItem = TextureSet.TextureKey.Up,
        ),
        val scale: Float = 2f,
    ) : ButtonTexture() {
        override val type: Type
            get() = Type.FIXED
    }

    @Serializable
    @SerialName("nine-patch")
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

@Serializable
sealed class ButtonActiveTexture {
    abstract val type: Type

    @Serializable
    @SerialName("same")
    data object Same : ButtonActiveTexture() {
        override val type = Type.SAME
    }

    @Serializable
    @SerialName("gray")
    data object Gray : ButtonActiveTexture() {
        override val type = Type.GRAY
    }

    @Serializable
    @SerialName("texture")
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
