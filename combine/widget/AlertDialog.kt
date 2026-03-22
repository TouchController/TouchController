package top.fifthlight.combine.widget.ui

import androidx.compose.runtime.*
import top.fifthlight.combine.animation.animateFloatAsState
import top.fifthlight.combine.layout.Alignment
import top.fifthlight.combine.layout.Arrangement
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.drawing.background
import top.fifthlight.combine.modifier.drawing.border
import top.fifthlight.combine.modifier.drawing.clip
import top.fifthlight.combine.modifier.placement.offset
import top.fifthlight.combine.modifier.placement.padding
import top.fifthlight.combine.modifier.pointer.consumePress
import top.fifthlight.combine.paint.Color
import top.fifthlight.combine.paint.Colors
import top.fifthlight.combine.paint.Drawable
import top.fifthlight.combine.theme.LocalTheme
import top.fifthlight.combine.widget.Dialog
import top.fifthlight.combine.widget.layout.Column
import top.fifthlight.combine.widget.layout.ColumnScope
import top.fifthlight.combine.widget.layout.Row
import top.fifthlight.combine.widget.layout.RowScope

@Composable
fun AlertDialog(
    modifier: Modifier = Modifier,
    visible: Boolean,
    background: Drawable = LocalTheme.current.drawables.alertDialogBackground,
    onDismissRequest: (() -> Unit)? = null,
    title: @Composable () -> Unit = {},
    action: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val showingProgress by animateFloatAsState(if (visible) 1f else 0f)
    if (showingProgress == 0f) {
        return
    }
    Dialog(
        modifier = Modifier.background(Colors.TRANSPARENT_BLACK * Color(showingProgress, 1f, 1f, 1f)),
        onDismissRequest = onDismissRequest,
    ) {
        Column(
            modifier = Modifier
                .padding(8)
                .border(background)
                .consumePress()
                .offset(y = (1f - showingProgress) * .2f)
                .clip(width = 1f, height = showingProgress)
                .then(modifier),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8)
        ) {
            title()
            content()
            action?.let {
                Row(
                    modifier = Modifier.alignment(Alignment.Right),
                    horizontalArrangement = Arrangement.spacedBy(8),
                ) {
                    action()
                }
            }
        }
    }
}

@Composable
inline fun <T, V> AlertDialog(
    modifier: Modifier = Modifier,
    value: T,
    crossinline valueTransformer: (T) -> V?,
    background: Drawable = LocalTheme.current.drawables.alertDialogBackground,
    noinline onDismissRequest: (() -> Unit)? = null,
    noinline title: @Composable (V) -> Unit = {},
    noinline action: (@Composable RowScope.(V) -> Unit)? = null,
    crossinline content: @Composable ColumnScope.(V) -> Unit,
) {
    var currentValue by remember { mutableStateOf(valueTransformer(value)) }
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(value) {
        val transformedValue = valueTransformer(value)?.also { currentValue = it }
        visible = transformedValue != null
    }
    AlertDialog(
        modifier = modifier,
        visible = visible,
        background = background,
        onDismissRequest = onDismissRequest,
        title = { currentValue?.let { value -> title(value) } },
        action = { currentValue?.let { value -> action?.let { action -> action(value) } } },
        content = { currentValue?.let { value -> content(value) } },
    )
}
