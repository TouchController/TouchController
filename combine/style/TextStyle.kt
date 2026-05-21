package top.fifthlight.combine.ui.style

import androidx.compose.runtime.staticCompositionLocalOf
import top.fifthlight.combine.core.data.TextStyle

val LocalTextStyle = staticCompositionLocalOf { TextStyle.default }

typealias TextStyleSet = StyleSet<TextStyle>

val EmptyTextStyleSet = TextStyleSet(TextStyle.default)
