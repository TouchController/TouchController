package top.fifthlight.combine.backend.minecraft.textbuilder.v26_2

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.contents.PlainTextContents
import top.fifthlight.combine.backend.minecraft.text.v26_2.TextImpl
import top.fifthlight.combine.backend.minecraft.text.v26_2.toFormatting
import top.fifthlight.combine.backend.minecraft.text.v26_2.toMinecraft
import top.fifthlight.combine.backend.minecraft.text.v26_2.toVanilla
import top.fifthlight.combine.data.TextBuilder
import top.fifthlight.combine.data.TextColor
import top.fifthlight.combine.data.TextStyle
import top.fifthlight.combine.data.Text as CombineText

class TextBuilderImpl(
    private val text: MutableComponent = Component.empty(),
    private val style: Style = Style.EMPTY,
) : TextBuilder {
    override fun bold(bold: Boolean, block: TextBuilder.() -> Unit) {
        block(
            TextBuilderImpl(
                text = text,
                style = style.withBold(bold),
            )
        )
    }

    override fun underline(underline: Boolean, block: TextBuilder.() -> Unit) {
        block(
            TextBuilderImpl(
                text = text,
                style = style.withUnderlined(underline),
            )
        )
    }

    override fun italic(italic: Boolean, block: TextBuilder.() -> Unit) {
        block(
            TextBuilderImpl(
                text = text,
                style = style.withItalic(italic),
            )
        )
    }

    override fun color(color: TextColor, block: TextBuilder.() -> Unit) {
        block(
            TextBuilderImpl(
                text = text,
                style = style.withColor(color.toFormatting()),
            )
        )
    }

    override fun style(textStyle: TextStyle, block: TextBuilder.() -> Unit) {
        block(
            TextBuilderImpl(
                text = text,
                style = textStyle.toVanilla().applyTo(style),
            )
        )
    }

    override fun append(string: String) {
        this.text.append(MutableComponent.create(PlainTextContents.create(string)).setStyle(style))
    }

    override fun appendWithoutStyle(text: CombineText) {
        this.text.append(MutableComponent.create(text.toMinecraft().contents).setStyle(style))
    }

    fun build(): CombineText = TextImpl(text)
}
