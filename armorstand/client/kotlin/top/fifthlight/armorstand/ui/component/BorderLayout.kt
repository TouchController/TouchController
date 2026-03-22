package top.fifthlight.armorstand.ui.component

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.Renderable
import net.minecraft.client.gui.layouts.AbstractLayout
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.client.gui.layouts.LayoutSettings
import java.util.function.Consumer

class BorderLayout(
    x: Int = 0,
    y: Int = 0,
    width: Int = 0,
    height: Int = 0,
    var direction: Direction = Direction.HORIZONTAL,
    val surface: Surface = Surface.empty,
) : AbstractLayout(x, y, width, height), ResizableLayout, Renderable {
    enum class Direction {
        HORIZONTAL,
        VERTICAL
    }

    private class Element<T : LayoutElement>(
        private val inner: T,
        layoutSettings: LayoutSettings,
        private val onSizeChanged: (widget: T, width: Int, height: Int) -> Unit = { _, _, _ -> },
    ) : AbstractChildWrapper(inner, layoutSettings) {
        fun setSize(width: Int, height: Int) = onSizeChanged(
            inner,
            width - layoutSettings.paddingLeft - layoutSettings.paddingRight,
            height - layoutSettings.paddingTop - layoutSettings.paddingBottom
        )
    }

    private var firstElement: Element<*>? = null
    private var secondElement: Element<*>? = null
    private var centerElement: Element<*>? = null

    override fun visitChildren(consumer: Consumer<LayoutElement>) {
        firstElement?.let { consumer.accept(it.child) }
        centerElement?.let { consumer.accept(it.child) }
        secondElement?.let { consumer.accept(it.child) }
    }

    fun <T : LayoutElement> setFirstElement(
        widget: T,
        layoutSettings: LayoutSettings = LayoutSettings.defaults(),
        onSizeChanged: (widget: T, width: Int, height: Int) -> Unit,
    ) {
        firstElement = Element(widget, layoutSettings, onSizeChanged)
    }

    fun <T : LayoutElement> setSecondElement(
        widget: T,
        layoutSettings: LayoutSettings = LayoutSettings.defaults(),
        onSizeChanged: (widget: T, width: Int, height: Int) -> Unit,
    ) {
        secondElement = Element(widget, layoutSettings, onSizeChanged)
    }

    fun <T : LayoutElement> setCenterElement(
        widget: T,
        layoutSettings: LayoutSettings = LayoutSettings.defaults(),
        onSizeChanged: (widget: T, width: Int, height: Int) -> Unit,
    ) {
        centerElement = Element(widget, layoutSettings, onSizeChanged)
    }


    fun <T : AbstractWidget> setFirstElement(
        widget: T,
        layoutSettings: LayoutSettings = LayoutSettings.defaults(),
    ) {
        firstElement = Element(widget, layoutSettings) { widget, width, height -> widget.setSize(width, height) }
    }

    fun <T : AbstractWidget> setSecondElement(
        widget: T,
        layoutSettings: LayoutSettings = LayoutSettings.defaults(),
    ) {
        secondElement = Element(widget, layoutSettings) { widget, width, height -> widget.setSize(width, height) }
    }

    fun <T : AbstractWidget> setCenterElement(
        widget: T,
        layoutSettings: LayoutSettings = LayoutSettings.defaults(),
    ) {
        centerElement = Element(widget, layoutSettings) { widget, width, height -> widget.setSize(width, height) }
    }

    fun <T> setFirstElement(
        widget: T,
        layoutSettings: LayoutSettings = LayoutSettings.defaults(),
    ) where T : ResizableLayout, T: LayoutElement {
        firstElement = Element(widget, layoutSettings) { widget, width, height -> widget.setDimensions(width, height) }
    }

    fun <T> setSecondElement(
        widget: T,
        layoutSettings: LayoutSettings = LayoutSettings.defaults(),
    ) where T : ResizableLayout, T: LayoutElement {
        secondElement = Element(widget, layoutSettings) { widget, width, height -> widget.setDimensions(width, height) }
    }

    fun <T> setCenterElement(
        widget: T,
        layoutSettings: LayoutSettings = LayoutSettings.defaults(),
    ) where T : ResizableLayout, T: LayoutElement {
        centerElement = Element(widget, layoutSettings) { widget, width, height -> widget.setDimensions(width, height) }
    }

    override fun setDimensions(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    override fun arrangeElements() {
        val first = firstElement
        val second = secondElement
        val center = centerElement

        when (direction) {
            Direction.HORIZONTAL -> {
                val leftWidth = first?.width ?: 0
                val rightWidth = second?.width ?: 0
                val centerWidth = width - leftWidth - rightWidth
                first?.let {
                    it.setSize(leftWidth, height)
                    it.setX(x, x + leftWidth)
                    it.setY(y, y + height)
                }
                center?.let {
                    it.setSize(centerWidth, height)
                    it.setX(x + leftWidth, x + width - rightWidth)
                    it.setY(y, y + height)
                }
                second?.let {
                    it.setSize(rightWidth, height)
                    it.setX(x + width - rightWidth, x + width)
                    it.setY(y, y + height)
                }
            }

            Direction.VERTICAL -> {
                val topHeight = first?.height ?: 0
                val bottomHeight = second?.height ?: 0
                val centerHeight = height - topHeight - bottomHeight
                first?.let {
                    it.setSize(width, topHeight)
                    it.setX(x, width)
                    it.setY(y, topHeight)
                }
                center?.let {
                    it.setSize(width, centerHeight)
                    it.setX(x, width)
                    it.setY(y + topHeight, height - bottomHeight)
                }
                second?.let {
                    it.setSize(width, bottomHeight)
                    it.setX(x, width)
                    it.setY(y + height - bottomHeight, height)
                }
            }
        }
        super.arrangeElements()
    }

    override fun render(
        context: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        deltaTicks: Float,
    ) {
        surface.draw(context, this@BorderLayout.x, this@BorderLayout.y, width, height)
    }
}
