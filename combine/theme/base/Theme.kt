package top.fifthlight.combine.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import top.fifthlight.combine.core.paint.Colors.SECONDARY_WHITE
import top.fifthlight.combine.core.paint.Drawable
import top.fifthlight.combine.ui.style.*

val LocalTheme = staticCompositionLocalOf { SimpleTheme }

data class Theme(
    val drawables: Drawables = Drawables(),
    val colors: Colors = Colors(),
    val textStyles: TextStyles = TextStyles(),
) {
    data class Drawables(
        val button: DrawableSet = EmptyDrawableSet,
        val guideButton: DrawableSet = button,
        val warningButton: DrawableSet = button,

        val textButton: DrawableSet = EmptyDrawableSet,

        val alertDialogBackground: Drawable = Drawable.Empty,

        val uncheckedCheckBox: DrawableSet = EmptyDrawableSet,
        val checkboxChecked: DrawableSet = uncheckedCheckBox,

        val checkBoxButton: DrawableSet = EmptyDrawableSet,

        val colorPickerHandleChoice: Drawable = Drawable.Empty,
        val colorPickerSliderHandleHollow: DrawableSet = EmptyDrawableSet,

        val sliderActiveTrack: DrawableSet = EmptyDrawableSet,
        val sliderInactiveTrack: DrawableSet = EmptyDrawableSet,
        val sliderHandle: DrawableSet = EmptyDrawableSet,

        val switchFrame: DrawableSet = EmptyDrawableSet,
        val switchBackground: TextureSet = EmptyTextureSet,
        val switchHandle: DrawableSet = EmptyDrawableSet,

        val editText: DrawableSet = EmptyDrawableSet,

        val iconButton: DrawableSet = EmptyDrawableSet,
        val selectedIconButton: DrawableSet = iconButton,

        val selectMenuBox: DrawableSet = EmptyDrawableSet,
        val selectFloatPanel: Drawable = Drawable.Empty,
        val selectItemUnselected: DrawableSet = EmptyDrawableSet,
        val selectItemSelected: DrawableSet = EmptyDrawableSet,

        val selectIconUp: Drawable = Drawable.Empty,
        val selectIconDown: Drawable = Drawable.Empty,

        val radioUnchecked: DrawableSet = EmptyDrawableSet,
        val radioChecked: DrawableSet = EmptyDrawableSet,

        val radioBoxBorder: Drawable = Drawable.Empty,

        val tab: DrawableSet = EmptyDrawableSet,

        val itemGridBackground: Drawable? = null,
    )

    data class Colors(
        val button: ColorThemeSet = ColorThemeSet(
            normal = ColorTheme.dark,
            disabled = ColorTheme.light.copy(foreground = SECONDARY_WHITE)
        ),
        val guideButton: ColorThemeSet = button,
        val warningButton: ColorThemeSet = button,

        val textButton: ColorThemeSet = button,

        val select: ColorThemeSet = ColorThemeSet(
            normal = ColorTheme.light,
            disabled = ColorTheme.light.copy(foreground = SECONDARY_WHITE)
        ),
    )

    data class TextStyles(
        val button: TextStyleSet = EmptyTextStyleSet,
        val guideButton: TextStyleSet = EmptyTextStyleSet,
        val warningButton: TextStyleSet = EmptyTextStyleSet,

        val textButton: TextStyleSet = EmptyTextStyleSet,

        val select: TextStyleSet = EmptyTextStyleSet,
    )
}

@Composable
operator fun Theme.invoke(block: @Composable () -> Unit): Unit = CompositionLocalProvider(LocalTheme provides this, block)
