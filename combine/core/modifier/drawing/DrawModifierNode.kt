package top.fifthlight.combine.modifier.drawing

import top.fifthlight.combine.input.focus.FocusStateListener
import top.fifthlight.combine.input.key.KeyEventReceiver
import top.fifthlight.combine.input.pointer.PointerEventReceiver
import top.fifthlight.combine.input.text.TextInputReceiver
import top.fifthlight.combine.layout.measure.Placeable
import top.fifthlight.combine.node.LayoutNode
import top.fifthlight.combine.node.WrapperFactory
import top.fifthlight.combine.node.WrapperLayoutNode
import top.fifthlight.combine.node.WrapperModifierNode
import top.fifthlight.combine.paint.Canvas
import top.fifthlight.combine.paint.withState
import top.fifthlight.data.Offset

interface DrawModifierNode : WrapperModifierNode {
    fun renderBefore(canvas: Canvas, wrapperNode: Placeable, node: LayoutNode, cursorPos: Offset) {}
    fun renderAfter(canvas: Canvas, wrapperNode: Placeable, node: LayoutNode, cursorPos: Offset) {}

    companion object {
        private class DrawWrapperNode(
            node: LayoutNode,
            children: WrapperLayoutNode,
            val modifierNode: DrawModifierNode,
        ) : WrapperLayoutNode.PositionWrapper(node, children),
            PointerEventReceiver by children,
            FocusStateListener by children,
            TextInputReceiver by children,
            KeyEventReceiver by children {

            override fun render(canvas: Canvas, cursorPos: Offset) {
                canvas.withState {
                    canvas.translate(x, y)
                    modifierNode.renderBefore(canvas, this, node, cursorPos)
                    children.render(canvas, cursorPos)
                    modifierNode.renderAfter(canvas, this, node, cursorPos)
                }
            }
        }

        val wrapperFactory = WrapperFactory<DrawModifierNode> { node, children, modifier ->
            DrawWrapperNode(node, children, modifier)
        }
    }

    override val wrapperFactory: WrapperFactory<*>
        get() = Companion.wrapperFactory
}
