package top.fifthlight.combine.widget.ui

import androidx.compose.runtime.Composable
import top.fifthlight.combine.data.Text
import top.fifthlight.combine.data.TextFactory
import top.fifthlight.combine.data.TextStyle
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.paint.Color
import top.fifthlight.combine.ui.style.LocalColorTheme
import top.fifthlight.combine.ui.style.LocalTextStyle
import top.fifthlight.combine.widget.BaseText

@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = LocalColorTheme.current.foreground,
    textStyle: TextStyle = LocalTextStyle.current,
) = if (textStyle.haveStyle) {
    BaseText(
        text = TextFactory.build { style(textStyle) { append(text) } },
        modifier = modifier,
        color = color,
    )
} else {
    BaseText(
        text = text,
        modifier = modifier,
        color = color,
    )
}

@Composable
fun Text(
    text: Text,
    modifier: Modifier = Modifier,
    color: Color = LocalColorTheme.current.foreground,
    textStyle: TextStyle = LocalTextStyle.current,
) = if (textStyle.haveStyle) {
    BaseText(
        text = text.style(textStyle),
        modifier = modifier,
        color = color,
    )
} else {
    BaseText(
        text = text,
        modifier = modifier,
        color = color,
    )
}
