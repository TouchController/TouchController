package top.fifthlight.combine.widget.ui

import androidx.compose.runtime.*
import top.fifthlight.combine.core.data.plus
import top.fifthlight.combine.core.input.interaction.MutableInteractionSource
import top.fifthlight.combine.core.layout.Alignment
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.drawing.border
import top.fifthlight.combine.core.modifier.focus.focusable
import top.fifthlight.combine.core.modifier.placement.minSize
import top.fifthlight.combine.core.modifier.placement.padding
import top.fifthlight.combine.core.modifier.pointer.clickable
import top.fifthlight.combine.core.sound.LocalSoundManager
import top.fifthlight.combine.core.sound.SoundKind
import top.fifthlight.combine.core.widget.layout.Box
import top.fifthlight.combine.core.widget.layout.BoxScope
import top.fifthlight.combine.theme.LocalTheme
import top.fifthlight.combine.ui.style.*
import top.fifthlight.data.IntPadding
import top.fifthlight.data.IntSize

@NonSkippableComposable
@Composable
fun GuideButton(
    modifier: Modifier = Modifier,
    focusable: Boolean = true,
    drawableSet: DrawableSet = LocalTheme.current.drawables.guideButton,
    colorThemeSet: ColorThemeSet = LocalTheme.current.colors.guideButton,
    textStyleSet: TextStyleSet = LocalTheme.current.textStyles.guideButton,
    minSize: IntSize = IntSize(48, 20),
    padding: IntPadding = IntPadding(left = 4, right = 4),
    enabled: Boolean = true,
    onClick: () -> Unit,
    clickSound: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) = Button(
    modifier = modifier,
    focusable = focusable,
    drawableSet = drawableSet,
    colorThemeSet = colorThemeSet,
    textStyleSet = textStyleSet,
    minSize = minSize,
    padding = padding,
    enabled = enabled,
    onClick = onClick,
    clickSound = clickSound,
    content = content
)

@NonSkippableComposable
@Composable
fun WarningButton(
    modifier: Modifier = Modifier,
    focusable: Boolean = true,
    drawableSet: DrawableSet = LocalTheme.current.drawables.warningButton,
    colorThemeSet: ColorThemeSet = LocalTheme.current.colors.warningButton,
    textStyleSet: TextStyleSet = LocalTheme.current.textStyles.warningButton,
    minSize: IntSize = IntSize(48, 20),
    padding: IntPadding = IntPadding(left = 4, right = 4),
    enabled: Boolean = true,
    onClick: () -> Unit,
    clickSound: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) = Button(
    modifier = modifier,
    focusable = focusable,
    drawableSet = drawableSet,
    colorThemeSet = colorThemeSet,
    textStyleSet = textStyleSet,
    minSize = minSize,
    padding = padding,
    enabled = enabled,
    onClick = onClick,
    clickSound = clickSound,
    content = content
)

@NonSkippableComposable
@Composable
fun TextButton(
    modifier: Modifier = Modifier,
    focusable: Boolean = true,
    drawableSet: DrawableSet = LocalTheme.current.drawables.textButton,
    colorThemeSet: ColorThemeSet = LocalTheme.current.colors.textButton,
    textStyleSet: TextStyleSet = LocalTheme.current.textStyles.textButton,
    padding: IntPadding = IntPadding(left = 8, right = 8, top = 1),
    minSize: IntSize = IntSize(width = 0, height = 20),
    enabled: Boolean = true,
    onClick: () -> Unit,
    clickSound: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) = Button(
    modifier = modifier,
    focusable = focusable,
    drawableSet = drawableSet,
    colorThemeSet = colorThemeSet,
    textStyleSet = textStyleSet,
    minSize = minSize,
    padding = padding,
    enabled = enabled,
    onClick = onClick,
    clickSound = clickSound,
    content = content
)

@Composable
fun Button(
    modifier: Modifier = Modifier,
    focusable: Boolean = true,
    drawableSet: DrawableSet = LocalTheme.current.drawables.button,
    colorThemeSet: ColorThemeSet = LocalTheme.current.colors.button,
    textStyleSet: TextStyleSet = LocalTheme.current.textStyles.button,
    minSize: IntSize = IntSize(48, 20),
    padding: IntPadding = IntPadding(left = 4, right = 4),
    enabled: Boolean = true,
    onClick: () -> Unit,
    clickSound: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val soundManager = LocalSoundManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val state by widgetState(interactionSource)
    val drawable = drawableSet.getByState(state, enabled = enabled)
    val colorTheme = colorThemeSet.getByState(state, enabled = enabled)
    val textStyle = textStyleSet.getByState(state, enabled = enabled) + LocalTextStyle.current

    val clickableModifier = Modifier.clickable(interactionSource) {
        if (clickSound) {
            soundManager.play(SoundKind.BUTTON_PRESS, 1f)
        }
        onClick()
    }
    val focusableModifier = Modifier.focusable(interactionSource)

    fun Modifier.then(modifier: Modifier?) = if (modifier != null) {
        then(modifier)
    } else {
        this
    }

    Box(
        modifier = Modifier
            .padding(padding)
            .border(drawable)
            .minSize(minSize)
            .then(clickableModifier.takeIf { enabled })
            .then(focusableModifier.takeIf { enabled && focusable })
            .then(modifier),
        alignment = Alignment.Center,
    ) {
        CompositionLocalProvider(
            LocalColorTheme provides colorTheme,
            LocalTextStyle provides textStyle,
            LocalWidgetState provides state,
        ) {
            content()
        }
    }
}
