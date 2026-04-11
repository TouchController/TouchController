package top.fifthlight.combine.backend.minecraft.textmeasurer.v1_21_8

import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import top.fifthlight.combine.backend.minecraft.text.v1_21_8.toMinecraft
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

    private val client = Minecraft.getInstance()
    private val textRenderer = client.font

    override fun measure(text: String) = IntSize(
        width = textRenderer.split(Component.literal(text), Int.MAX_VALUE).maxOfOrNull { textRenderer.width(it) } ?: 0,
        height = textRenderer.wordWrapHeight(text, Int.MAX_VALUE),
    )

    override fun measure(text: String, maxWidth: Int) = maxWidth.coerceAtLeast(0).let { maxWidth ->
        IntSize(
            width = textRenderer.split(Component.literal(text), maxWidth)
                .maxOfOrNull { textRenderer.width(it) }
                ?.coerceIn(0, maxWidth) ?: 0,
            height = textRenderer.wordWrapHeight(text, maxWidth),
        )
    }

    override fun measure(text: Text) = IntSize(
        width = textRenderer.split(text.toMinecraft(), Int.MAX_VALUE).maxOfOrNull { textRenderer.width(it) } ?: 0,
        height = textRenderer.wordWrapHeight(text.toMinecraft(), Int.MAX_VALUE),
    )

    override fun measure(text: Text, maxWidth: Int) = maxWidth.coerceAtLeast(0).let { maxWidth ->
        IntSize(
            width = textRenderer.split(text.toMinecraft(), maxWidth)
                .maxOfOrNull { textRenderer.width(it) }
                ?.coerceIn(0, maxWidth) ?: 0,
            height = textRenderer.wordWrapHeight(text.toMinecraft(), maxWidth),
        )
    }
}
