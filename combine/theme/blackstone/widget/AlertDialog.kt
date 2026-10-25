package top.fifthlight.combine.theme.blackstone.widget

import androidx.compose.runtime.*
import top.fifthlight.combine.core.animation.animateFloatAsState
import top.fifthlight.combine.core.layout.Alignment
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.drawing.background
import top.fifthlight.combine.core.modifier.drawing.border
import top.fifthlight.combine.core.modifier.drawing.clip
import top.fifthlight.combine.core.modifier.placement.offset
import top.fifthlight.combine.core.modifier.placement.padding
import top.fifthlight.combine.core.paint.Color
import top.fifthlight.combine.core.paint.Colors
import top.fifthlight.combine.core.paint.Drawable
import top.fifthlight.combine.core.widget.Dialog
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.core.widget.layout.ColumnScope
import top.fifthlight.combine.core.widget.layout.Row
import top.fifthlight.combine.core.widget.layout.RowScope
import top.fifthlight.combine.theme.blackstone.BlackstoneTextures

val LocalAlertDialogBackground = staticCompositionLocalOf { BlackstoneTextures.widget_background_background_gray }

@Composable
fun AlertDialog(
    modifier: Modifier = Modifier,
    visible: Boolean,
    background: Drawable = LocalAlertDialogBackground.current,
    onDismissRequest: (() -> Unit)? = null,
    title: @Composable () -> Unit = {},
    actions: (@Composable RowScope.() -> Unit)? = null,
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
                .offset(y = (1f - showingProgress) * .2f)
                .clip(width = 1f, height = showingProgress)
                .then(modifier),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8)
        ) {
            title()
            content()
            actions?.let {
                Row(
                    modifier = Modifier.alignment(Alignment.Right),
                    horizontalArrangement = Arrangement.spacedBy(8),
                ) {
                    actions()
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
    background: Drawable = LocalAlertDialogBackground.current,
    noinline onDismissRequest: (() -> Unit)? = null,
    crossinline title: @Composable (V) -> Unit = {},
    noinline actions: (@Composable RowScope.(V) -> Unit)? = null,
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
        actions = { currentValue?.let { value -> actions?.invoke(this, value) } },
        content = { currentValue?.let { value -> content(value) } },
    )
}
