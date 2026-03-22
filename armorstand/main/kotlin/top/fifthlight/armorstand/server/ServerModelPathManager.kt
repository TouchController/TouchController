package top.fifthlight.armorstand.server

import top.fifthlight.armorstand.util.ModelHash
import java.util.*

object ServerModelPathManager {
    private val _models = mutableMapOf<UUID, ModelHash>()
    var onUpdateListener: ((UUID, ModelHash?) -> Unit)? = null

    fun update(uuid: UUID, hash: ModelHash?) {
        if (hash == null) {
            _models.remove(uuid)
        } else {
            _models[uuid] = hash
        }
        onUpdateListener?.let { it(uuid, hash) }
    }

    fun getModels(): Map<UUID, ModelHash> = _models

    fun clear() {
        _models.clear()
    }
}
