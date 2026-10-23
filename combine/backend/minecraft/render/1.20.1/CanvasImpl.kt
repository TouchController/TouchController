package top.fifthlight.combine.backend.minecraft.render.v1_20_1

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferUploader
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.ShaderInstance
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import org.joml.Quaternionf
import top.fifthlight.combine.backend.minecraft.item.v1_20_1.toVanilla
import top.fifthlight.combine.backend.minecraft.text.v1_20_1.toMinecraft
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.paint.Color
import top.fifthlight.combine.item.data.ItemStack
import top.fifthlight.combine.item.paint.ItemCanvas
import top.fifthlight.data.*
import java.util.function.Supplier

class CanvasImpl(val guiGraphics: GuiGraphics) : ItemCanvas {
    val client: Minecraft
        get() = Minecraft.getInstance()
    private val font: Font
        get() = client.font

    override fun pushState() {
        guiGraphics.pose().pushPose()
    }

    override fun popState() {
        guiGraphics.pose().popPose()
    }

    override fun translate(x: Int, y: Int) {
        guiGraphics.pose().translate(x.toFloat(), y.toFloat(), 0f)
    }

    override fun translate(x: Float, y: Float) {
        guiGraphics.pose().translate(x, y, 0f)
    }

    override fun rotate(degrees: Float) {
        guiGraphics.pose().mulPose(Quaternionf().rotateZ(Math.toRadians(degrees.toDouble()).toFloat()))
    }

    override fun scale(x: Float, y: Float) {
        guiGraphics.pose().scale(x, y, 1f)
    }

    override fun fillRect(
        offset: IntOffset,
        size: IntSize,
        color: Color,
    ) {
        guiGraphics.fill(offset.x, offset.y, offset.x + size.width, offset.y + size.height, color.value)
    }

    override fun fillGradientRect(
        offset: Offset,
        size: Size,
        leftTopColor: Color,
        leftBottomColor: Color,
        rightTopColor: Color,
        rightBottomColor: Color,
    ) {
        val x0 = offset.x
        val y0 = offset.y
        val x1 = offset.x + size.width
        val y1 = offset.y + size.height
        val matrix = guiGraphics.pose().last().pose()
        val vertexConsumer = guiGraphics.bufferSource().getBuffer(RenderType.gui())
        vertexConsumer.vertex(matrix, x0, y0, 0f).color(leftTopColor.value).endVertex()
        vertexConsumer.vertex(matrix, x0, y1, 0f).color(leftBottomColor.value).endVertex()
        vertexConsumer.vertex(matrix, x1, y1, 0f).color(rightBottomColor.value).endVertex()
        vertexConsumer.vertex(matrix, x1, y0, 0f).color(rightTopColor.value).endVertex()
    }

    override fun drawRect(
        offset: IntOffset,
        size: IntSize,
        color: Color,
    ) {
        guiGraphics.renderOutline(offset.x, offset.y, size.width, size.height, color.value)
    }

    override fun drawText(
        offset: IntOffset,
        text: String,
        color: Color,
    ) {
        guiGraphics.drawString(font, text, offset.x, offset.y, color.value, false)
    }

    override fun drawText(
        offset: IntOffset,
        width: Int,
        text: String,
        color: Color,
    ) {
        guiGraphics.drawWordWrap(font, Component.literal(text), offset.x, offset.y, width, color.value)
    }

    override fun drawText(
        offset: IntOffset,
        text: Text,
        color: Color,
    ) {
        guiGraphics.drawString(font, text.toMinecraft(), offset.x, offset.y, color.value, false)
    }

    override fun drawText(
        offset: IntOffset,
        width: Int,
        text: Text,
        color: Color,
    ) {
        guiGraphics.drawWordWrap(font, text.toMinecraft(), offset.x, offset.y, width, color.value)
    }

    override fun pushClip(absoluteArea: IntRect, relativeArea: IntRect) {
        guiGraphics.enableScissor(absoluteArea.left, absoluteArea.top, absoluteArea.right, absoluteArea.bottom)
    }

    override fun popClip() {
        guiGraphics.disableScissor()
    }

    override fun drawItemStack(
        offset: IntOffset,
        size: IntSize,
        stack: ItemStack,
    ) {
        val minecraftStack = stack.toVanilla()
        pushState()
        guiGraphics.pose().scale(size.width.toFloat() / 16f, size.height.toFloat() / 16f, 1f)
        guiGraphics.renderItem(minecraftStack, offset.x, offset.y)
        popState()
    }

    fun drawTexture(
        resourceLocation: ResourceLocation,
        dstRect: Rect,
        uvRect: Rect,
        tint: Color,
    ) {
        // TODO: Rewrite to RenderType?
        guiGraphics.flush()
        RenderSystem.setShaderTexture(0, resourceLocation)
        withShader({ GameRenderer.getPositionTexColorShader()!! }) {
            val matrix = guiGraphics.pose().last().pose()
            val bufferBuilder = Tesselator.getInstance().builder
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR)
            bufferBuilder
                .vertex(matrix, dstRect.left, dstRect.top, 0f)
                .uv(uvRect.left, uvRect.top)
                .color(tint.value)
                .endVertex()
            bufferBuilder
                .vertex(matrix, dstRect.left, dstRect.bottom, 0f)
                .uv(uvRect.left, uvRect.bottom)
                .color(tint.value)
                .endVertex()
            bufferBuilder
                .vertex(matrix, dstRect.right, dstRect.bottom, 0f)
                .uv(uvRect.right, uvRect.bottom)
                .color(tint.value)
                .endVertex()
            bufferBuilder
                .vertex(matrix, dstRect.right, dstRect.top, 0f)
                .uv(uvRect.right, uvRect.top)
                .color(tint.value)
                .endVertex()
            RenderSystem.enableBlend()
            BufferUploader.drawWithShader(bufferBuilder.end())
        }
    }
}

private inline fun withShader(program: Supplier<ShaderInstance>, crossinline block: () -> Unit) {
    val originalShader = RenderSystem.getShader()
    RenderSystem.setShader(program)
    block()
    originalShader?.let {
        RenderSystem.setShader { originalShader }
    }
}
