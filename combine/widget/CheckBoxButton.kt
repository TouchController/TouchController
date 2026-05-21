package top.fifthlight.combine.widget.ui

import androidx.compose.runtime.Composable
import top.fifthlight.combine.core.layout.Alignment
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.widget.layout.Row
import top.fifthlight.combine.core.widget.layout.RowScope
import top.fifthlight.combine.theme.LocalTheme
import top.fifthlight.combine.ui.style.ColorThemeSet
import top.fifthlight.combine.ui.style.DrawableSet
import top.fifthlight.data.IntSize

@Composable
fun CheckBoxButton(
    modifier: Modifier = Modifier,
    drawableSet: DrawableSet = LocalTheme.current.drawables.checkBoxButton,
    checkBoxDrawableSet: CheckBoxDrawableSet = CheckBoxDrawableSet.current,
    colorThemeSet: ColorThemeSet = LocalTheme.current.colors.button,
    minSize: IntSize = IntSize(48, 20),
    enabled: Boolean = true,
    checked: Boolean = false,
    onClick: () -> Unit,
    clickSound: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) = Button(
    modifier = modifier,
    drawableSet = drawableSet,
    colorThemeSet = colorThemeSet,
    minSize = minSize,
    enabled = enabled,
    onClick = onClick,
    clickSound = clickSound,
    content = {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4),
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()

            val state = LocalWidgetState.current
            val drawableSet = if (checked) {
                checkBoxDrawableSet.checked
            } else {
                checkBoxDrawableSet.unchecked
            }
            Icon(drawableSet.getByState(state))
        }
    }
)
