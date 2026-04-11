package top.fifthlight.combine.text

import top.fifthlight.combine.data.Text
import top.fifthlight.data.IntSize
import top.fifthlight.mergetools.api.ExpectFactory

interface TextMeasurer {
    fun measure(text: String): IntSize
    fun measure(text: String, maxWidth: Int): IntSize
    fun measure(text: Text): IntSize
    fun measure(text: Text, maxWidth: Int): IntSize

    @ExpectFactory
    interface Factory {
        fun of(): TextMeasurer
    }

    companion object : TextMeasurer by TextMeasurerFactory.of()
}
