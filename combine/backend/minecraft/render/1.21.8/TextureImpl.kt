package top.fifthlight.combine.backend.minecraft.render.v1_21_8

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.gui.render.state.GuiElementRenderState
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.resources.ResourceLocation
import org.joml.Matrix3x2f
import top.fifthlight.combine.backend.minecraft.render.v1_21_8.extension.SpriteAccessibleGuiGraphics
import top.fifthlight.combine.backend.minecraft.render.v1_21_8.extension.SubmittableGuiGraphics
import top.fifthlight.combine.paint.BackgroundTexture
import top.fifthlight.combine.paint.Canvas
import top.fifthlight.combine.paint.Color
import top.fifthlight.combine.paint.Texture
import top.fifthlight.data.IntPadding
import top.fifthlight.data.IntRect
import top.fifthlight.data.IntSize
import top.fifthlight.data.Rect
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl

private fun GuiGraphics.getSprite(resourceLocation: ResourceLocation) =
    (this as SpriteAccessibleGuiGraphics).`combine$getSprite`(resourceLocation)

private fun GuiGraphics.submitElement(guiElementRenderState: GuiElementRenderState) =
    (this as SubmittableGuiGraphics).`combine$submitElement`(guiElementRenderState)

private fun GuiGraphics.peekScissorStack() =
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

    override fun buildVertices(vertexConsumer: VertexConsumer, traverseRange: Float) {
        vertexConsumer.addVertexWith2DPose(pose, x0, y0, traverseRange).setUv(u0, v0).setColor(color)
        vertexConsumer.addVertexWith2DPose(pose, x0, y1, traverseRange).setUv(u0, v1).setColor(color)
        vertexConsumer.addVertexWith2DPose(pose, x1, y1, traverseRange).setUv(u1, v1).setColor(color)
        vertexConsumer.addVertexWith2DPose(pose, x1, y0, traverseRange).setUv(u1, v0).setColor(color)
    }

    override fun pipeline() = pipeline

    override fun textureSetup() = textureSetup

    override fun scissorArea() = scissorArea

    override fun bounds() = bounds
}

@ActualImpl(Texture::class)
data class TextureImpl(
    val resourceLocation: ResourceLocation,
    override val size: IntSize,
    override val padding: IntPadding = IntPadding.ZERO,
): Texture {
    companion object: Texture.Factory {
        @ActualConstructor
        @JvmStatic
        override fun create(
            namespace: String,
            id: String,
            width: Int,
            height: Int,
            padding: IntPadding,
        ): Texture = TextureImpl(
            resourceLocation = ResourceLocation.fromNamespaceAndPath(namespace, id),
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
        val guiGraphics = (canvas as CanvasImpl).guiGraphics
        val client = Minecraft.getInstance()
        val sprite = guiGraphics.getSprite(resourceLocation)
        val atlasLocation = sprite.atlasLocation()
        val gpuTextureView = client.textureManager.getTexture(atlasLocation).getTextureView()

        guiGraphics.submitElement(
            BlitRenderState(
                pipeline = RenderPipelines.GUI_TEXTURED,
                textureSetup = TextureSetup.singleTexture(gpuTextureView),
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
    }

    override fun draw(
        canvas: Canvas,
        dstRect: IntRect,
        tint: Color,
    ) {
        val guiGraphics = (canvas as CanvasImpl).guiGraphics
        guiGraphics.blitSprite(
            RenderPipelines.GUI_TEXTURED,
            resourceLocation,
            dstRect.offset.x,
            dstRect.offset.y,
            dstRect.size.width,
            dstRect.size.height,
            tint.value,
        )
    }
}

@ActualImpl(BackgroundTexture::class)
data class BackgroundTextureImpl(
    val resourceLocation: ResourceLocation,
    override val size: IntSize,
): BackgroundTexture {
    companion object: BackgroundTexture.Factory {
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
        val guiGraphics = (canvas as CanvasImpl).guiGraphics
        val gpuTextureView = canvas.client.textureManager.getTexture(resourceLocation).getTextureView()

        guiGraphics.submitElement(
            BlitRenderState(
                pipeline = RenderPipelines.GUI_TEXTURED,
                textureSetup = TextureSetup.singleTexture(gpuTextureView),
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
