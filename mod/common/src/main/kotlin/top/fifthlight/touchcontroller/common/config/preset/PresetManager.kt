package top.fifthlight.touchcontroller.common.config.preset

import kotlinx.collections.immutable.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.slf4j.LoggerFactory
import top.fifthlight.touchcontroller.common.config.ConfigDirectoryProvider
import java.io.IOException
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.*
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

fun PresetsContainer(
    presets: ImmutableMap<Uuid, LayoutPreset>,
    order: ImmutableList<Uuid>,
): PresetsContainer {
    val orderedEntries = mutableListOf<Pair<Uuid, LayoutPreset>>()
    val entries = presets.toMutableMap()
    for (uuid in order) {
        val preset = entries.remove(uuid) ?: continue
        orderedEntries += Pair(uuid, preset)
    }
    for ((uuid, preset) in entries.entries.sortedBy { (id, _) -> id.toJavaUuid() }) {
        orderedEntries += uuid to preset
    }
    return PresetsContainer(orderedEntries.toPersistentList())
}

data class PresetsContainer(
    val orderedEntries: PersistentList<Pair<Uuid, LayoutPreset>> = persistentListOf()
): PersistentMap<Uuid, LayoutPreset> by orderedEntries.toMap().toPersistentMap() {
    val order: ImmutableList<Uuid>
        get() = orderedEntries.map { it.first }.toPersistentList()
}

class PresetManager : KoinComponent {
    private val logger = LoggerFactory.getLogger(PresetManager::class.java)
    private val configDirectoryProvider: ConfigDirectoryProvider = get()
    val presetDir: Path = configDirectoryProvider.getConfigDirectory().resolve("preset")
    private val orderFile = presetDir.resolve("order.json")
    private val json: Json by inject()
    private val _presets = MutableStateFlow(PresetsContainer())
    val presets = _presets.asStateFlow()

    @OptIn(ExperimentalSerializationApi::class)
    fun load() {
        try {
            logger.info("Reading TouchController preset file")

            // Create preset directory if it doesn't exist
            if (!presetDir.exists()) {
                presetDir.createDirectories()
                _presets.value = PresetsContainer()
                return
            }

            val order = runCatching<PresetManager, List<Uuid>> {
                orderFile.inputStream().use(json::decodeFromStream)
            }.getOrNull()?.toPersistentList() ?: persistentListOf()
            val presets = buildMap {
                for (entry in presetDir.listDirectoryEntries("*.json")) {
                    try {
                        val uuidStr = entry.fileName.toString().lowercase().removeSuffix(".json")
                        val uuid = Uuid.parse(uuidStr)
                        val preset: LayoutPreset = entry.inputStream().use(json::decodeFromStream)
                        put(uuid, preset)
                    } catch (_: Exception) {
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

    @OptIn(ExperimentalSerializationApi::class)
    private fun saveOrder(order: ImmutableList<Uuid>) {
        logger.info("Saving TouchController preset order file")
        orderFile.outputStream().use { json.encodeToStream<List<Uuid>>(order, it) }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun savePreset(uuid: Uuid, preset: LayoutPreset, create: Boolean = true) {
        logger.info("Saving TouchController preset ${preset.name}($uuid)")
        presetDir.createDirectories()
        try {
            getPresetFile(uuid).outputStream(
                *if (create) {
                    arrayOf(StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE, StandardOpenOption.CREATE)
                } else {
                    arrayOf(StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)
                }
            ).use { json.encodeToStream(preset, it) }
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