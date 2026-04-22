package top.fifthlight.combine.backend.minecraft.render.v26_2

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.renderer.state.gui.GuiElementRenderState
import org.joml.Matrix3x2f
import top.fifthlight.combine.paint.Color

internal data class GradientRectangleRenderState(
    val pipeline: RenderPipeline,
    val textureSetup: TextureSetup,
    val pose: Matrix3x2f,
    val x0: Float,
    val y0: Float,
    val x1: Float,
    val y1: Float,
    val leftTopColor: Int,
    val leftBottomColor: Int,
    val rightTopColor: Int,
    val rightBottomColor: Int,
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
        leftTopColor: Color,
        leftBottomColor: Color,
        rightTopColor: Color,
        rightBottomColor: Color,
        screenRectangle: ScreenRectangle?,
    ) : this(
        pipeline = pipeline,
        textureSetup = textureSetup,
        pose = pose,
        x0 = x0,
        y0 = y0,
        x1 = x1,
        y1 = y1,
        leftTopColor = leftTopColor.value,
        leftBottomColor = leftBottomColor.value,
        rightTopColor = rightTopColor.value,
        rightBottomColor = rightBottomColor.value,
        scissorArea = screenRectangle,
        bounds = GuiElementUtil.getBounds(x0, y0, x1, y1, pose, screenRectangle),
    )

    override fun buildVertices(vertexConsumer: VertexConsumer) {
        vertexConsumer.addVertexWith2DPose(pose, x0, y0).setColor(leftTopColor)
        vertexConsumer.addVertexWith2DPose(pose, x0, y1).setColor(leftBottomColor)
        vertexConsumer.addVertexWith2DPose(pose, x1, y1).setColor(rightBottomColor)
        vertexConsumer.addVertexWith2DPose(pose, x1, y0).setColor(rightTopColor)
    }

    override fun pipeline() = pipeline

    override fun textureSetup() = textureSetup

    override fun scissorArea() = scissorArea

    override fun bounds() = bounds
}
