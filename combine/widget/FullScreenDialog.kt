package top.fifthlight.combine.widget.ui

import androidx.compose.runtime.Composable
import top.fifthlight.combine.screen.DismissHandler
import top.fifthlight.combine.widget.Popup

@Composable
fun FullScreenDialog(
    onDismissRequest: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    DismissHandler(onDismissRequest != null) {
        onDismissRequest?.let { it() }
    }
    Popup(content = content)
}
