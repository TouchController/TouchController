/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.gal.paint.v1_20_1

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferUploader
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.renderer.GameRenderer
import top.fifthlight.combine.backend.minecraft.render.v1_20_1.CanvasImpl
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

private fun point(angle: Float, radius: Float) =
    Offset(x = cos(angle) * radius, y = sin(angle) * radius)

@ActualImpl(CrosshairRenderer::class)
object CrosshairRendererImpl : CrosshairRenderer {
    @JvmStatic
    @ActualConstructor
    fun of(): CrosshairRenderer = this

    override fun renderOuter(canvas: Canvas, radius: Int, outerRadius: Int) {
        val drawContext = (canvas as CanvasImpl).guiGraphics
        val originalShader = RenderSystem.getShader()
        RenderSystem.setShader { GameRenderer.getPositionColorShader()!! }
        try {
            val matrix = drawContext.pose().last().pose()
            val bufferBuilder = Tesselator.getInstance().builder
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR)
            val innerRadius = radius.toFloat()
            val outerRadius = (radius + outerRadius).toFloat()
            var angle = -PI.toFloat() / 2f
            for (i in 0 until CROSSHAIR_CIRCLE_PARTS) {
                val endAngle = angle + CROSSHAIR_CIRCLE_ANGLE
                val point0 = point(angle, outerRadius)
                val point1 = point(endAngle, outerRadius)
                val point2 = point(angle, innerRadius)
                val point3 = point(endAngle, innerRadius)
                angle = endAngle

                bufferBuilder.vertex(matrix, point0.x, point0.y, 0f).color(Colors.WHITE.value).endVertex()
                bufferBuilder.vertex(matrix, point2.x, point2.y, 0f).color(Colors.WHITE.value).endVertex()
                bufferBuilder.vertex(matrix, point3.x, point3.y, 0f).color(Colors.WHITE.value).endVertex()
                bufferBuilder.vertex(matrix, point1.x, point1.y, 0f).color(Colors.WHITE.value).endVertex()
            }

            RenderSystem.enableBlend()
            RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
                GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO,
            )
            BufferUploader.drawWithShader(bufferBuilder.end())
            RenderSystem.defaultBlendFunc()
        } finally {
            originalShader?.let { RenderSystem.setShader { originalShader } }
        }
    }

    override fun renderInner(
        canvas: Canvas,
        radius: Int,
        outerRadius: Int,
        initialProgress: Float,
        progress: Float,
    ) {
        val drawContext = (canvas as CanvasImpl).guiGraphics
        val originalShader = RenderSystem.getShader()
        RenderSystem.setShader { GameRenderer.getPositionColorShader()!! }
        try {
            val matrix = drawContext.pose().last().pose()
            val bufferBuilder = Tesselator.getInstance().builder
            bufferBuilder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR)
            val scale = radius * progress
            for (i in 0 until CROSSHAIR_CIRCLE_PARTS) {
                val angle0 = -i * CROSSHAIR_CIRCLE_ANGLE
                val angle1 = -(i + 1) * CROSSHAIR_CIRCLE_ANGLE
                val p0 = point(angle0, scale)
                val p1 = point(angle1, scale)
                bufferBuilder.vertex(matrix, 0f, 0f, 0f).color(Colors.WHITE.value).endVertex()
                bufferBuilder.vertex(matrix, p0.x, p0.y, 0f).color(Colors.WHITE.value).endVertex()
                bufferBuilder.vertex(matrix, p1.x, p1.y, 0f).color(Colors.WHITE.value).endVertex()
            }

            RenderSystem.enableBlend()
            RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
                GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO,
            )
            BufferUploader.drawWithShader(bufferBuilder.end())
            RenderSystem.defaultBlendFunc()
        } finally {
            originalShader?.let { RenderSystem.setShader { originalShader } }
        }
    }
}
