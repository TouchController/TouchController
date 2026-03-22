package top.fifthlight.combine.modifier.focus

import top.fifthlight.combine.input.Interaction
import top.fifthlight.combine.input.MutableInteractionSource
import top.fifthlight.combine.input.focus.LocalFocusManager
import top.fifthlight.combine.input.pointer.PointerEvent
import top.fifthlight.combine.input.pointer.PointerEventType
import top.fifthlight.combine.layout.measure.Placeable
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.pointer.PointerInputModifierNode
import top.fifthlight.combine.node.LayoutNode
import top.fifthlight.combine.node.plus

sealed class FocusInteraction : Interaction {
    data object Blur : FocusInteraction()
    data object Focus : FocusInteraction()
}

fun Modifier.focusable(
    interactionSource: MutableInteractionSource? = null,
) = then(
    FocusableModifierNode(
        interactionSource = interactionSource,
    )
)

data class FocusableModifierNode(
    val interactionSource: MutableInteractionSource?,
) : Modifier.Node<FocusableModifierNode>, FocusStateListenerModifierNode, PointerInputModifierNode {
    override fun onFocusStateChanged(focused: Boolean) {
        interactionSource?.tryEmit(if (focused) FocusInteraction.Focus else FocusInteraction.Blur)
    }

    override fun onPointerEvent(
        event: PointerEvent,
        node: Placeable,
        layoutNode: LayoutNode,
        children: (PointerEvent) -> Boolean,
    ): Boolean {
        if (event.type == PointerEventType.Press) {
            layoutNode.compositionLocalMap[LocalFocusManager].requestFocus(layoutNode)
            children(event)
            return true
        }
        return false
    }

    companion object {
        private val wrapperFactory =
            PointerInputModifierNode.wrapperFactory + FocusStateListenerModifierNode.wrapperFactory + { node, children, _ ->
                node.focusable = true
                children
            }
    }

    override val wrapperFactory
        get() = Companion.wrapperFactory
}
