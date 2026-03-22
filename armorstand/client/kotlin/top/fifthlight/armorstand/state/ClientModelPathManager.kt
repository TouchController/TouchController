package top.fifthlight.armorstand.state

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.minecraft.client.Minecraft
import top.fifthlight.armorstand.ArmorStand
import top.fifthlight.armorstand.ArmorStandClient
import top.fifthlight.armorstand.config.ConfigHolder
import top.fifthlight.armorstand.manage.ModelManagerHolder
import top.fifthlight.armorstand.util.ModelHash
import java.nio.file.Path
import java.util.*

object ClientModelPathManager {
    private val minecraft = Minecraft.getInstance()
    var selfPath: Path? = null
        private set
    private val selfUuid: UUID?
        get() = minecraft.player?.uuid
    private val modelPaths = mutableMapOf<UUID, Path>()

    fun initialize() {
        ArmorStand.instance.scope.launch {
            ConfigHolder.config
                .map { it.modelPath }
                .distinctUntilChanged()
                .collect { selfPath = it }
        }
        ArmorStand.instance.scope.launch {
            ModelManagerHolder.instance.lastUpdateTime.filterNotNull().collectLatest {
                modelPaths.clear()
                ModelHashManager.getModelHashes().forEach { (uuid, hash) ->
                    update(uuid, hash)
                }
            }
        }
    }

    suspend fun update(uuid: UUID, hash: ModelHash?) {
        if (hash == null) {
            modelPaths.remove(uuid)
            return
        }
        val path = ModelManagerHolder.instance.getModelByHash(hash)?.path
        if (path != null) {
            modelPaths[uuid] = path
        } else {
            modelPaths.remove(uuid)
        }
    }

    fun getPath(uuid: UUID) = if (uuid == selfUuid) {
        selfPath
    } else if (ArmorStandClient.instance.debug) {
        modelPaths[uuid] ?: selfPath
    } else {
        modelPaths[uuid]
    }
}
