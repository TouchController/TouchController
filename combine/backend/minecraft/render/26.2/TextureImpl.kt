package top.fifthlight.combine.backend.minecraft.render.v26_2

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.state.gui.GuiElementRenderState
import net.minecraft.resources.Identifier
import org.joml.Matrix3x2f
import top.fifthlight.combine.backend.minecraft.render.v26_2.extension.SpriteAccessibleGuiGraphics
import top.fifthlight.combine.backend.minecraft.render.v26_2.extension.SubmittableGuiGraphics
import top.fifthlight.combine.core.paint.BackgroundTexture
import top.fifthlight.combine.core.paint.Canvas
import top.fifthlight.combine.core.paint.Color
import top.fifthlight.combine.core.paint.Texture
import top.fifthlight.data.IntPadding
import top.fifthlight.data.IntRect
import top.fifthlight.data.IntSize
import top.fifthlight.data.Rect
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl

private fun GuiGraphicsExtractor.getSprite(identifier: Identifier) =
    (this as SpriteAccessibleGuiGraphics).`combine$getSprite`(identifier)

private fun GuiGraphicsExtractor.submitElement(guiElementRenderState: GuiElementRenderState) =
    (this as SubmittableGuiGraphics).`combine$addGuiElement`(guiElementRenderState)

private fun GuiGraphicsExtractor.peekScissorStack() =
    (this as SubmittableGuiGraphics).`combine$peekScissorStack`()

private data class BlitRenderState(
    val pipeline: RenderPipeline,
    val textureSetup: TextureSetup,
    val pose: Matrix3x2f,
    val x0: Float,
    val y0: Float,
    val x1: Float,
    val y1: Float,
    val u0: Float,
    val u1: Float,
    val v0: Float,
    val v1: Float,
    val color: Int,
    val scissorArea: ScreenRectangle?,
    val bounds: ScreenRectangle?,
) : GuiElementRenderState {
    constructor(
        pipeline: RenderPipeline,
        textureSetup: TextureSetup,
        pose: Matrix3x2f,
        x0: Float,
        y0: Float,
        x1: Float,
        y1: Float,
        u0: Float,
        u1: Float,
        v0: Float,
        v1: Float,
        color: Int,
        screenRectangle: ScreenRectangle?,
    ) : this(
        pipeline = pipeline,
        textureSetup = textureSetup,
        pose = pose,
        x0 = x0,
        y0 = y0,
        x1 = x1,
        y1 = y1,
        u0 = u0,
        u1 = u1,
        v0 = v0,
        v1 = v1,
        color = color,
        scissorArea = screenRectangle,
        bounds = GuiElementUtil.getBounds(x0, y0, x1, y1, pose, screenRectangle)
    )

    override fun buildVertices(vertexConsumer: VertexConsumer) {
        vertexConsumer.addVertexWith2DPose(pose, x0, y0).setUv(u0, v0).setColor(color)
        vertexConsumer.addVertexWith2DPose(pose, x0, y1).setUv(u0, v1).setColor(color)
        vertexConsumer.addVertexWith2DPose(pose, x1, y1).setUv(u1, v1).setColor(color)
        vertexConsumer.addVertexWith2DPose(pose, x1, y0).setUv(u1, v0).setColor(color)
    }

    override fun pipeline() = pipeline

    override fun textureSetup() = textureSetup

    override fun scissorArea() = scissorArea

    override fun bounds() = bounds
}

