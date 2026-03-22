package top.fifthlight.combine.modifier.drawing

import top.fifthlight.combine.layout.measure.Placeable
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.node.LayoutNode
import top.fifthlight.combine.paint.Canvas
import top.fifthlight.combine.paint.translate
import top.fifthlight.data.IntOffset
import top.fifthlight.data.IntRect
import top.fifthlight.data.IntSize
import top.fifthlight.data.Offset

fun Modifier.clip() = then(ClipNode)

private data object ClipNode : DrawModifierNode, Modifier.Node<ClipNode> {
    override fun renderBefore(
        canvas: Canvas,
        wrapperNode: Placeable,
        node: LayoutNode,
        cursorPos: Offset,
    ) {
        canvas.pushClip(
            IntRect(
                offset = IntOffset(wrapperNode.absoluteX, wrapperNode.absoluteY),
                size = wrapperNode.size,
            ),
            IntRect(
                offset = IntOffset(wrapperNode.x, wrapperNode.y),
                size = wrapperNode.size,
            ),
        )
    }

    override fun renderAfter(
        canvas: Canvas,
        wrapperNode: Placeable,
        node: LayoutNode,
        cursorPos: Offset,
    ) {
        canvas.popClip()
    }
}


fun Modifier.clip(
    width: Float,
    height: Float,
    anchorOffset: IntOffset? = null,
) = then(PercentClipNode(width, height, anchorOffset))

private data class PercentClipNode(
    val width: Float,
    val height: Float,
    val anchorOffset: IntOffset? = null,
) : DrawModifierNode, Modifier.Node<PercentClipNode> {
    override fun renderBefore(
        canvas: Canvas,
        wrapperNode: Placeable,
        node: LayoutNode,
        cursorPos: Offset,
    ) {
        val size = IntSize(
            width = (wrapperNode.width * width).toInt(),
            height = (wrapperNode.height * height).toInt(),
        )
        val offset = anchorOffset?.let {
            IntOffset(
                x = if (wrapperNode.absoluteX > anchorOffset.x) {
                    wrapperNode.width - size.width
                } else {
                    0
                },
                y = if (wrapperNode.absoluteY > anchorOffset.y) {
                    size.height - wrapperNode.height
                } else {
                    0
                }
            )
        } ?: IntOffset.ZERO
        canvas.pushClip(
            IntRect(
                offset = IntOffset(wrapperNode.absoluteX, wrapperNode.absoluteY),
                size = size,
            ),
            IntRect(
                offset = IntOffset.ZERO,
                size = size,
            ),
        )
        canvas.translate(offset)
    }

    override fun renderAfter(
        canvas: Canvas,
        wrapperNode: Placeable,
        node: LayoutNode,
        cursorPos: Offset,
    ) {
        canvas.popClip()
    }
}
