package top.fifthlight.armorstand.ui.component

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Renderable
import net.minecraft.client.gui.components.events.ContainerEventHandler
import net.minecraft.client.gui.components.tabs.TabManager
import net.minecraft.client.gui.components.tabs.TabNavigationBar
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen
import net.minecraft.client.renderer.RenderPipelines
import java.util.function.Consumer

@Suppress("JavaDefaultMethodsNotOverriddenByDelegation")
class TabNavigationWrapper(
    val tabManager: TabManager,
    val inner: TabNavigationBar,
    val surface: Surface = Surface.empty,
) : ContainerEventHandler by inner, NarratableEntry by inner, LayoutElement, ResizableLayout, Renderable, Layout {
    private var height: Int = 0
    private var width: Int = 0
    private var _x = 0
    private var _y = 0

    override fun getX() = _x

    override fun getY() = _y

    override fun setX(x: Int) {
        this._x = x
    }

    override fun setY(y: Int) {
        this._y = y
    }

    override fun setPosition(x: Int, y: Int) {
        this._x = x
        this._y = y
    }

    override fun setDimensions(width: Int, height: Int) {
        inner.setWidth(width)
        this.height = height
        this.width = width
    }

    override fun getWidth() = width

    override fun getHeight() = height

    override fun visitChildren(consumer: Consumer<LayoutElement>) {}

    override fun isMouseOver(mouseX: Double, mouseY: Double) = inner.isMouseOver(mouseX, mouseY)

    override fun arrangeElements() {
        inner.arrangeElements()
        val realWidth = width.coerceAtMost(400) - 28
        inner.layout.setPosition((width - realWidth) / 2 + _x, _y)
        inner.layout.arrangeElements()
        val navHeight = inner.rectangle.height()
        val area = ScreenRectangle(_x, _y + navHeight, width, height - navHeight)
        tabManager.setTabArea(area)
    }

    override fun render(
        context: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        deltaTicks: Float,
    ) {
        context.blit(
            RenderPipelines.GUI_TEXTURED,
            CreateWorldScreen.TAB_HEADER_BACKGROUND,
            _x,
            _y,
            0.0F,
            0.0F,
            width,
            inner.rectangle.height(),
            16,
            16,
        )
        val left = inner.tabButtons.first().x
        val right = inner.tabButtons.last().right
        context.blit(
            RenderPipelines.GUI_TEXTURED,
            Screen.HEADER_SEPARATOR,
            _x,
            _y + inner.rectangle.height() - 2,
            0.0f,
            0.0f,
            left - _x,
            2,
            32,
            2,
        )
        context.blit(
            RenderPipelines.GUI_TEXTURED,
            Screen.HEADER_SEPARATOR,
            right,
            _y + inner.rectangle.height() - 2,
            0.0F,
            0.0F,
            _x + width - right,
            2,
            32,
            2,
        )
        inner.tabButtons.forEach { button ->
            button.render(context, mouseX, mouseY, deltaTicks)
        }
        surface.draw(context, _x, _y + inner.rectangle.height(), width, height - inner.rectangle.height())
    }

    override fun isActive() = inner.isActive

    override fun getNarratables(): Collection<NarratableEntry> = inner.narratables

    override fun narrationPriority(): NarratableEntry.NarrationPriority = inner.narrationPriority()

    override fun getRectangle(): ScreenRectangle = inner.rectangle
}