@ActualImpl(Texture::class)
data class TextureImpl(
    val identifier: Identifier,
    val sprite: Boolean,
    override val size: IntSize,
    override val padding: IntPadding = IntPadding.ZERO,
) : Texture {
    companion object : Texture.Factory {
        @ActualConstructor
        @JvmStatic
        override fun create(
            namespace: String,
            id: String,
            width: Int,
            height: Int,
            padding: IntPadding,
        ): Texture = TextureImpl(
            identifier = Identifier.fromNamespaceAndPath(namespace, id),
            sprite = false,
            size = IntSize(width, height),
            padding = padding,
        )

        @ActualConstructor
        @JvmStatic
        override fun createSprite(
            namespace: String,
            id: String,
            width: Int,
            height: Int,
            padding: IntPadding,
        ): Texture = TextureImpl(
            identifier = Identifier.fromNamespaceAndPath(namespace, id),
            sprite = true,
            size = IntSize(width, height),
            padding = padding,
        )
    }

    override fun draw(
        canvas: Canvas,
        dstRect: Rect,
        tint: Color,
        srcRect: Rect,
    ) {
        val guiGraphics = (canvas as CanvasImpl).guiGraphics ?: return
        val client = Minecraft.getInstance()
        if (sprite) {
            val sprite = guiGraphics.getSprite(identifier)
            val atlasLocation = sprite.atlasLocation()
            val texture = client.textureManager.getTexture(atlasLocation)

            guiGraphics.submitElement(
                BlitRenderState(
                    pipeline = RenderPipelines.GUI_TEXTURED,
                    textureSetup = TextureSetup.singleTexture(texture.textureView, texture.sampler),
                    pose = Matrix3x2f(guiGraphics.pose()),
                    x0 = dstRect.offset.x,
                    y0 = dstRect.offset.y,
                    x1 = dstRect.offset.x + dstRect.size.width,
                    y1 = dstRect.offset.y + dstRect.size.height,
                    u0 = sprite.getU(srcRect.offset.x / sprite.contents().width()),
                    u1 = sprite.getU((srcRect.offset.x + srcRect.size.width) / sprite.contents().width()),
                    v0 = sprite.getV(srcRect.offset.y / sprite.contents().height()),
                    v1 = sprite.getV((srcRect.offset.y + srcRect.size.height) / sprite.contents().height()),
                    color = tint.value,
                    screenRectangle = guiGraphics.peekScissorStack(),
                )
            )
        } else {
            val texture = client.textureManager.getTexture(identifier)
            guiGraphics.submitElement(
                BlitRenderState(
                    pipeline = RenderPipelines.GUI_TEXTURED,
                    textureSetup = TextureSetup.singleTexture(texture.textureView, texture.sampler),
                    pose = Matrix3x2f(guiGraphics.pose()),
                    x0 = dstRect.offset.x,
                    y0 = dstRect.offset.y,
                    x1 = dstRect.offset.x + dstRect.size.width,
                    y1 = dstRect.offset.y + dstRect.size.height,
                    u0 = srcRect.offset.x / size.width,
                    u1 = (srcRect.offset.x + srcRect.size.width) / size.width,
                    v0 = srcRect.offset.y / size.height,
                    v1 = (srcRect.offset.y + srcRect.size.height) / size.height,
                    color = tint.value,
                    screenRectangle = guiGraphics.peekScissorStack(),
                )
            )
        }
    }

    override fun draw(
        canvas: Canvas,
        dstRect: IntRect,
        tint: Color,
    ) {
        val guiGraphics = (canvas as CanvasImpl).guiGraphics ?: return
        if (sprite) {
            guiGraphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                identifier,
                dstRect.offset.x,
                dstRect.offset.y,
                dstRect.size.width,
                dstRect.size.height,
                tint.value,
            )
        } else {
            guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                identifier,
                dstRect.offset.x,
                dstRect.offset.y,
                0f,
                0f,
                dstRect.size.width,
                dstRect.size.height,
                size.width,
                size.height,
                tint.value,
            )
        }
    }
}

@ActualImpl(BackgroundTexture::class)
data class BackgroundTextureImpl(
    val identifier: Identifier,
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
            identifier = Identifier.fromNamespaceAndPath(namespace, id),
            size = IntSize(width, height),
        )
    }

    override fun draw(
        canvas: Canvas,
        dstRect: Rect,
        tint: Color,
        scale: Float,
    ) {
        val guiGraphics = (canvas as CanvasImpl).guiGraphics ?: return
        val texture = canvas.client.textureManager.getTexture(identifier)
        guiGraphics.submitElement(
            BlitRenderState(
                pipeline = RenderPipelines.GUI_TEXTURED,
                textureSetup = TextureSetup.singleTexture(texture.textureView, texture.sampler),
                pose = Matrix3x2f(guiGraphics.pose()),
                x0 = dstRect.offset.x,
                y0 = dstRect.offset.y,
                x1 = dstRect.offset.x + dstRect.size.width,
                y1 = dstRect.offset.y + dstRect.size.height,
                u0 = 0f,
                u1 = dstRect.size.width / size.width / scale,
                v0 = 0f,
                v1 = dstRect.size.height / size.height / scale,
                color = tint.value,
                screenRectangle = guiGraphics.peekScissorStack(),
            )
        )
    }
}
