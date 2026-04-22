package top.fifthlight.combine.backend.minecraft.text.v26_2

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import top.fifthlight.combine.data.TextColor
import top.fifthlight.combine.data.TextStyle
import top.fifthlight.combine.data.Text as CombineText

fun TextColor.toFormatting() = when (this) {
    TextColor.BLACK -> ChatFormatting.BLACK
    TextColor.DARK_BLUE -> ChatFormatting.DARK_BLUE
    TextColor.DARK_GREEN -> ChatFormatting.DARK_GREEN
    TextColor.DARK_AQUA -> ChatFormatting.DARK_AQUA
    TextColor.DARK_RED -> ChatFormatting.DARK_RED
    TextColor.DARK_PURPLE -> ChatFormatting.DARK_PURPLE
    TextColor.GOLD -> ChatFormatting.GOLD
    TextColor.GRAY -> ChatFormatting.GRAY
    TextColor.DARK_GRAY -> ChatFormatting.DARK_GRAY
    TextColor.BLUE -> ChatFormatting.BLUE
    TextColor.GREEN -> ChatFormatting.GREEN
    TextColor.AQUA -> ChatFormatting.AQUA
    TextColor.RED -> ChatFormatting.RED
    TextColor.LIGHT_PURPLE -> ChatFormatting.LIGHT_PURPLE
    TextColor.YELLOW -> ChatFormatting.YELLOW
    TextColor.WHITE -> ChatFormatting.WHITE
}

fun TextStyle.toVanilla() = Style.EMPTY
    .withBold(bold)
    .withUnderlined(underline)
    .withItalic(italic)
    .withColor(color?.toFormatting())

class TextImpl(
    val inner: Component,
) : CombineText {
    override val string: String
        get() = inner.string

    override fun bold(): CombineText = TextImpl(MutableComponent.create(inner.contents).setStyle(STYLE_BOLD))

    override fun underline(): CombineText = TextImpl(MutableComponent.create(inner.contents).setStyle(STYLE_UNDERLINE))

    override fun italic(): CombineText = TextImpl(MutableComponent.create(inner.contents).setStyle(STYLE_ITALIC))

    override fun color(color: TextColor) =
        TextImpl(MutableComponent.create(inner.contents).withStyle(color.toFormatting()))

    override fun style(textStyle: TextStyle) =
        TextImpl(MutableComponent.create(inner.contents).withStyle(textStyle.toVanilla()))

    override fun copy(): CombineText = TextImpl(inner.copy())

    override fun plus(other: CombineText): CombineText = TextImpl(inner.copy().append(other.toMinecraft()))

    companion object {
        val EMPTY = TextImpl(Component.empty())
        private val STYLE_BOLD = Style.EMPTY.withBold(true)
        private val STYLE_UNDERLINE = Style.EMPTY.withUnderlined(true)
        private val STYLE_ITALIC = Style.EMPTY.withItalic(true)
    }
}

fun CombineText.toMinecraft() = (this as TextImpl).inner
