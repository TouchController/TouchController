package top.fifthlight.combine.node

import top.fifthlight.combine.layout.measure.Placeable
import top.fifthlight.combine.paint.Canvas

fun interface NodeRenderer {
    fun render(canvas: Canvas, node: Placeable)

    companion object EmptyRenderer : NodeRenderer {
        override fun render(canvas: Canvas, node: Placeable) = Unit
    }
}
