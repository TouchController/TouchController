package top.fifthlight.combine.ui.style

import androidx.compose.runtime.staticCompositionLocalOf
import top.fifthlight.combine.data.Text
import top.fifthlight.combine.data.TextFactory

val LocalTextStyle = staticCompositionLocalOf { TextStyle.default }

data class TextStyle(
    val bold: Boolean = false,
    val underline: Boolean = false,
    val italic: Boolean = false,
) {
    val haveStyle: Boolean
        get() = bold || underline || italic

    fun applyOnString(textFactory: TextFactory, string: String) = textFactory.build {
        bold(bold) {
            underline(underline) {
                italic(italic) {
                    append(string)
                }
            }
        }
    }

    fun applyOnText(textFactory: TextFactory, text: Text) = textFactory.build {
        bold(bold) {
            underline(underline) {
                italic(italic) {
                    appendWithoutStyle(text)
                }
            }
        }
    }

    companion object {
        val default = TextStyle()
    }
}
