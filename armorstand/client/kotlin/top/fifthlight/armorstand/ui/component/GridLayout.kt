package top.fifthlight.armorstand.ui.component

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Renderable
import net.minecraft.client.gui.layouts.AbstractLayout
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.client.gui.layouts.LayoutSettings
import java.util.function.Consumer
import kotlin.math.max

class GridLayout(
    x: Int = 0,
    y: Int = 0,
    width: Int = 0,
    height: Int = 0,
    private val surface: Surface?,
    private val gridPadding: Insets = Insets.ZERO,
) : AbstractLayout(x, y, width, height), ResizableLayout, Renderable {
    private class Element<T : LayoutElement>(
        val column: Int,
        val row: Int,
        inner: T,
        layoutSettings: LayoutSettings,
    ) : AbstractChildWrapper(inner, layoutSettings)

    private val elements = mutableListOf<Element<*>>()
    private val grids = mutableMapOf<Pair<Int, Int>, Element<*>>()

    override fun visitChildren(consumer: Consumer<LayoutElement>) {
        elements.forEach { consumer.accept(it.child) }
    }

    override fun setDimensions(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    private val rowHeights = LinkedHashMap<Int, Int>()
    private val columnWidths = LinkedHashMap<Int, Int>()

    private fun recalculateSizes() {
        rowHeights.clear()
        columnWidths.clear()
        grids.clear()
        for (element in elements) {
            grids[element.column to element.row] = element
            val rowHeight = rowHeights[element.row]
            val columnWidth = columnWidths[element.column]
            rowHeights[element.row] = max(rowHeight ?: 0, element.child.height + gridPadding.top + gridPadding.bottom)
            columnWidths[element.column] =
                max(columnWidth ?: 0, element.child.width + gridPadding.left + gridPadding.right)
        }
    }

    fun <T : LayoutElement> add(
        column: Int,
        row: Int,
        widget: T,
        layoutSettings: LayoutSettings = LayoutSettings.defaults().alignHorizontallyCenter().alignVerticallyMiddle(),
    ) {
        elements.add(Element(column, row, widget, layoutSettings))
    }

    fun clear() {
        elements.clear()
        grids.clear()
        rowHeights.clear()
        columnWidths.clear()
    }

    fun pack() {
        recalculateSizes()
        val totalRowHeight = rowHeights.values.sum()
        val totalColumnWidth = columnWidths.values.sum()
        setDimensions(totalColumnWidth, totalRowHeight)
    }

    override fun arrangeElements() {
        recalculateSizes()
        var currentY = y
        for (row in rowHeights.sequencedKeySet()) {
            var currentX = x
            val columnHeight = rowHeights[row] ?: 0
            for (column in columnWidths.sequencedKeySet()) {
                val columnWidth = columnWidths[column] ?: 0
                val element = grids[column to row]
                element?.setX(currentX + gridPadding.left, columnWidth - gridPadding.left - gridPadding.right)
                element?.setY(currentY + gridPadding.top, columnHeight - gridPadding.top - gridPadding.bottom)
                currentX += columnWidth
            }
            currentY += columnHeight
        }
        super.arrangeElements()
    }

    override fun render(
        context: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        deltaTicks: Float,
    ) {
        surface?.draw(context, x, y, width, height)
    }
}
