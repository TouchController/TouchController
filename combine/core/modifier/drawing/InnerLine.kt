package top.fifthlight.combine.modifier.drawing

import top.fifthlight.combine.layout.measure.Placeable
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.node.LayoutNode
import top.fifthlight.combine.paint.Canvas
import top.fifthlight.combine.paint.Color
import top.fifthlight.data.IntOffset
import top.fifthlight.data.Offset

fun Modifier.innerLine(color: Color) = then(InnerLineNode(color))

private data class InnerLineNode(
    val color: Color,
) : DrawModifierNode, Modifier.Node<InnerLineNode> {
    override fun renderAfter(canvas: Canvas, wrapperNode: Placeable, node: LayoutNode, cursorPos: Offset) {
        canvas.drawRect(IntOffset.ZERO, wrapperNode.size, color)
    }
}
