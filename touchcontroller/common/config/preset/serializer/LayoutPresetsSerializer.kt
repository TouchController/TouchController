package top.fifthlight.touchcontroller.common.config.preset.serializer

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
import top.fifthlight.touchcontroller.common.config.preset.LayoutPreset
import top.fifthlight.touchcontroller.common.config.preset.LayoutPresets
import top.fifthlight.touchcontroller.common.control.ControllerWidget

class LayoutPresetsSerializer : KSerializer<LayoutPresets> {
    @OptIn(SealedSerializationApi::class)
    private class PersistentListDescriptor : SerialDescriptor by serialDescriptor<List<ControllerWidget>>() {
        @OptIn(ExperimentalSerializationApi::class)
        override val serialName: String = "top.fifthlight.touchcontroller.config.LayoutPresets"
    }

    private val itemSerializer = serializer<LayoutPreset>()
    private val delegatedSerializer = ListSerializer(itemSerializer)

    override val descriptor: SerialDescriptor = PersistentListDescriptor()

    override fun serialize(encoder: Encoder, value: LayoutPresets) =
        delegatedSerializer.serialize(encoder, value.presets)

    override fun deserialize(decoder: Decoder) = LayoutPresets(
        delegatedSerializer.deserialize(decoder).toPersistentList()
    )
}
