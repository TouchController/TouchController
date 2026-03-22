package top.fifthlight.armorstand.network

import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import top.fifthlight.armorstand.util.ModelHash
import java.util.*
import kotlin.jvm.optionals.getOrNull

data class ModelUpdateC2SPayload(
    val modelHash: ModelHash?,
) : CustomPacketPayload {
    companion object {
        private val PAYLOAD_ID = ResourceLocation.fromNamespaceAndPath("armorstand", "model_update")
        val ID = CustomPacketPayload.Type<ModelUpdateC2SPayload>(PAYLOAD_ID)
        val STREAM_CODEC: StreamCodec<ByteBuf, ModelUpdateC2SPayload> = ByteBufCodecs.optional(ModelHash.STREAM_CODEC).map(
            { ModelUpdateC2SPayload(it.getOrNull()) },
            { Optional.ofNullable(it.modelHash) },
        )
    }

    override fun type() = ID
}
