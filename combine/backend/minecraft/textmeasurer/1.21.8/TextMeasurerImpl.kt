package top.fifthlight.combine.backend.minecraft.textmeasurer.v1_21_8

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
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

    val font: Font = Minecraft.getInstance().font

    override fun measure(text: String) = IntSize(
        width = font.split(Component.literal(text), Int.MAX_VALUE).maxOfOrNull { font.width(it) } ?: 0,
        height = font.wordWrapHeight(text, Int.MAX_VALUE),
    )

    override fun measure(text: String, maxWidth: Int) = maxWidth.coerceAtLeast(0).let { maxWidth ->
        IntSize(
            width = font.split(Component.literal(text), maxWidth)
                .maxOfOrNull { font.width(it) }
                ?.coerceIn(0, maxWidth) ?: 0,
            height = font.wordWrapHeight(text, maxWidth),
        )
    }

    override fun measure(text: Text) = IntSize(
        width = font.split(text.toMinecraft(), Int.MAX_VALUE).maxOfOrNull { font.width(it) } ?: 0,
        height = font.wordWrapHeight(text.toMinecraft(), Int.MAX_VALUE),
    )

    override fun measure(text: Text, maxWidth: Int) = maxWidth.coerceAtLeast(0).let { maxWidth ->
        IntSize(
            width = font.split(text.toMinecraft(), maxWidth)
                .maxOfOrNull { font.width(it) }
                ?.coerceIn(0, maxWidth) ?: 0,
            height = font.wordWrapHeight(text.toMinecraft(), maxWidth),
        )
    }
}
