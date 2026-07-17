package top.fifthlight.combine.backend.minecraft.render.v26_2

import com.mojang.blaze3d.platform.cursor.CursorType
import com.mojang.blaze3d.platform.cursor.CursorTypes
import it.unimi.dsi.fastutil.booleans.BooleanArrayList
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
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.input.pointer.PointerIcon
import top.fifthlight.combine.core.paint.Color
import top.fifthlight.combine.item.data.ItemStack
import top.fifthlight.combine.item.paint.ItemCanvas
import top.fifthlight.data.*

class CanvasImpl(graphics: GuiGraphicsExtractor) : ItemCanvas {
    private fun GuiGraphicsExtractor.addGuiElement(guiElementRenderState: GuiElementRenderState) =
        (this as SubmittableGuiGraphics).`combine$addGuiElement`(guiElementRenderState)

    private fun GuiGraphicsExtractor.peekScissorStack() =
        (this as SubmittableGuiGraphics).`combine$peekScissorStack`()

    private val graphics = graphics
        get() =
            field
    val client: Minecraft
        get() = Minecraft.getInstance()
    private val font: Font
        get() = client.font

    private val disableRenderingStack = BooleanArrayList(64)
    private val disableRendering
        get() = disableRenderingStack.isNotEmpty() && disableRenderingStack.getBoolean(disableRenderingStack.size - 1)

    val guiGraphics: GuiGraphicsExtractor?
        get() = graphics.takeIf { !disableRendering }

    override fun pushState() {
        graphics.pose().pushMatrix()
    }

    override fun popState() {
        graphics.pose().popMatrix()
    }

    override fun translate(x: Int, y: Int) {
        graphics.pose().translate(x.toFloat(), y.toFloat())
    }

    override fun translate(x: Float, y: Float) {
        graphics.pose().translate(x, y)
    }

    override fun rotate(degrees: Float) {
        graphics.pose().rotate(Math.toRadians(degrees.toDouble()).toFloat())
    }

    override fun scale(x: Float, y: Float) {
        graphics.pose().scale(x, y)
    }

    override fun fillRect(
        offset: IntOffset,
        size: IntSize,
        color: Color,
    ) {
        if (disableRendering) {
            return
        }
        graphics.fill(offset.x, offset.y, offset.x + size.width, offset.y + size.height, color.value)
    }

    override fun fillGradientRect(
        offset: Offset,
        size: Size,
        leftTopColor: Color,
        leftBottomColor: Color,
        rightTopColor: Color,
        rightBottomColor: Color,
    ) {
        if (disableRendering) {
            return
        }
        graphics.addGuiElement(
            GradientRectangleRenderState(
                pipeline = RenderPipelines.GUI,
                textureSetup = TextureSetup.noTexture(),
                pose = Matrix3x2f(graphics.pose()),
                x0 = offset.x,
                y0 = offset.y,
                x1 = offset.x + size.width,
                y1 = offset.y + size.height,
                leftTopColor = leftTopColor,
                leftBottomColor = leftBottomColor,
                rightTopColor = rightTopColor,
                rightBottomColor = rightBottomColor,
                screenRectangle = graphics.peekScissorStack(),
            )
        )
    }

    override fun drawRect(
        offset: IntOffset,
        size: IntSize,
        color: Color,
    ) {
        if (disableRendering) {
            return
        }
        graphics.outline(offset.x, offset.y, size.width, size.height, color.value)
    }

    override fun drawText(
        offset: IntOffset,
        text: String,
        color: Color,
    ) {
        if (disableRendering) {
            return
        }
        graphics.text(font, text, offset.x, offset.y, color.value, false)
    }

    override fun drawText(
        offset: IntOffset,
        width: Int,
        text: String,
        color: Color,
    ) {
        if (disableRendering) {
            return
        }
        graphics.textWithWordWrap(font, Component.literal(text), offset.x, offset.y, width, color.value, false)
    }

    override fun drawText(
        offset: IntOffset,
        text: Text,
        color: Color,
    ) {
        if (disableRendering) {
            return
        }
        graphics.text(font, text.toMinecraft(), offset.x, offset.y, color.value, false)
    }

    override fun drawText(
        offset: IntOffset,
        width: Int,
        text: Text,
        color: Color,
    ) {
        if (disableRendering) {
            return
        }
        graphics.textWithWordWrap(font, text.toMinecraft(), offset.x, offset.y, width, color.value, false)
    }

    override fun pushClip(absoluteArea: IntRect, relativeArea: IntRect) {
        graphics.enableScissor(relativeArea.left, relativeArea.top, relativeArea.right, relativeArea.bottom)
        val lastScissorItem = graphics.peekScissorStack()
        disableRenderingStack.push(lastScissorItem.width <= 0 || lastScissorItem.height <= 0)
    }

    override fun popClip() {
        graphics.disableScissor()
        disableRenderingStack.popBoolean()
    }

    override fun drawItemStack(
        offset: IntOffset,
        size: IntSize,
        stack: ItemStack,
    ) {
        if (disableRendering) {
            return
        }
        val minecraftStack = stack.toVanilla()
        pushState()
        graphics.pose().scale(size.width.toFloat() / 16f, size.height.toFloat() / 16f)
        graphics.item(minecraftStack, offset.x, offset.y)
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
        graphics.requestCursor(mapPointer(pointer))
    }
}
