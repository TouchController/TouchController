package top.fifthlight.combine.widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import top.fifthlight.combine.input.InteractionSource
import top.fifthlight.combine.input.MutableInteractionSource
import top.fifthlight.combine.layout.Alignment
import top.fifthlight.combine.layout.Arrangement
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.drawing.border
import top.fifthlight.combine.modifier.focus.focusable
import top.fifthlight.combine.modifier.placement.padding
import top.fifthlight.combine.modifier.pointer.clickable
import top.fifthlight.combine.modifier.pointer.toggleable
import top.fifthlight.combine.paint.Drawable
import top.fifthlight.combine.sound.LocalSoundManager
import top.fifthlight.combine.sound.SoundKind
import top.fifthlight.combine.sound.SoundManager
import top.fifthlight.combine.theme.LocalTheme
import top.fifthlight.combine.ui.style.ColorTheme
import top.fifthlight.combine.ui.style.DrawableSet
import top.fifthlight.combine.ui.style.LocalColorTheme
import top.fifthlight.combine.widget.layout.Column
import top.fifthlight.combine.widget.layout.ColumnScope
import top.fifthlight.combine.widget.layout.Row
import top.fifthlight.combine.widget.layout.RowScope

data class RadioDrawableSet(
    val unchecked: DrawableSet,
    val checked: DrawableSet,
) {
    companion object {
        val current
            @Composable get() = LocalTheme.current.let { theme ->
                RadioDrawableSet(
                    unchecked = theme.drawables.radioUnchecked,
                    checked = theme.drawables.radioChecked,
                )
            }
    }
}

@Composable
fun RadioIcon(
    modifier: Modifier = Modifier,
    interactionSource: InteractionSource,
    drawableSet: RadioDrawableSet = RadioDrawableSet.current,
    value: Boolean,
) {
    val currentDrawableSet = if (value) {
        drawableSet.checked
    } else {
        drawableSet.unchecked
    }
    val state by widgetState(interactionSource)
    val drawable = currentDrawableSet.getByState(state)

    Icon(
        modifier = modifier,
        drawable = drawable,
    )
}

@Composable
fun RadioRow(
    modifier: Modifier = Modifier,
    border: Drawable = LocalTheme.current.drawables.radioBoxBorder,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .padding(4)
            .border(border)
            .then(modifier),
        horizontalArrangement = Arrangement.spacedBy(4),
    ) {
        CompositionLocalProvider(
            LocalColorTheme provides ColorTheme.light,
        ) {
            content()
        }
    }
}

@Composable
fun RadioColumn(
    modifier: Modifier = Modifier,
    border: Drawable = LocalTheme.current.drawables.radioBoxBorder,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .padding(4)
            .border(border)
            .then(modifier),
        verticalArrangement = Arrangement.spacedBy(4),
    ) {
        CompositionLocalProvider(
            LocalColorTheme provides ColorTheme.light,
        ) {
            content()
        }
    }
}

@Composable
fun RadioBoxItem(
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    value: Boolean,
    onValueChanged: (Boolean) -> Unit,
    clickSound: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val soundManager: SoundManager = LocalSoundManager.current
    Row(
        modifier = modifier.toggleable(
            interactionSource = interactionSource,
            value = value,
            onValueChanged = {
                if (clickSound) {
                    soundManager.play(SoundKind.BUTTON_PRESS, 1f)
                }
                onValueChanged(it)
            },
        ),
        horizontalArrangement = Arrangement.spacedBy(4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioIcon(
            interactionSource = interactionSource,
            value = value,
        )
        content()
    }
}

@Composable
fun Radio(
    modifier: Modifier = Modifier,
    drawableSet: RadioDrawableSet = RadioDrawableSet.current,
    value: Boolean,
    onValueChanged: ((Boolean) -> Unit)?,
) {
    val interactionSource = remember { MutableInteractionSource() }

    val modifier = if (onValueChanged == null) {
        modifier
    } else {
        Modifier
            .clickable(interactionSource) {
                onValueChanged(!value)
            }
            .focusable(interactionSource)
            .then(modifier)
    }

    RadioIcon(
        modifier = modifier,
        interactionSource = interactionSource,
        drawableSet = drawableSet,
        value = value,
    )
}
