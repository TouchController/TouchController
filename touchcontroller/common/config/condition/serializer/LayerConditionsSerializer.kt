package top.fifthlight.touchcontroller.common.config.condition.serializer

import kotlinx.collections.immutable.toPersistentList
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SealedSerializationApi
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer
import top.fifthlight.touchcontroller.common.config.condition.LayerConditions

class LayerConditionsSerializer : KSerializer<LayerConditions> {
    @OptIn(SealedSerializationApi::class)
    private class PersistentMapDescriptor :
        SerialDescriptor by serialDescriptor<Map<LayerConditions.Key, LayerConditions.Value>>() {
        @ExperimentalSerializationApi
        override val serialName: String = "top.fifthlight.touchcontroller.common.config.condition.LayerConditions"
    }

    private val itemSerializer = serializer<LayerConditions.Item>()
    private val delegatedSerializer = ListSerializer(itemSerializer)

    override val descriptor: SerialDescriptor = PersistentMapDescriptor()

    override fun serialize(encoder: Encoder, value: LayerConditions) =
        delegatedSerializer.serialize(encoder, value.conditions)

    override fun deserialize(decoder: Decoder) =
        LayerConditions(delegatedSerializer.deserialize(decoder).toPersistentList())
}
