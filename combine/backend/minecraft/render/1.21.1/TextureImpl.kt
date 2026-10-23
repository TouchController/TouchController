package top.fifthlight.combine.backend.minecraft.render.v1_21_1

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.resources.ResourceLocation
import top.fifthlight.combine.backend.minecraft.identifier.v1_21_1.toMinecraft
import top.fifthlight.combine.core.data.Identifier as CombineIdentifier
import top.fifthlight.combine.core.paint.BackgroundTexture
import top.fifthlight.combine.core.paint.Canvas
import top.fifthlight.combine.core.paint.Color
import top.fifthlight.combine.core.paint.Texture
import top.fifthlight.combine.core.util.atlas.AtlasTexture
import top.fifthlight.data.*
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl

@ActualImpl(Texture::class)
sealed class TextureImpl : Texture {
    abstract val resourceLocation: ResourceLocation

    data class Normal(
        override val resourceLocation: ResourceLocation,
        override val size: IntSize,
        override val padding: IntPadding = IntPadding.ZERO,
    ) : TextureImpl() {
        override fun draw(
            canvas: Canvas,
            dstRect: Rect,
            tint: Color,
            srcRect: Rect,
        ) {
            val canvasImpl = canvas as CanvasImpl
            canvasImpl.drawTexture(
                resourceLocation = resourceLocation,
                dstRect = dstRect,
                uvRect = srcRect / size.toSize(),
                tint = tint,
            )
        }

        override fun draw(
            canvas: Canvas,
            dstRect: IntRect,
            tint: Color,
        ) {
            val guiGraphics = (canvas as CanvasImpl).guiGraphics
            RenderSystem.enableBlend()
            guiGraphics.setColor(tint.rFloat, tint.gFloat, tint.bFloat, tint.aFloat)
            guiGraphics.blit(
                resourceLocation,
                dstRect.offset.x,
                dstRect.offset.y,
                0f,
                0f,
                dstRect.size.width,
                dstRect.size.height,
                size.width,
                size.height,
            )
            guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f)
        }
    }

    data class Sprite(
        override val resourceLocation: ResourceLocation,
        override val size: IntSize,
        override val padding: IntPadding = IntPadding.ZERO,
    ) : TextureImpl() {
        override fun draw(
            canvas: Canvas,
            dstRect: Rect,
            tint: Color,
            srcRect: Rect,
        ) {
            (canvas as CanvasImpl).drawSprite(
                spriteId = resourceLocation,
                dstRect = dstRect,
                srcRect = srcRect,
                tint = tint,
            )
        }

        override fun draw(
            canvas: Canvas,
            dstRect: IntRect,
            tint: Color,
        ) {
            val guiGraphics = (canvas as CanvasImpl).guiGraphics
            RenderSystem.enableBlend()
            guiGraphics.setColor(tint.rFloat, tint.gFloat, tint.bFloat, tint.aFloat)
            guiGraphics.blitSprite(
                resourceLocation,
                dstRect.offset.x,
                dstRect.offset.y,
                dstRect.size.width,
                dstRect.size.height,
            )
            guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f)
        }
    }

    companion object : Texture.Factory {
        @ActualConstructor
        @JvmStatic
        override fun create(
            identifier: CombineIdentifier,
            width: Int,
            height: Int,
            padding: IntPadding,
        ): Texture = Normal(
            resourceLocation = identifier.toMinecraft(),
            size = IntSize(width, height),
            padding = padding,
        )

        @ActualConstructor
        @JvmStatic
        override fun createSprite(
            data: Texture.SpriteTextureData,
            padding: IntPadding,
        ): Texture = when (data) {
            is Texture.SpriteTextureData.Atlas -> AtlasTexture(Normal(
                resourceLocation = data.identifier.toMinecraft(),
                size = data.atlasSize,
                padding = padding,
            )).createPart(
                offset = data.atlasOffset,
                size = data.atlasSize,
            )

            is Texture.SpriteTextureData.Native -> Sprite(
                resourceLocation = data.identifier.toMinecraft(),
                size = data.spriteSize,
                padding = padding,
            )
        }
    }
}

@ActualImpl(BackgroundTexture::class)
data class BackgroundTextureImpl(
    val resourceLocation: ResourceLocation,
    override val size: IntSize,
) : BackgroundTexture {
    companion object : BackgroundTexture.Factory {
        @ActualConstructor
        @JvmStatic
        override fun create(
            namespace: String,
            id: String,
            width: Int,
            height: Int,
        ): BackgroundTexture = BackgroundTextureImpl(
            resourceLocation = ResourceLocation.fromNamespaceAndPath(namespace, id),
            size = IntSize(width, height),
        )
    }

    override fun draw(
        canvas: Canvas,
        dstRect: Rect,
        tint: Color,
        scale: Float,
    ) {
        val canvasImpl = canvas as CanvasImpl
        canvasImpl.drawTexture(
            resourceLocation = resourceLocation,
            dstRect = dstRect,
            uvRect = Rect(
                offset = Offset.ZERO,
                size = Size(
                    dstRect.size.width / size.width / scale,
                    dstRect.size.height / size.height / scale,
                ),
            ),
            tint = tint,
        )
    }
}
