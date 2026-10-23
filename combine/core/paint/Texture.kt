package top.fifthlight.combine.core.paint

import androidx.compose.runtime.Immutable
import top.fifthlight.combine.core.data.Identifier
import top.fifthlight.data.*
import top.fifthlight.mergetools.api.ExpectFactory

@Immutable
interface Texture : Drawable {
    fun draw(
        canvas: Canvas,
        dstRect: Rect,
        tint: Color = Colors.WHITE,
    ) = draw(
        canvas = canvas,
        dstRect = dstRect,
        tint = tint,
        srcRect = Rect(
            offset = Offset.ZERO,
            size = size.toSize(),
        )
    )

    fun draw(
        canvas: Canvas,
        dstRect: Rect,
        tint: Color = Colors.WHITE,
        srcRect: Rect,
    )

    override fun draw(
        canvas: Canvas,
        dstRect: IntRect,
        tint: Color,
    ) = draw(
        canvas = canvas,
        dstRect = dstRect.toRect(),
        tint = tint,
    )

    fun draw(
        canvas: Canvas,
        dstRect: IntRect,
        tint: Color = Colors.WHITE,
        srcRect: IntRect,
    ) = draw(
        canvas = canvas,
        dstRect = dstRect.toRect(),
        tint = tint,
        srcRect = srcRect.toRect(),
    )

    sealed interface SpriteTextureData {
        data class Atlas(
            val identifier: Identifier,
            val spriteSize: IntSize,
            val atlasOffset: IntOffset,
            val atlasSize: IntSize,
        ): SpriteTextureData

        data class Native(
            val identifier: Identifier,
            val spriteSize: IntSize,
        ): SpriteTextureData
    }

    @ExpectFactory
    interface Factory {
        fun create(
            identifier: Identifier,
            width: Int,
            height: Int,
            padding: IntPadding,
        ): Texture

        fun createSprite(
            data: SpriteTextureData,
            padding: IntPadding,
        ): Texture
    }

    companion object {
        val empty = object : Texture {
            override val size: IntSize
                get() = IntSize(16)
            override val padding: IntPadding
                get() = IntPadding.ZERO

            override fun draw(
                canvas: Canvas,
                dstRect: Rect,
                tint: Color,
                srcRect: Rect
            ) = Unit
        }
    }
}

@Immutable
interface BackgroundTexture : Drawable {
    override val padding: IntPadding
        get() = IntPadding.ZERO

    override fun draw(canvas: Canvas, dstRect: IntRect, tint: Color) = draw(canvas, dstRect, tint, 1f)
    fun draw(canvas: Canvas, dstRect: IntRect, tint: Color = Colors.WHITE, scale: Float) =
        draw(canvas, dstRect.toRect(), tint, scale)

    fun draw(canvas: Canvas, dstRect: Rect, tint: Color = Colors.WHITE, scale: Float)

    @ExpectFactory
    interface Factory {
        fun create(
            namespace: String,
            id: String,
            width: Int,
            height: Int,
        ): BackgroundTexture
    }
}
