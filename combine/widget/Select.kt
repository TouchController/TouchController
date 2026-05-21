package top.fifthlight.combine.widget.ui

import androidx.compose.runtime.*
import top.fifthlight.combine.core.data.plus
import top.fifthlight.combine.core.input.interaction.MutableInteractionSource
import top.fifthlight.combine.core.layout.Alignment
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.drawing.border
import top.fifthlight.combine.core.modifier.focus.focusable
import top.fifthlight.combine.core.modifier.placement.anchor
import top.fifthlight.combine.core.modifier.pointer.clickable
import top.fifthlight.combine.core.paint.Drawable
import top.fifthlight.combine.core.widget.layout.Row
import top.fifthlight.combine.core.widget.layout.RowScope
import top.fifthlight.combine.theme.LocalTheme
import top.fifthlight.combine.ui.style.*
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
    colorThemeSet: ColorThemeSet = LocalTheme.current.colors.select,
    textStyleSet: TextStyleSet = LocalTheme.current.textStyles.select,
    expanded: Boolean = false,
    onExpandedChanged: (Boolean) -> Unit,
    dropDownContent: @Composable DropdownMenuScope.() -> Unit,
    content: @Composable RowScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val state by widgetState(interactionSource)
    val menuTexture = drawableSet.menuBox.getByState(state)
    val colorTheme = colorThemeSet.getByState(state)
    val textTheme = textStyleSet.getByState(state) + LocalTextStyle.current

    var anchor by remember { mutableStateOf(IntRect.ZERO) }

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
            LocalTextStyle provides textTheme,
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
            LocalColorTheme provides colorTheme,
            LocalTextStyle provides textTheme,
        ) {
            dropDownContent()
        }
    }
}
