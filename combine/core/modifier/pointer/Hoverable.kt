package top.fifthlight.combine.modifier.pointer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import top.fifthlight.combine.input.Interaction
import top.fifthlight.combine.input.MutableInteractionSource
import top.fifthlight.combine.input.pointer.PointerEvent
import top.fifthlight.combine.input.pointer.PointerEventType
import top.fifthlight.combine.layout.measure.Placeable
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.node.LayoutNode
import top.fifthlight.data.Offset

sealed class HoverInteraction : Interaction {
    data object Empty : HoverInteraction()
    data object Hover : HoverInteraction()
}

@Composable
fun Modifier.hoverable(
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onHovered: (Boolean) -> Unit
) = then(HoverableModifierNode(interactionSource, onHovered))

private data class HoverableModifierNode(
    val interactionSource: MutableInteractionSource,
    val onHovered: (Boolean) -> Unit,
) : Modifier.Node<HoverableModifierNode>, PointerInputModifierNode {

    override fun onPointerEvent(
        event: PointerEvent,
        node: Placeable,
        layoutNode: LayoutNode,
        children: (PointerEvent) -> Boolean
    ): Boolean {
        when (event.type) {
            PointerEventType.Enter -> {
                onHovered(true)
                interactionSource.tryEmit(HoverInteraction.Hover)
            }

            PointerEventType.Leave -> {
                onHovered(false)
                interactionSource.tryEmit(HoverInteraction.Empty)
            }
        }
        return false
    }
}

@Composable
fun Modifier.hoverableWithOffset(
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onHovered: (Boolean?, Offset) -> Unit
) = then(HoverableWithOffsetModifierNode(interactionSource, onHovered))

private data class HoverableWithOffsetModifierNode(
    val interactionSource: MutableInteractionSource,
    val onHovered: (Boolean?, Offset) -> Unit,
) : Modifier.Node<HoverableWithOffsetModifierNode>, PointerInputModifierNode {

    override fun onPointerEvent(
        event: PointerEvent,
        node: Placeable,
        layoutNode: LayoutNode,
        children: (PointerEvent) -> Boolean
    ): Boolean {
        when (event.type) {
            PointerEventType.Enter -> {
                onHovered(true, event.position - node.absolutePosition)
                interactionSource.tryEmit(HoverInteraction.Hover)
            }

            PointerEventType.Move -> {
                onHovered(null, event.position - node.absolutePosition)
            }

            PointerEventType.Leave -> {
                onHovered(false, event.position - node.absolutePosition)
                interactionSource.tryEmit(HoverInteraction.Empty)
            }
        }
        return false
    }
}
