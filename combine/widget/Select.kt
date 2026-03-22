package top.fifthlight.combine.widget.ui

import androidx.compose.runtime.*
import top.fifthlight.combine.input.MutableInteractionSource
import top.fifthlight.combine.layout.Alignment
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.drawing.border
import top.fifthlight.combine.modifier.focus.focusable
import top.fifthlight.combine.modifier.placement.anchor
import top.fifthlight.combine.modifier.pointer.clickable
import top.fifthlight.combine.paint.Drawable
import top.fifthlight.combine.theme.LocalTheme
import top.fifthlight.combine.ui.style.ColorTheme
import top.fifthlight.combine.ui.style.DrawableSet
import top.fifthlight.combine.ui.style.LocalColorTheme
import top.fifthlight.combine.widget.layout.Row
import top.fifthlight.combine.widget.layout.RowScope
import top.fifthlight.data.IntRect

data class SelectDrawableSet(
    val menuBox: DrawableSet,
    val floatPanel: Drawable,
    val itemUnselected: DrawableSet,
    val itemSelected: DrawableSet,
) {
    companion object {
        val current
            @Composable get() = LocalTheme.current.let { theme ->
                SelectDrawableSet(
                    menuBox = theme.drawables.selectMenuBox,
                    floatPanel = theme.drawables.selectFloatPanel,
                    itemUnselected = theme.drawables.selectItemUnselected,
                    itemSelected = theme.drawables.selectItemSelected,
                )
            }
    }
}

@Composable
fun SelectIcon(
    expanded: Boolean,
    modifier: Modifier = Modifier,
    upIcon: Drawable = LocalTheme.current.drawables.selectIconUp,
    downIcon: Drawable = LocalTheme.current.drawables.selectIconDown,
) {
    Icon(
        modifier = modifier,
        drawable = if (expanded) {
            upIcon
        } else {
            downIcon
        },
    )
}

@Composable
fun Select(
    modifier: Modifier = Modifier,
    drawableSet: SelectDrawableSet = SelectDrawableSet.current,
    colorTheme: ColorTheme? = null,
    expanded: Boolean = false,
    onExpandedChanged: (Boolean) -> Unit,
    dropDownContent: @Composable DropdownMenuScope.() -> Unit,
    content: @Composable RowScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val state by widgetState(interactionSource)
    val menuTexture = drawableSet.menuBox.getByState(state)

    var anchor by remember { mutableStateOf(IntRect.ZERO) }
    val colorTheme = colorTheme ?: ColorTheme.light

    Row(
        modifier = Modifier
            .border(menuTexture)
            .clickable(interactionSource) { onExpandedChanged(!expanded) }
            .focusable(interactionSource)
            .then(modifier)
            .anchor { anchor = it },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(
            LocalColorTheme provides colorTheme,
            LocalWidgetState provides state,
        ) {
            content()
        }
    }

    DropDownMenu(
        border = drawableSet.floatPanel,
        anchor = anchor,
        expanded = expanded,
        onDismissRequest = { onExpandedChanged(false) }
    ) {
        CompositionLocalProvider(
            LocalColorTheme provides colorTheme
        ) {
            dropDownContent()
        }
    }
}
