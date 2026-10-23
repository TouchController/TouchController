package top.fifthlight.combine.backend.minecraft.hittest.v1_20_1

import net.minecraft.client.Minecraft
import net.minecraft.network.chat.ClickEvent
import top.fifthlight.combine.backend.minecraft.text.v1_20_1.toMinecraft
import top.fifthlight.combine.backend.minecraft.textmeasurer.v1_20_1.TextMeasurerImpl
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.text.TextHitTestProvider
import top.fifthlight.combine.core.text.TextHitTestResult
import top.fifthlight.data.IntOffset
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl

@ActualImpl(TextHitTestProvider::class)
object TextHitTestProviderImpl : TextHitTestProvider {
    @JvmStatic
    @ActualConstructor
    fun of(): TextHitTestProvider = this

    override fun hitTest(
        text: Text,
        maxWidth: Int,
        position: IntOffset,
        acceptInsertion: Boolean,
    ): TextHitTestResult? {
        if (position.x !in 0 until maxWidth) return null
        if (position.y < 0) return null

        val font = TextMeasurerImpl.font
        val textLines = font.split(text.toMinecraft(), maxWidth)
        val line = textLines.getOrNull(position.y / font.lineHeight) ?: return null
        val result = font.splitter.componentStyleAtWidth(line, position.x) ?: return null

        if (acceptInsertion) {
            return result.insertion?.let {
                TextHitTestResult.InsertText(
                    text = it,
                    overwrite = false,
                )
            }
        }
        val clientEvent = result.clickEvent ?: return null
        return when (clientEvent.action) {
            ClickEvent.Action.SUGGEST_COMMAND -> TextHitTestResult.InsertText(
                text = clientEvent.value,
                overwrite = true,
            )

            else -> TextHitTestResult.Native {
                val client = Minecraft.getInstance()
                client.screen?.handleComponentClicked(result)
            }
        }
    }
}
