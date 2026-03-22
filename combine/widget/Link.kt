package top.fifthlight.combine.widget.ui

import androidx.compose.runtime.Composable
import top.fifthlight.combine.data.Text
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.pointer.clickable
import top.fifthlight.combine.paint.Color
import top.fifthlight.combine.paint.Colors
import top.fifthlight.combine.ui.style.LocalTextStyle
import top.fifthlight.combine.ui.style.TextStyle

@Composable
fun Link(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    color: Color = Colors.BLUE,
    textStyle: TextStyle = LocalTextStyle.current.copy(underline = true),
) {
    Text(
        text = text,
        modifier = Modifier.clickable(onClick = onClick).then(modifier),
        color = color,
        textStyle = textStyle
    )
}

@Composable
fun Link(
    text: Text,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    color: Color = Colors.BLUE,
    textStyle: TextStyle = LocalTextStyle.current.copy(underline = true),
) {
    Text(
        text = text,
        modifier = Modifier.clickable(onClick = onClick).then(modifier),
        color = color,
        textStyle = textStyle,
    )
}
