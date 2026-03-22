package top.fifthlight.combine.data

import androidx.compose.runtime.Immutable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Immutable
@Serializable(with = IdentifierSerializer::class)
sealed class Identifier {
    data class Namespaced(val namespace: String, val id: String) : Identifier() {
        override fun toString() = "$namespace:$id"
    }

    data class Vanilla(val id: String) : Identifier() {
        override fun toString() = "minecraft:$id"
    }

    companion object {
        fun of(namespace: String, id: String) = Namespaced(namespace, id)
        fun ofVanilla(id: String) = Vanilla(id)
    }
}

fun Identifier(string: String): Identifier {
    val colonIndex = string.indexOf(':')
    return if (colonIndex == -1) {
        Identifier.ofVanilla(string)
    } else {
        val namespace = string.substring(0, colonIndex)
        val id = string.substring(colonIndex + 1)
        Identifier.of(namespace, id)
    }
}

class IdentifierSerializer : KSerializer<Identifier> {
    override val descriptor: SerialDescriptor = SerialDescriptor(
        serialName = "top.fifthlight.combine.data.Identifier",
        original = serialDescriptor<String>(),
    )

    override fun serialize(
        encoder: Encoder,
        value: Identifier,
    ) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Identifier {
        val string = decoder.decodeString()
        val split = string.split(":")
        return when (split.size) {
            1 -> Identifier.ofVanilla(string)

            2 -> if (split[0] == "minecraft") {
                Identifier.ofVanilla(split[1])
            } else {
                Identifier.of(split[0], split[1])
            }

            else -> error("Bad identifier: $split")
        }
    }
}
