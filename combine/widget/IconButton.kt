package top.fifthlight.combine.widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonSkippableComposable
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.widget.layout.BoxScope
import top.fifthlight.combine.theme.LocalTheme
import top.fifthlight.combine.ui.style.ColorThemeSet
import top.fifthlight.combine.ui.style.DrawableSet
import top.fifthlight.data.IntPadding
import top.fifthlight.data.IntSize

@NonSkippableComposable
@Composable
fun IconButton(
    modifier: Modifier = Modifier,
    focusable: Boolean = true,
    selected: Boolean = false,
    drawableSet: DrawableSet = if (selected) {
        LocalTheme.current.drawables.selectedIconButton
    } else {
        LocalTheme.current.drawables.iconButton
    },
    colorThemeSet: ColorThemeSet = LocalTheme.current.colors.button,
    minSize: IntSize = IntSize(0, 0),
    padding: IntPadding = IntPadding(1),
    enabled: Boolean = true,
    onClick: () -> Unit,
    clickSound: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) = Button(
    modifier = modifier,
    focusable = focusable,
    drawableSet = drawableSet,
    colorThemeSet = colorThemeSet,
    minSize = minSize,
    padding = padding,
    enabled = enabled,
    onClick = onClick,
    clickSound = clickSound,
    content = content
)
