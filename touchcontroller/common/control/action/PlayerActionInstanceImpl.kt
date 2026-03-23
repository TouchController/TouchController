package top.fifthlight.touchcontroller.common.control.action

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import top.fifthlight.combine.data.Text
import top.fifthlight.touchcontroller.api.v1.action.PlayerActionInstance
import top.fifthlight.touchcontroller.common.gal.player.PlayerHandle

@Serializable(with = PlayerActionInstanceSerializerProviderImpl::class)
class PlayerActionInstanceImpl(
    val hidden: Boolean = false,
    val name: Text,
    val action: (PlayerHandle) -> Unit,
) : PlayerActionInstance {
    operator fun invoke(player: PlayerHandle) = action(player)
}

class PlayerActionInstanceSerializerProviderImpl : KSerializer<PlayerActionInstanceImpl> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        serialName = "top.fifthlight.touchcontroller.common.control.action.PlayerActionInstanceImpl",
        kind = PrimitiveKind.STRING,
    )

    override fun serialize(
        encoder: Encoder,
        value: PlayerActionInstanceImpl,
    ) = encoder.encodeString(
        PlayerActions.registry.getId(value)
            ?: throw SerializationException("PlayerActionInstance $value not registered")
    )

    override fun deserialize(decoder: Decoder) = decoder.decodeString().let {
        PlayerActions.registry[it] ?: PlayerActions.unknown
    }
}
