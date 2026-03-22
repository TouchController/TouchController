package top.fifthlight.armorstand.ui.component

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.Renderable
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.resources.ResourceLocation
import java.util.function.Consumer

class LoadingOverlay<T>(
    val inner: T,
    var loading: Boolean = true,
) : Layout by inner, ResizableLayout by inner, Renderable where T: Layout, T: ResizableLayout {
    companion object {
        private val LOADING_ICON: ResourceLocation = ResourceLocation.fromNamespaceAndPath("armorstand", "loading")
        private const val ICON_WIDTH = 32
        private const val ICON_HEIGHT = 32
    }

    override fun visitWidgets(consumer: Consumer<AbstractWidget>) = inner.visitWidgets(consumer)

    override fun arrangeElements() = inner.arrangeElements()

    override fun getRectangle(): ScreenRectangle = inner.rectangle

    override fun setPosition(x: Int, y: Int) = inner.setPosition(x, y)

    override fun render(
        context: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        deltaTicks: Float,
    ) {
        if (loading) {
            context.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                LOADING_ICON,
                x + (width - ICON_WIDTH) / 2,
                y + (height - ICON_HEIGHT) / 2,
                ICON_WIDTH,
                ICON_HEIGHT,
            )
        }
    }
}
