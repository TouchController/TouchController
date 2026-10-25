package top.fifthlight.combine.core.modifier.pointer

import top.fifthlight.combine.core.input.focus.FocusStateListener
import top.fifthlight.combine.core.input.key.KeyEventReceiver
import top.fifthlight.combine.core.input.pointer.PointerEvent
import top.fifthlight.combine.core.input.text.TextInputReceiver
import top.fifthlight.combine.core.layout.measure.Placeable
import top.fifthlight.combine.core.node.LayoutNode
import top.fifthlight.combine.core.node.WrapperFactory
import top.fifthlight.combine.core.node.WrapperLayoutNode
import top.fifthlight.combine.core.node.WrapperModifierNode

interface PointerInputModifierNode: WrapperModifierNode {
    fun onPointerEvent(
        event: PointerEvent,
        node: Placeable,
        layoutNode: LayoutNode,
        children: (PointerEvent) -> Boolean
    ): Boolean

    companion object {
        private class PointerInputWrapperNode(
            node: LayoutNode,
            children: WrapperLayoutNode,
            private val modifierNode: PointerInputModifierNode,
        ) : WrapperLayoutNode.PositionWrapper(node, children),
            FocusStateListener by children,
            TextInputReceiver by children,
            KeyEventReceiver by children {

            override fun onPointerEvent(event: PointerEvent): Boolean {
                val consumed = modifierNode.onPointerEvent(event, this, node) {
                    children.onPointerEvent(it)
                }
                return consumed || children.onPointerEvent(event)
            }
        }

        val wrapperFactory = WrapperFactory<PointerInputModifierNode> { node, children, modifier ->
            PointerInputWrapperNode(node, children, modifier)
        }
    }

    override val wrapperFactory: WrapperFactory<*>
        get() = Companion.wrapperFactory
}
