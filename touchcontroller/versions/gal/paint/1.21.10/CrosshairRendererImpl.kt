/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.gal.paint.v1_21_10

import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.DestFactor
import com.mojang.blaze3d.platform.SourceFactor
import com.mojang.blaze3d.shaders.UniformType
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.gui.render.state.GuiElementRenderState
import org.joml.Matrix3x2f
import top.fifthlight.combine.backend.minecraft.render.v1_21_10.CanvasImpl
import top.fifthlight.combine.backend.minecraft.render.v1_21_10.extension.SubmittableGuiGraphics
import top.fifthlight.combine.backend.minecraft.identifier.v1_21_10.toMinecraft
import top.fifthlight.combine.data.Identifier
import top.fifthlight.combine.paint.Canvas
import top.fifthlight.combine.paint.Colors
import top.fifthlight.data.Offset
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.buildinfo.BuildInfo
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

private val CROSSHAIR_SNIPPET = RenderPipeline.builder()
    .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
    .withUniform("Projection", UniformType.UNIFORM_BUFFER)
    .withVertexShader("core/gui")
    .withFragmentShader("core/gui")
    .withBlend(
        BlendFunction(
            SourceFactor.ONE_MINUS_DST_COLOR,
            DestFactor.ONE_MINUS_SRC_COLOR,
            SourceFactor.ONE,
            DestFactor.ZERO
        )
    )
    .buildSnippet()

private class CrosshairOuterGuiElementRenderState(
    val pose: Matrix3x2f,
    val radius: Int = 36,
    val outerRadius: Int = 2,
) : GuiElementRenderState {
    companion object {
        private val CROSSHAIR_OUTER_PIPELINE: RenderPipeline = RenderPipeline.builder(CROSSHAIR_SNIPPET)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
            .withLocation(Identifier.of(BuildInfo.MOD_ID, "pipeline/crosshair_outer").toMinecraft())
            .build()
    }

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

    override fun pipeline() = CROSSHAIR_OUTER_PIPELINE

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
    companion object {
        private val CROSSHAIR_INNER_PIPELINE: RenderPipeline = RenderPipeline.builder(CROSSHAIR_SNIPPET)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
            .withLocation(Identifier.of(BuildInfo.MOD_ID, "pipeline/crosshair_inner").toMinecraft())
            .build()
    }

    override fun buildVertices(vertexConsumer: VertexConsumer) {
        val scale = radius * progress
        for (i in 0 until CROSSHAIR_CIRCLE_PARTS) {
            val angle0 = -i * CROSSHAIR_CIRCLE_ANGLE
            val angle1 = - (i+1) * CROSSHAIR_CIRCLE_ANGLE
            val p0 = point(angle0, scale)
            val p1 = point(angle1, scale)
            vertexConsumer.addVertexWith2DPose(pose, 0f, 0f).setColor(Colors.WHITE.value)
            vertexConsumer.addVertexWith2DPose(pose, p0.x, p0.y).setColor(Colors.WHITE.value)
            vertexConsumer.addVertexWith2DPose(pose, p1.x, p1.y).setColor(Colors.WHITE.value)
            vertexConsumer.addVertexWith2DPose(pose, 0f, 0f).setColor(Colors.WHITE.value)
        }
    }

    override fun pipeline() = CROSSHAIR_INNER_PIPELINE

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

    private fun GuiGraphics.submitElement(guiElementRenderState: GuiElementRenderState) =
        (this as SubmittableGuiGraphics).`combine$submitElement`(guiElementRenderState)

    override fun renderOuter(canvas: Canvas, radius: Int, outerRadius: Int) {
        val drawContext = (canvas as CanvasImpl).guiGraphics
        drawContext.submitElement(
            CrosshairOuterGuiElementRenderState(
                pose = Matrix3x2f(drawContext.pose()),
                radius = radius,
                outerRadius = outerRadius,
            )
        )
    }

    override fun renderInner(canvas: Canvas, radius: Int, outerRadius: Int, initialProgress: Float, progress: Float) {
        val drawContext = (canvas as CanvasImpl).guiGraphics
        drawContext.submitElement(CrosshairInnerGuiElementRenderState(Matrix3x2f(drawContext.pose()), radius, progress))
    }
}
