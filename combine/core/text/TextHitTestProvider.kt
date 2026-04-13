package top.fifthlight.combine.text

import top.fifthlight.combine.data.Text
import top.fifthlight.data.IntOffset
import top.fifthlight.mergetools.api.ExpectFactory

interface TextHitTestProvider {
    fun hitTest(
        text: Text,
        maxWidth: Int,
        position: IntOffset,
        acceptInsertion: Boolean = false,
    ): TextHitTestResult?

    @ExpectFactory
    interface Factory {
        fun of(): TextHitTestProvider
    }

    companion object : TextHitTestProvider by TextHitTestProviderFactory.of()
}

sealed interface TextHitTestResult {
    data class InsertText(
        val text: String,
        val overwrite: Boolean = false,
    ) : TextHitTestResult

    fun interface Native : TextHitTestResult {
        fun action()
    }
}
