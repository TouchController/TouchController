package top.fifthlight.combine.core.modifier.pointer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import top.fifthlight.combine.core.input.interaction.Interaction
import top.fifthlight.combine.core.input.interaction.MutableInteractionSource
import top.fifthlight.combine.core.input.pointer.PointerEvent
import top.fifthlight.combine.core.input.pointer.PointerEventType
import top.fifthlight.combine.core.input.pointer.PointerIcon
import top.fifthlight.combine.core.layout.measure.Placeable
import top.fifthlight.combine.core.layout.measure.contains
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.drawing.DrawModifierNode
import top.fifthlight.combine.core.node.LayoutNode
import top.fifthlight.combine.core.node.WrapperFactory
import top.fifthlight.combine.core.node.plus
import top.fifthlight.combine.core.paint.Canvas
import top.fifthlight.data.Offset

sealed class DragInteraction : Interaction {
    data object Empty : DragInteraction()
    data object Hover : DragInteraction()
    data object Active : DragInteraction()
}

class DragState internal constructor(
    internal var pressed: Boolean = false,
    internal var entered: Boolean = false,
    internal var lastPosition: Offset? = null,
)

@Composable
fun Modifier.draggable(
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    dragState: DragState = remember { DragState() },
    pointerIcon: PointerIcon = PointerIcon.ResizeAll,
    onDrag: (relative: Offset) -> Unit,
) = then(
    DraggableModifierNode(
        interactionSource = interactionSource,
        dragState = dragState,
        onDrag = { relative, _ -> onDrag(relative) },
        onRelease = { _, _ -> },
        onCancel = { _, _ -> },
        pointerIcon = pointerIcon,
    )
)

@Composable
fun Modifier.draggable(
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    dragState: DragState = remember { DragState() },
    pointerIcon: PointerIcon = PointerIcon.ResizeAll,
    onDrag: Placeable.(relative: Offset, absolute: Offset) -> Unit,
) = then(
    DraggableModifierNode(
        interactionSource = interactionSource,
        dragState = dragState,
        onDrag = onDrag,
        onRelease = { _, _ -> },
        onCancel = { _, _ -> },
        pointerIcon = pointerIcon,
    )
)

@Composable
fun Modifier.draggable(
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    dragState: DragState = remember { DragState() },
    pointerIcon: PointerIcon = PointerIcon.ResizeAll,
    onDrag: Placeable.(relative: Offset, absolute: Offset) -> Unit,
    onRelease: Placeable.(relative: Offset, absolute: Offset) -> Unit,
    onCancel: Placeable.(relative: Offset, absolute: Offset) -> Unit = onRelease,
) = then(
    DraggableModifierNode(
        interactionSource = interactionSource,
        dragState = dragState,
        onDrag = onDrag,
        onRelease = onRelease,
        onCancel = onCancel,
        pointerIcon = pointerIcon,
    )
)

private data class DraggableModifierNode(
    val interactionSource: MutableInteractionSource,
    val dragState: DragState,
    val onDrag: Placeable.(relative: Offset, absolute: Offset) -> Unit,
    val onRelease: Placeable.(relative: Offset, absolute: Offset) -> Unit,
    val onCancel: Placeable.(relative: Offset, absolute: Offset) -> Unit,
    val pointerIcon: PointerIcon,
) : Modifier.Node<DraggableModifierNode>, PointerInputModifierNode, DrawModifierNode {

    override fun onPointerEvent(
        event: PointerEvent,
        node: Placeable,
        layoutNode: LayoutNode,
        children: (PointerEvent) -> Boolean,
    ): Boolean {
        val absolutePosition = event.position - node.absolutePosition
        when (event.type) {
            PointerEventType.Enter -> dragState.entered = true

            PointerEventType.Leave -> dragState.entered = false

            PointerEventType.Press -> {
                dragState.pressed = true
                dragState.lastPosition = event.position
                onDrag(node, Offset.ZERO, absolutePosition)
            }

            PointerEventType.Move -> {
                if (dragState.pressed) {
                    val lastPosition = dragState.lastPosition
                    if (lastPosition != null) {
                        val diff = event.position - lastPosition
                        onDrag(node, diff, absolutePosition)
                    } else {
                        onDrag(node, Offset.ZERO, absolutePosition)
                    }
                    dragState.lastPosition = event.position
                }
            }

            PointerEventType.Release -> {
                if (dragState.pressed) {
                    onRelease(node, Offset.ZERO, absolutePosition)
                }
                dragState.pressed = false
            }

            PointerEventType.Cancel -> {
                if (dragState.pressed) {
                    onCancel(node, Offset.ZERO, absolutePosition)
                }
                dragState.pressed = false
            }

            else -> return false
        }
        if (dragState.pressed) {
            interactionSource.tryEmit(DragInteraction.Active)
        } else {
            if (dragState.entered) {
                interactionSource.tryEmit(DragInteraction.Hover)
            } else {
                interactionSource.tryEmit(DragInteraction.Empty)
            }
        }
        return true
    }

    override fun renderAfter(canvas: Canvas, wrapperNode: Placeable, node: LayoutNode, cursorPos: Offset) {
        if ((dragState.entered || dragState.pressed) && cursorPos in node) {
            canvas.requestPointerIcon(pointerIcon)
        }
    }

    override val wrapperFactory: WrapperFactory<*>
        get() = DrawModifierNode.wrapperFactory + PointerInputModifierNode.wrapperFactory
}
