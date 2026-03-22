package top.fifthlight.combine.backend.minecraft.render.v1_21_8

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.gui.render.state.GuiElementRenderState
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import org.joml.Matrix3x2f
import top.fifthlight.combine.backend.minecraft.item.v1_21_8.toVanilla
import top.fifthlight.combine.backend.minecraft.render.v1_21_8.extension.SubmittableGuiGraphics
import top.fifthlight.combine.backend.minecraft.text.v1_21_8.toMinecraft
import top.fifthlight.combine.data.Text
import top.fifthlight.combine.item.data.ItemStack
import top.fifthlight.combine.item.paint.ItemCanvas
import top.fifthlight.combine.paint.Color
import top.fifthlight.data.*

class CanvasImpl(val guiGraphics: GuiGraphics) : ItemCanvas {
    private fun GuiGraphics.submitElement(guiElementRenderState: GuiElementRenderState) =
        (this as SubmittableGuiGraphics).`combine$submitElement`(guiElementRenderState)

    private fun GuiGraphics.peekScissorStack() =
        (this as SubmittableGuiGraphics).`combine$peekScissorStack`()

    val client: Minecraft
        get() = Minecraft.getInstance()
    private val font: Font
        get() = client.font

    override fun pushState() {
        guiGraphics.pose().pushMatrix()
    }

    override fun popState() {
        guiGraphics.pose().popMatrix()
    }

    override fun translate(x: Int, y: Int) {
        guiGraphics.pose().translate(x.toFloat(), y.toFloat())
    }

    override fun translate(x: Float, y: Float) {
        guiGraphics.pose().translate(x, y)
    }

    override fun rotate(degrees: Float) {
        guiGraphics.pose().rotate(Math.toRadians(degrees.toDouble()).toFloat())
    }

    override fun scale(x: Float, y: Float) {
        guiGraphics.pose().scale(x, y)
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
        guiGraphics.submitElement(
            GradientRectangleRenderState(
                pipeline = RenderPipelines.GUI,
                textureSetup = TextureSetup.noTexture(),
                pose = Matrix3x2f(guiGraphics.pose()),
                x0 = offset.x,
                y0 = offset.y,
                x1 = offset.x + size.width,
                y1 = offset.y + size.height,
                leftTopColor = leftTopColor,
                leftBottomColor = leftBottomColor,
                rightTopColor = rightTopColor,
                rightBottomColor = rightBottomColor,
                screenRectangle = guiGraphics.peekScissorStack(),
            )
        )
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
        guiGraphics.drawWordWrap(font, Component.literal(text), offset.x, offset.y, width, color.value, false)
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
        guiGraphics.drawWordWrap(font, text.toMinecraft(), offset.x, offset.y, width, color.value, false)
    }

    override fun pushClip(absoluteArea: IntRect, relativeArea: IntRect) {
        guiGraphics.enableScissor(relativeArea.left, relativeArea.top, relativeArea.right, relativeArea.bottom)
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
        guiGraphics.pose().scale(size.width.toFloat() / 16f, size.height.toFloat() / 16f)
        guiGraphics.renderItem(minecraftStack, offset.x, offset.y)
        popState()
    }
}
