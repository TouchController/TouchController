package top.fifthlight.armorstand.network

import net.minecraft.core.UUIDUtil
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import top.fifthlight.armorstand.util.ModelHash
import java.util.*
import kotlin.jvm.optionals.getOrNull

data class PlayerModelUpdateS2CPayload(
    val uuid: UUID,
    val modelHash: ModelHash?,
) : CustomPacketPayload {
    companion object {
        private val PAYLOAD_ID = ResourceLocation.fromNamespaceAndPath("armorstand", "player_model_update")
        val ID = CustomPacketPayload.Type<PlayerModelUpdateS2CPayload>(PAYLOAD_ID)
        val STREAM_CODEC: StreamCodec<FriendlyByteBuf, PlayerModelUpdateS2CPayload> = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            PlayerModelUpdateS2CPayload::uuid,
            ByteBufCodecs.optional(ModelHash.STREAM_CODEC),
            { Optional.ofNullable(it.modelHash) },
            { uuid, modelId -> PlayerModelUpdateS2CPayload(uuid, modelId.getOrNull()) },
        )
    }

    override fun type() = ID

}
