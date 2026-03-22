package top.fifthlight.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*

@Serializable(with = IntPaddingSerializer::class)
data class IntPadding(
    val left: Int = 0,
    val top: Int = 0,
    val right: Int = 0,
    val bottom: Int = 0,
) {
    constructor(padding: Int) : this(padding, padding, padding, padding)
    constructor(horizontal: Int = 0, vertical: Int = 0) : this(horizontal, vertical, horizontal, vertical)

    companion object {
        val ZERO = IntPadding(0, 0, 0, 0)
    }

    val width: Int
        get() = left + right

    val height: Int
        get() = top + bottom

    val leftTopOffset: IntOffset
        get() = IntOffset(left, top)

    val size: IntSize
        get() = IntSize(width, height)

    operator fun plus(other: IntPadding) = IntPadding(
        left = left + other.left,
        top = top + other.top,
        right = right + other.right,
        bottom = bottom + other.bottom
    )

    operator fun minus(other: IntPadding) = IntPadding(
        left = left - other.left,
        top = top - other.top,
        right = right - other.right,
        bottom = bottom - other.bottom
    )

    operator fun times(num: Int) = IntPadding(
        left = left * num,
        top = top * num,
        right = right * num,
        bottom = bottom * num
    )

    override fun toString(): String {
        return "IntPadding(left=$left, top=$top, right=$right, bottom=$bottom)"
    }
}

class IntPaddingSerializer : KSerializer<IntPadding> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("top.fifthlight.data.IntPadding") {
        element<Int>("left")
        element<Int>("top")
        element<Int>("right")
        element<Int>("bottom")
    }

    override fun serialize(encoder: Encoder, value: IntPadding) = encoder.encodeStructure(descriptor) {
        encodeIntElement(descriptor, 0, value.left)
        encodeIntElement(descriptor, 1, value.top)
        encodeIntElement(descriptor, 2, value.right)
        encodeIntElement(descriptor, 3, value.bottom)
    }

    override fun deserialize(decoder: Decoder): IntPadding = decoder.decodeStructure(descriptor) {
        var left: Int? = null
        var top: Int? = null
        var right: Int? = null
        var bottom: Int? = null
        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                0 -> left = decodeIntElement(descriptor, 0)
                1 -> top = decodeIntElement(descriptor, 1)
                2 -> right = decodeIntElement(descriptor, 2)
                3 -> bottom = decodeIntElement(descriptor, 3)
                CompositeDecoder.DECODE_DONE -> break
                else -> error("Unexpected index: $index")
            }
        }
        require(left != null) { "Missing left in IntPadding" }
        require(top != null) { "Missing top in IntPadding" }
        require(right != null) { "Missing right in IntPadding" }
        require(bottom != null) { "Missing bottom in IntPadding" }
        IntPadding(left, top, right, bottom)
    }
}
