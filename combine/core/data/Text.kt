package top.fifthlight.combine.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import top.fifthlight.mergetools.api.ExpectFactory

enum class TextColor {
    BLACK,
    DARK_BLUE,
    DARK_GREEN,
    DARK_AQUA,
    DARK_RED,
    DARK_PURPLE,
    GOLD,
    GRAY,
    DARK_GRAY,
    BLUE,
    GREEN,
    AQUA,
    RED,
    LIGHT_PURPLE,
    YELLOW,
    WHITE,
}

data class TextStyle(
    val bold: Boolean = false,
    val underline: Boolean = false,
    val italic: Boolean = false,
    val color: TextColor? = null,
) {
    val haveStyle: Boolean
        get() = bold || underline || italic || color != null

    companion object {
        val default = TextStyle()
    }
}

interface TextBuilder {
    fun bold(bold: Boolean = true, block: TextBuilder.() -> Unit)
    fun underline(underline: Boolean = true, block: TextBuilder.() -> Unit)
    fun italic(italic: Boolean = true, block: TextBuilder.() -> Unit)
    fun color(color: TextColor, block: TextBuilder.() -> Unit)
    fun style(textStyle: TextStyle, block: TextBuilder.() -> Unit)

    fun append(string: String)
    fun appendWithoutStyle(text: Text)
}

interface TextFactory {
    fun build(block: TextBuilder.() -> Unit): Text
    fun literal(string: String): Text
    fun of(identifier: Identifier): Text
    fun of(id: String): Text
    fun empty(): Text
    fun format(identifier: Identifier, vararg arguments: Any?): Text
    fun format(id: String, vararg arguments: Any?): Text
    fun toNative(text: Text): Any

    @ExpectFactory
    interface Factory {
        fun of(): TextFactory
    }

    companion object : TextFactory by TextFactoryFactory.of()
}

interface Text {
    val string: String

    fun bold(): Text
    fun underline(): Text
    fun italic(): Text
    fun color(color: TextColor): Text
    fun style(textStyle: TextStyle): Text

    fun copy(): Text
    operator fun plus(other: Text): Text

    @Composable
    fun native(): Any = TextFactory.toNative(this)

    companion object {
        fun translatable(identifier: Identifier) = TextFactory.of(identifier)

        fun format(identifier: Identifier, vararg arguments: Any?): Text {
            val outArguments = Array(arguments.size) { index ->
                val item = arguments[index]
                if (item is Text) {
                    TextFactory.toNative(item)
                } else {
                    item
                }
            }
            return TextFactory.format(identifier, *outArguments)
        }

        fun empty() = TextFactory.empty()

        fun literal(string: String) = TextFactory.literal(string)

        fun build(block: TextBuilder.() -> Unit) = TextFactory.build(block)
    }
}
