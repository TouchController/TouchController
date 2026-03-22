package top.fifthlight.combine.backend.minecraft.textmeasurer.v26_1

import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import top.fifthlight.combine.backend.minecraft.text.v26_1.toMinecraft
import top.fifthlight.combine.data.Text
import top.fifthlight.combine.paint.TextMeasurer
import top.fifthlight.data.IntSize
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl

@ActualImpl(TextMeasurer::class)
object TextMeasurerImpl : TextMeasurer {
    @JvmStatic
    @ActualConstructor
    fun of(): TextMeasurer = TextMeasurerImpl

    private val client = Minecraft.getInstance()
    private val textRenderer = client.font

    private fun measure(text: FormattedText) = IntSize(
        width = textRenderer.split(text, Int.MAX_VALUE).maxOfOrNull { textRenderer.width(it) } ?: 0,
        height = textRenderer.wordWrapHeight(text, Int.MAX_VALUE),
    )

    private fun measure(text: FormattedText, maxWidth: Int) = IntSize(
        width = textRenderer.split(text, maxWidth)
            .maxOfOrNull { textRenderer.width(it) }
            ?.coerceIn(0, maxWidth) ?: 0,
        height = textRenderer.wordWrapHeight(text, maxWidth),
    )

    override fun measure(text: String) = measure(Component.literal(text))

    override fun measure(text: String, maxWidth: Int) = measure(Component.literal(text), maxWidth)

    override fun measure(text: Text) = measure(text.toMinecraft())

    override fun measure(text: Text, maxWidth: Int) = measure(text.toMinecraft(), maxWidth)
}
