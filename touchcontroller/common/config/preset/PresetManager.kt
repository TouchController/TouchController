package top.fifthlight.touchcontroller.common.config.preset

import kotlinx.collections.immutable.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet
import org.slf4j.LoggerFactory
import top.fifthlight.touchcontroller.common.gal.config.ConfigDirectoryProvider
import top.fifthlight.touchcontroller.common.gal.config.ConfigDirectoryProviderFactory
import top.fifthlight.touchcontroller.common.serialization.jsonFormat
import java.io.IOException
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.*
import kotlin.uuid.Uuid

object PresetManager {
    private val logger = LoggerFactory.getLogger(PresetManager::class.java)
    private val configDirectoryProvider: ConfigDirectoryProvider = ConfigDirectoryProviderFactory.of()
    val presetDir: Path = configDirectoryProvider.configDirectory.resolve("preset")
    private val orderFile = presetDir.resolve("order.json")
    private val _presets = MutableStateFlow(PresetsContainer())
    val presets = _presets.asStateFlow()

    fun load() {
        try {
            logger.info("Reading TouchController preset file")
            val order = runCatching<PresetManager, List<Uuid>> {
                jsonFormat.decodeFromString(orderFile.readText())
            }.getOrNull()?.toPersistentList() ?: persistentListOf()
            val presets = buildMap {
                for (entry in presetDir.listDirectoryEntries("*.json")) {
                    val uuidStr = entry.fileName.toString().lowercase().removeSuffix(".json")
                    val uuid = runCatching {
                        Uuid.parse(uuidStr)
                    }.getOrNull() ?: continue
                    try {
                        put(uuid, jsonFormat.decodeFromString<LayoutPreset>(entry.readText()))
                    } catch (ex: Exception) {
                        logger.warn("Failed to load preset $uuidStr", ex)
                        continue
                    }
                }
            }.toPersistentMap()
            _presets.value = PresetsContainer(
                presets = presets,
                order = order,
            )
        } catch (ex: Exception) {
            logger.warn("Failed to read presets", ex)
        }
    }

    private fun getPresetFile(uuid: Uuid) = presetDir.resolve("$uuid.json")

    private fun saveOrder(order: ImmutableList<Uuid>) {
        logger.info("Saving TouchController preset order file")
        orderFile.writeText(jsonFormat.encodeToString<List<Uuid>>(order))
    }

    fun savePreset(uuid: Uuid, preset: LayoutPreset, create: Boolean = true) {
        logger.info("Saving TouchController preset ${preset.name}($uuid)")
        presetDir.createDirectories()
        try {
            getPresetFile(uuid).writeText(
                text = jsonFormat.encodeToString(preset),
                options = if (create) {
                    arrayOf(StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE, StandardOpenOption.CREATE)
                } else {
                    arrayOf(StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)
                },
            )
            var addedPresets = false
            val newPresets = _presets.updateAndGet {
                if (it.containsKey(uuid)) {
                    addedPresets = false
                    val index = it.orderedEntries.indexOfFirst { (id, _) -> id == uuid }
                    PresetsContainer(it.orderedEntries.set(index, Pair(uuid, preset)))
                } else {
                    addedPresets = true
                    PresetsContainer(it.orderedEntries + (uuid to preset))
                }
            }
            if (addedPresets) {
                saveOrder(newPresets.order)
            }
        } catch (ex: IOException) {
            if (create) {
                logger.warn("Failed to save preset: ", ex)
            }
        }
    }

    fun movePreset(uuid: Uuid, offset: Int) {
        val newPresets = _presets.updateAndGet {
            val index = it.orderedEntries
                .indexOfFirst { (id, _) -> id == uuid }
                .takeIf { it != -1 } ?: return@updateAndGet it
            val newIndex = (index + offset).coerceIn(it.orderedEntries.indices)
            val preset = it.orderedEntries[index]
            val newEntries = it.orderedEntries.removeAt(index).add(newIndex, preset)
            PresetsContainer(newEntries)
        }
        saveOrder(newPresets.order)
    }

    fun removePreset(uuid: Uuid) {
        getPresetFile(uuid).deleteIfExists()
        val newPresets = _presets.updateAndGet {
            PresetsContainer(it.orderedEntries.removeAll { it.first == uuid })
        }
        saveOrder(newPresets.order)
    }
}
