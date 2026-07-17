/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.gal.paint.v26_2

import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.state.gui.GuiElementRenderState
import org.joml.Matrix3x2f
import top.fifthlight.combine.backend.minecraft.render.v26_2.CanvasImpl
import top.fifthlight.combine.backend.minecraft.render.v26_2.extension.SubmittableGuiGraphics
import top.fifthlight.combine.core.paint.Canvas
import top.fifthlight.combine.core.paint.Colors
import top.fifthlight.data.Offset
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.common.gal.paint.CrosshairRenderer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val CROSSHAIR_CIRCLE_PARTS = 24
private const val CROSSHAIR_CIRCLE_ANGLE = 2 * PI.toFloat() / CROSSHAIR_CIRCLE_PARTS

private fun point(angle: Float, radius: Float) = Offset(
    x = cos(angle) * radius,
    y = sin(angle) * radius
)

private class CrosshairOuterGuiElementRenderState(
    val pose: Matrix3x2f,
    val radius: Int = 36,
    val outerRadius: Int = 2,
) : GuiElementRenderState {
    override fun buildVertices(vertexConsumer: VertexConsumer) {
        val innerRadius = radius.toFloat()
        val outerRadius = (radius + outerRadius).toFloat()
        var angle = -PI.toFloat() / 2f
        repeat(CROSSHAIR_CIRCLE_PARTS) {
            val endAngle = angle + CROSSHAIR_CIRCLE_ANGLE
            val point0 = point(angle, outerRadius)
            val point1 = point(endAngle, outerRadius)
            val point2 = point(angle, innerRadius)
            val point3 = point(endAngle, innerRadius)
            angle = endAngle

            with(vertexConsumer) {
                addVertexWith2DPose(pose, point0.x, point0.y).setColor(Colors.WHITE.value)
                addVertexWith2DPose(pose, point2.x, point2.y).setColor(Colors.WHITE.value)
                addVertexWith2DPose(pose, point3.x, point3.y).setColor(Colors.WHITE.value)
                addVertexWith2DPose(pose, point1.x, point1.y).setColor(Colors.WHITE.value)
            }
        }
    }

    override fun pipeline() = RenderPipelines.GUI_INVERT

    override fun textureSetup(): TextureSetup = TextureSetup.noTexture()

    override fun scissorArea() = null

    private val bounds by lazy {
        val totalRadius = radius + outerRadius
        ScreenRectangle(-totalRadius, -totalRadius, totalRadius, totalRadius)
    }

    override fun bounds() = bounds
}

private class CrosshairInnerGuiElementRenderState(
    val pose: Matrix3x2f,
    val radius: Int = 36,
    val progress: Float,
) : GuiElementRenderState {
    override fun buildVertices(vertexConsumer: VertexConsumer) {
        val scale = radius * progress
        for (i in 0 until CROSSHAIR_CIRCLE_PARTS) {
            val angle0 = -i * CROSSHAIR_CIRCLE_ANGLE
            val angle1 = -(i + 1) * CROSSHAIR_CIRCLE_ANGLE
            val p0 = point(angle0, scale)
            val p1 = point(angle1, scale)
            vertexConsumer.addVertexWith2DPose(pose, 0f, 0f).setColor(Colors.WHITE.value)
            vertexConsumer.addVertexWith2DPose(pose, p0.x, p0.y).setColor(Colors.WHITE.value)
            vertexConsumer.addVertexWith2DPose(pose, p1.x, p1.y).setColor(Colors.WHITE.value)
            vertexConsumer.addVertexWith2DPose(pose, 0f, 0f).setColor(Colors.WHITE.value)
        }
    }

    override fun pipeline() = RenderPipelines.GUI_INVERT

    override fun textureSetup(): TextureSetup = TextureSetup.noTexture()

    override fun scissorArea() = null

    private val bounds by lazy {
        ScreenRectangle(-radius, -radius, radius, radius)
    }

    override fun bounds() = bounds
}

@ActualImpl(CrosshairRenderer::class)
object CrosshairRendererImpl : CrosshairRenderer {
    @JvmStatic
    @ActualConstructor
    fun of(): CrosshairRenderer = this

    private fun GuiGraphicsExtractor.addGuiElement(guiElementRenderState: GuiElementRenderState) =
        (this as SubmittableGuiGraphics).`combine$addGuiElement`(guiElementRenderState)

    override fun renderOuter(canvas: Canvas, radius: Int, outerRadius: Int) {
        val drawContext = (canvas as CanvasImpl).guiGraphics
        drawContext?.addGuiElement(
            CrosshairOuterGuiElementRenderState(
                pose = Matrix3x2f(drawContext.pose()),
                radius = radius,
                outerRadius = outerRadius,
            )
        )
    }

    override fun renderInner(canvas: Canvas, radius: Int, outerRadius: Int, initialProgress: Float, progress: Float) {
        val drawContext = (canvas as CanvasImpl).guiGraphics
        drawContext?.addGuiElement(CrosshairInnerGuiElementRenderState(Matrix3x2f(drawContext.pose()), radius, progress))
    }
}
