package top.fifthlight.combine.modifier.focus

import top.fifthlight.combine.input.focus.FocusManager
import top.fifthlight.combine.input.focus.LocalFocusManager
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.node.AttachListenerModifierNode
import top.fifthlight.combine.node.LayoutNode

class FocusRequester {
    internal var focusManager: FocusManager? = null
    internal var layoutNode: LayoutNode? = null

    fun requestFocus() {
        layoutNode?.let { focusManager?.requestFocus(it) }
    }

    fun requestBlur() {
        layoutNode?.let { focusManager?.requestBlur(it) }
    }
}

fun Modifier.focusRequester(focusRequester: FocusRequester) = then(FocusRequesterModifierNode(focusRequester))

data class FocusRequesterModifierNode(
    private val focusRequester: FocusRequester,
) : Modifier.Node<FocusRequesterModifierNode>, AttachListenerModifierNode {
    override fun onAttachedToNode(node: LayoutNode) {
        focusRequester.focusManager = node.compositionLocalMap[LocalFocusManager]
        focusRequester.layoutNode = node
    }
}
