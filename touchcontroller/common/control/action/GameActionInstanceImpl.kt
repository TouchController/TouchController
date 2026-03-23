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
import top.fifthlight.touchcontroller.api.v1.action.GameAction
import top.fifthlight.touchcontroller.api.v1.action.GameActionInstance

@Serializable(with = GameActionInstanceSerializerProviderImpl::class)
class GameActionInstanceImpl(
    val hidden: Boolean = false,
    val name: Text,
    val action: GameAction,
) : GameActionInstance {
    operator fun invoke() = action.action()
}

class GameActionInstanceSerializerProviderImpl : KSerializer<GameActionInstanceImpl> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        serialName = "top.fifthlight.touchcontroller.common.control.action.GameActionInstanceImpl",
        kind = PrimitiveKind.STRING,
    )

    override fun serialize(
        encoder: Encoder,
        value: GameActionInstanceImpl,
    ) = encoder.encodeString(
        GameActions.registry.getId(value)
            ?: throw SerializationException("GameActionInstance $value not registered")
    )

    override fun deserialize(decoder: Decoder) = decoder.decodeString().let {
        GameActions.registry[it] ?: GameActions.unknown
    }
}
