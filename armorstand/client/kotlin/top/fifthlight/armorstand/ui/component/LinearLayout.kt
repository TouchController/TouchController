package top.fifthlight.armorstand.ui.component

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.Renderable
import net.minecraft.client.gui.layouts.AbstractLayout
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.client.gui.layouts.LayoutSettings
import java.util.function.Consumer

class LinearLayout(
    x: Int = 0,
    y: Int = 0,
    width: Int = 0,
    height: Int = 0,
    var direction: Direction = Direction.HORIZONTAL,
    val align: Align = Align.START,
    var gap: Int = 0,
    var padding: Insets = Insets.ZERO,
    val surface: Surface = Surface.empty,
) : AbstractLayout(x, y, width, height), Renderable, ResizableLayout {
    enum class Direction {
        HORIZONTAL,
        VERTICAL,
    }

    enum class Align {
        START,
        CENTER,
        END,
    }

    private class Element<T : LayoutElement>(
        private val inner: T,
        positioner: LayoutSettings,
        private val onSizeChanged: (widget: T, width: Int, height: Int) -> Unit,
    ) : AbstractChildWrapper(inner, positioner) {
        fun setSize(width: Int, height: Int) = onSizeChanged(
            inner,
            width - layoutSettings.paddingLeft - layoutSettings.paddingRight,
            height - layoutSettings.paddingTop - layoutSettings.paddingBottom
        )
    }

    private val elements = mutableListOf<Element<*>>()

    fun <T : LayoutElement> add(
        widget: T,
        layoutSettings: LayoutSettings = LayoutSettings.defaults(),
        onSizeChanged: (widget: T, width: Int, height: Int) -> Unit,
    ) {
        elements.add(Element(widget, layoutSettings, onSizeChanged))
    }

    fun <T : AbstractWidget> add(
        widget: T,
        layoutSettings: LayoutSettings = LayoutSettings.defaults(),
        expand: Boolean = false,
    ) {
        if (expand) {
            add(widget, layoutSettings) { w, width, height -> w.setSize(width, height) }
        } else {
            add(widget, layoutSettings) { w, width, height -> }
        }
    }

    fun <T> add(
        widget: T,
        layoutSettings: LayoutSettings = LayoutSettings.defaults(),
        expand: Boolean = false,
    ) where T : ResizableLayout, T : LayoutElement {
        if (expand) {
            add(widget, layoutSettings) { w, width, height -> w.setDimensions(width, height) }
        } else {
            add(widget, layoutSettings) { w, width, height -> }
        }
    }

    fun removeAt(index: Int): LayoutElement = elements.removeAt(index).child

    fun <T : LayoutElement> setAt(
        index: Int,
        widget: T,
        layoutSettings: LayoutSettings = LayoutSettings.defaults(),
        onSizeChanged: (widget: T, width: Int, height: Int) -> Unit,
    ) {
        elements[index] = Element(widget, layoutSettings, onSizeChanged)
    }

    fun <T : AbstractWidget> setAt(
        index: Int,
        widget: T,
        layoutSettings: LayoutSettings = LayoutSettings.defaults(),
        expand: Boolean = false,
    ) {
        if (expand) {
            setAt(index, widget, layoutSettings) { w, width, height -> w.setSize(width, height) }
        } else {
            setAt(index, widget, layoutSettings) { w, width, height -> }
        }
    }

    fun <T> setAt(
        index: Int,
        widget: T,
        layoutSettings: LayoutSettings = LayoutSettings.defaults(),
        expand: Boolean = false,
    ) where T : ResizableLayout, T : LayoutElement {
        if (expand) {
            setAt(index, widget, layoutSettings) { w, width, height -> w.setDimensions(width, height) }
        } else {
            setAt(index, widget, layoutSettings) { w, width, height -> }
        }
    }

    fun clear() = elements.map { it.child }.also { elements.clear() }

    override fun setDimensions(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    fun pack() = when (direction) {
        Direction.HORIZONTAL -> {
            width = padding.left + padding.right + elements.sumOf { it.width } + gap * (elements.size - 1)
        }
        Direction.VERTICAL -> {
            height = padding.top + padding.bottom + elements.sumOf { it.height } + gap * (elements.size - 1)
        }
    }

    override fun arrangeElements() {
        val sizes = elements.map {
            when (direction) {
                Direction.HORIZONTAL -> it.width
                Direction.VERTICAL -> it.height
            }
        }
        val totalSize = sizes.sum() + gap * (sizes.size - 1)
        val initial = when (direction) {
            Direction.HORIZONTAL -> this@LinearLayout.x + padding.left
            Direction.VERTICAL -> this@LinearLayout.y + padding.top
        }
        val total = when (direction) {
            Direction.HORIZONTAL -> width - padding.left - padding.right
            Direction.VERTICAL -> height - padding.top - padding.bottom
        }
        var pos = initial + when (align) {
            Align.START -> 0
            Align.CENTER -> (total - totalSize) / 2
            Align.END -> total - totalSize
        }
        for ((index, size) in sizes.withIndex()) {
            val element = elements[index]
            when (direction) {
                Direction.HORIZONTAL -> {
                    val availableHeight = height - padding.top - padding.bottom
                    element.setSize(element.width, availableHeight)
                    element.setX(pos, pos + element.width)
                    element.setY(this@LinearLayout.y + padding.top, availableHeight)
                }

                Direction.VERTICAL -> {
                    val availableWidth = width - padding.left - padding.right
                    element.setSize(availableWidth, element.height)
                    element.setX(this@LinearLayout.x + padding.left, availableWidth)
                    element.setY(pos, pos + element.height)
                }
            }
            pos += size + gap
        }
        super.arrangeElements()
    }

    override fun visitChildren(consumer: Consumer<LayoutElement>) {
        elements.forEach { consumer.accept(it.child) }
    }

    override fun render(
        graphics: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        deltaTicks: Float,
    ) {
        surface.draw(graphics, this@LinearLayout.x, this@LinearLayout.y, width, height)
    }
}
