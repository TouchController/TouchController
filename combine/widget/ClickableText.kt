package top.fifthlight.combine.widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import top.fifthlight.combine.data.Text
import top.fifthlight.combine.data.TextStyle
import top.fifthlight.combine.input.pointer.PointerEvent
import top.fifthlight.combine.input.pointer.PointerEventType
import top.fifthlight.combine.input.pointer.PointerIcon
import top.fifthlight.combine.layout.measure.Placeable
import top.fifthlight.combine.layout.measure.contains
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.drawing.DrawModifierNode
import top.fifthlight.combine.modifier.pointer.ClickState
import top.fifthlight.combine.modifier.pointer.PointerInputModifierNode
import top.fifthlight.combine.node.LayoutNode
import top.fifthlight.combine.node.WrapperFactory
import top.fifthlight.combine.node.plus
import top.fifthlight.combine.paint.Canvas
import top.fifthlight.combine.paint.Color
import top.fifthlight.combine.text.TextHitTestProvider
import top.fifthlight.combine.text.TextHitTestResult
import top.fifthlight.combine.ui.style.LocalColorTheme
import top.fifthlight.combine.ui.style.LocalTextStyle
import top.fifthlight.combine.widget.BaseText
import top.fifthlight.data.Offset

val LocalTextHitHandler = staticCompositionLocalOf<(TextHitTestResult) -> Unit> {
    {
        if (it is TextHitTestResult.Native) {
            it.action()
        }
    }
}

private data class ClickableTextModifierNode(
    val text: Text,
    val clickState: ClickState,
    val acceptInsertion: Boolean,
    val onClick: (TextHitTestResult) -> Unit,
) : Modifier.Node<ClickableTextModifierNode>, PointerInputModifierNode, DrawModifierNode {
    override fun onPointerEvent(
        event: PointerEvent,
        node: Placeable,
        layoutNode: LayoutNode,
        children: (PointerEvent) -> Boolean
    ): Boolean {
        when (event.type) {
            PointerEventType.Enter -> clickState.entered = true
            PointerEventType.Leave -> clickState.entered = false
            PointerEventType.Cancel -> clickState.pressed = false

            PointerEventType.Press -> if (event.position in node) {
                val position = (event.position - node.absolutePosition).toIntOffset()
                TextHitTestProvider.hitTest(
                    text = text,
                    maxWidth = node.width,
                    acceptInsertion = acceptInsertion,
                    position = position,
                ) ?: return true
                clickState.entered = true
                clickState.pressed = true
            }

            PointerEventType.Move -> if (event.position in node && clickState.pressed) {
                val position = (event.position - node.absolutePosition).toIntOffset()
                val action = TextHitTestProvider.hitTest(
                    text = text,
                    maxWidth = node.width,
                    acceptInsertion = acceptInsertion,
                    position = position
                )
                if (action == null) {
                    clickState.pressed = false
                }
            }

            PointerEventType.Release -> {
                if (clickState.pressed && clickState.entered) {
                    val position = (event.position - node.absolutePosition).toIntOffset()
                    val action = TextHitTestProvider.hitTest(
                        text = text,
                        maxWidth = node.width,
                        acceptInsertion = acceptInsertion,
                        position = position
                    )
                    if (action != null) {
                        onClick(action)
                    }
                }
                clickState.pressed = false
            }

            else -> return false
        }
        return true
    }

    override fun renderAfter(canvas: Canvas, wrapperNode: Placeable, node: LayoutNode, cursorPos: Offset) {
        if (cursorPos in node) {
            val position = (cursorPos - node.absolutePosition).toIntOffset()
            val action = TextHitTestProvider.hitTest(
                text = text,
                maxWidth = node.width,
                acceptInsertion = acceptInsertion,
                position = position,
            )
            if (action != null) {
                canvas.requestPointerIcon(PointerIcon.PointingHand)
            }
        }
    }

    override val wrapperFactory: WrapperFactory<*>
        get() = DrawModifierNode.wrapperFactory + PointerInputModifierNode.wrapperFactory
}

@Composable
fun ClickableText(
    text: Text,
    modifier: Modifier = Modifier,
    color: Color = LocalColorTheme.current.foreground,
    textStyle: TextStyle = LocalTextStyle.current,
    acceptInsertion: Boolean = false,
    onClick: (TextHitTestResult) -> Unit = LocalTextHitHandler.current,
) = if (textStyle.haveStyle) {
    text.style(textStyle)
} else {
    text
}.let { text ->
    val clickState = remember { ClickState() }
    BaseText(
        modifier = Modifier
            .then(
                ClickableTextModifierNode(
                    text = text,
                    clickState = clickState,
                    acceptInsertion = acceptInsertion,
                    onClick = onClick,
                )
            )
            .then(modifier),
        text = text,
        color = color,
    )
}
