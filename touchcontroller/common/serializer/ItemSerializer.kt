package top.fifthlight.touchcontroller.common.serializer

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.serializer
import top.fifthlight.combine.data.Identifier
import top.fifthlight.combine.item.data.Item
import top.fifthlight.combine.item.data.ItemFactory

class ItemSerializer : KSerializer<Item> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("top.fifthlight.combine.data.Item") {
        element<Identifier>("id")
    }

    override fun serialize(encoder: Encoder, value: Item) = encoder.encodeStructure(descriptor) {
        encodeSerializableElement(descriptor, 0, serializer<Identifier>(), value.id)
    }

    override fun deserialize(decoder: Decoder): Item {
        return decoder.decodeStructure(descriptor) {
            var id: Identifier? = null
            while (true) {
                @OptIn(ExperimentalSerializationApi::class)
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> id = decodeSerializableElement(descriptor, 0, serializer<Identifier>())
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }
            require(id != null) { "No id provided" }
            ItemFactory.create(id) ?: error("Bad item identifier: $id")
        }
    }
}
