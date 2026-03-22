package top.fifthlight.combine.ui.style

import androidx.compose.runtime.staticCompositionLocalOf
import top.fifthlight.combine.paint.Color
import top.fifthlight.combine.paint.Colors

val LocalColorTheme = staticCompositionLocalOf { ColorTheme.dark }

data class ColorTheme(
    val background: Color,
    val border: Color,
    val foreground: Color,
) {
    companion object {
        val dark = ColorTheme(
            background = Colors.BLACK,
            border = Colors.WHITE,
            foreground = Colors.WHITE,
        )
        val light = ColorTheme(
            background = Colors.WHITE,
            border = Colors.BLACK,
            foreground = Colors.BLACK,
        )
    }
}
