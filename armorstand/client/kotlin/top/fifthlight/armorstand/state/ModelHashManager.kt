package top.fifthlight.armorstand.state

import top.fifthlight.armorstand.util.ModelHash
import java.util.*

object ModelHashManager {
    private val modelHashes = mutableMapOf<UUID, ModelHash>()

    suspend fun putModelHash(uuid: UUID, hash: ModelHash?) {
        if (hash == null) {
            modelHashes.remove(uuid)
        } else {
            modelHashes[uuid] = hash
        }
        ClientModelPathManager.update(uuid, hash)
    }

    fun clearHash() = modelHashes.clear()

    fun getHash(uuid: UUID) = modelHashes[uuid]

    fun getModelHashes(): Map<UUID, ModelHash> = modelHashes
}
