package top.fifthlight.combine.backend.minecraft.render.v26_2

import com.mojang.blaze3d.platform.cursor.CursorType
import com.mojang.blaze3d.platform.cursor.CursorTypes
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.state.gui.GuiElementRenderState
import net.minecraft.network.chat.Component
import org.joml.Matrix3x2f
import top.fifthlight.combine.backend.minecraft.item.v26_2.toVanilla
import top.fifthlight.combine.backend.minecraft.render.v26_2.extension.SubmittableGuiGraphics
import top.fifthlight.combine.backend.minecraft.text.v26_2.toMinecraft
import top.fifthlight.combine.data.Text
import top.fifthlight.combine.input.pointer.PointerIcon
import top.fifthlight.combine.item.data.ItemStack
import top.fifthlight.combine.item.paint.ItemCanvas
import top.fifthlight.combine.paint.Color
import top.fifthlight.data.*

class CanvasImpl(val guiGraphics: GuiGraphicsExtractor) : ItemCanvas {
    private fun GuiGraphicsExtractor.addGuiElement(guiElementRenderState: GuiElementRenderState) =
        (this as SubmittableGuiGraphics).`combine$addGuiElement`(guiElementRenderState)

    private fun GuiGraphicsExtractor.peekScissorStack() =
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
        guiGraphics.addGuiElement(
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
        guiGraphics.outline(offset.x, offset.y, size.width, size.height, color.value)
    }

    override fun drawText(
        offset: IntOffset,
        text: String,
        color: Color,
    ) {
        guiGraphics.text(font, text, offset.x, offset.y, color.value, false)
    }

    override fun drawText(
        offset: IntOffset,
        width: Int,
        text: String,
        color: Color,
    ) {
        guiGraphics.textWithWordWrap(font, Component.literal(text), offset.x, offset.y, width, color.value, false)
    }

    override fun drawText(
        offset: IntOffset,
        text: Text,
        color: Color,
    ) {
        guiGraphics.text(font, text.toMinecraft(), offset.x, offset.y, color.value, false)
    }

    override fun drawText(
        offset: IntOffset,
        width: Int,
        text: Text,
        color: Color,
    ) {
        guiGraphics.textWithWordWrap(font, text.toMinecraft(), offset.x, offset.y, width, color.value, false)
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
        guiGraphics.item(minecraftStack, offset.x, offset.y)
        popState()
    }

    private fun mapPointer(pointer: PointerIcon) = when (pointer) {
        PointerIcon.Arrow -> CursorTypes.ARROW
        PointerIcon.Edit -> CursorTypes.IBEAM
        PointerIcon.Crosshair -> CursorTypes.CROSSHAIR
        PointerIcon.PointingHand -> CursorTypes.POINTING_HAND
        PointerIcon.ResizeVertical -> CursorTypes.RESIZE_NS
        PointerIcon.ResizeHorizonal -> CursorTypes.RESIZE_EW
        PointerIcon.ResizeAll -> CursorTypes.RESIZE_ALL
        PointerIcon.NotAllowed -> CursorTypes.NOT_ALLOWED
        else -> CursorType.DEFAULT
    }

    override fun requestPointerIcon(pointer: PointerIcon) {
        guiGraphics.requestCursor(mapPointer(pointer))
    }
}
