package top.fifthlight.combine.core.widget

import androidx.compose.runtime.*
import top.fifthlight.combine.core.node.LocalCombineOwner

@Composable
fun Popup(
    onDismissRequest: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val owner = LocalCombineOwner.current
    val parentComposition = rememberCompositionContext()
    val currentContent by rememberUpdatedState(content)
    val currentOnDismiss by rememberUpdatedState(onDismissRequest)
    DisposableEffect(owner, parentComposition) {
        val layer = owner.addLayer(
            parentContext = parentComposition,
            onDismissRequest = { currentOnDismiss?.invoke() },
            content = { currentContent() },
        )
        onDispose {
            layer.dispose()
        }
    }
}
