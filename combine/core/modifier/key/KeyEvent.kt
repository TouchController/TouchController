package top.fifthlight.combine.modifier.key

import top.fifthlight.combine.input.key.KeyEvent
import top.fifthlight.combine.input.key.KeyEventReceiver
import top.fifthlight.combine.modifier.Modifier

fun Modifier.onKeyEvent(handler: KeyEventReceiver) = then(KeyEventReceiverModifierNode(handler))

private data class KeyEventReceiverModifierNode(
    val handler: KeyEventReceiver
) : Modifier.Node<KeyEventReceiverModifierNode>, KeyInputModifierNode {
    override fun onKeyEvent(event: KeyEvent) = handler.onKeyEvent(event)
}
