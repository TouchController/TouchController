package top.fifthlight.combine.backend.minecraft.textmeasurer.v26_1

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import top.fifthlight.combine.backend.minecraft.text.v26_1.toMinecraft
import top.fifthlight.combine.data.Text
import top.fifthlight.combine.text.TextMeasurer
import top.fifthlight.data.IntSize
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl

@ActualImpl(TextMeasurer::class)
object TextMeasurerImpl : TextMeasurer {
    @JvmStatic
    @ActualConstructor
    fun of(): TextMeasurer = TextMeasurerImpl

    val font: Font = Minecraft.getInstance().font

    private fun measure(text: FormattedText) = IntSize(
        width = font.split(text, Int.MAX_VALUE).maxOfOrNull { font.width(it) } ?: 0,
        height = font.wordWrapHeight(text, Int.MAX_VALUE),
    )

    private fun measure(text: FormattedText, maxWidth: Int) = maxWidth.coerceAtLeast(0).let { maxWidth ->
        IntSize(
            width = font.split(text, maxWidth)
                .maxOfOrNull { font.width(it) }
                ?.coerceIn(0, maxWidth) ?: 0,
            height = font.wordWrapHeight(text, maxWidth),
        )
    }

    override fun measure(text: String) = measure(Component.literal(text))

    override fun measure(text: String, maxWidth: Int) = measure(Component.literal(text), maxWidth)

    override fun measure(text: Text) = measure(text.toMinecraft())

    override fun measure(text: Text, maxWidth: Int) = measure(text.toMinecraft(), maxWidth)
}
