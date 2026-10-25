package top.fifthlight.combine.core.widget

import androidx.compose.runtime.Composable
import top.fifthlight.combine.core.layout.Alignment
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.placement.fillMaxSize
import top.fifthlight.combine.core.modifier.pointer.consumePress
import top.fifthlight.combine.core.screen.DismissHandler
import top.fifthlight.combine.core.widget.layout.Box
import top.fifthlight.combine.core.widget.layout.BoxScope

@Composable
fun Dialog(
    modifier: Modifier = Modifier,
    onDismissRequest: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    DismissHandler {
        onDismissRequest?.invoke()
    }
    Popup(
        onDismissRequest = onDismissRequest
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(modifier),
            alignment = Alignment.Center,
        ) {
            Box(modifier = Modifier.consumePress()) {
                content()
            }
        }
    }
}
