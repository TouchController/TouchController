package top.fifthlight.combine.input.focus

import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate

interface FocusNode {
    val focusable: Boolean

    fun onFocusStateChanged(focused: Boolean)
}

val LocalFocusManager = staticCompositionLocalOf<FocusManager> { error("No FocusManager in context") }

class FocusManager {
    private var _focusedNode = MutableStateFlow<FocusNode?>(null)
    val focusedNode = _focusedNode.asStateFlow()

    fun requestFocus(node: FocusNode) {
        if (!node.focusable) {
            return
        }
        val oldFocusedNode = _focusedNode.value
        _focusedNode.getAndUpdate {
            if (it != node) {
                it?.onFocusStateChanged(false)
            }
            node
        }
        if (oldFocusedNode != node) {
            node.onFocusStateChanged(true)
        }
    }

    fun requestBlur() {
        _focusedNode.getAndUpdate {
            it?.onFocusStateChanged(false)
            null
        }
    }

    fun requestBlur(node: FocusNode) {
        _focusedNode.getAndUpdate {
            if (it == node) {
                node.onFocusStateChanged(false)
                null
            } else {
                it
            }
        }
    }
}
