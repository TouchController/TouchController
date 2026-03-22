package top.fifthlight.touchcontroller.common.config.holder

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet
import org.slf4j.LoggerFactory
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.common.config.GlobalConfig
import top.fifthlight.touchcontroller.common.config.PresetConfig
import top.fifthlight.touchcontroller.common.config.preset.PresetManager
import top.fifthlight.touchcontroller.common.config.preset.builtin.key.BuiltinPresetKey
import top.fifthlight.touchcontroller.common.config.platform.PlatformConfigProvider
import top.fifthlight.touchcontroller.common.config.widget.WidgetPresetManager
import top.fifthlight.touchcontroller.common.ext.combineStates
import top.fifthlight.touchcontroller.common.ext.mapState
import top.fifthlight.touchcontroller.common.gal.config.ConfigDirectoryProviderFactory
import top.fifthlight.touchcontroller.common.gal.gameconfig.GameConfigEditor
import top.fifthlight.touchcontroller.common.gal.gameconfig.GameConfigEditorFactory
import top.fifthlight.touchcontroller.common.serialization.jsonFormat
import kotlin.collections.get
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.moveTo
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.jvm.java
import kotlin.runCatching

@ActualImpl(PlatformConfigProvider::class)
object GlobalConfigHolder : PlatformConfigProvider {
    @ActualConstructor
    @JvmStatic
    fun of(): PlatformConfigProvider = this

    private val logger = LoggerFactory.getLogger(GlobalConfigHolder::class.java)
    private val gameConfigEditor: GameConfigEditor = GameConfigEditorFactory.of()
    private val configDir = ConfigDirectoryProviderFactory.of().configDirectory
    private val configFile = configDir.resolve("config.json")

    private val _config = MutableStateFlow(GlobalConfig.default)
    val config = _config.asStateFlow()
    override val platformConfig
        get() = config.mapState { it.platform }

    private val defaultPreset = BuiltinPresetKey.DEFAULT.preset
    val currentPreset = combineStates(config, PresetManager.presets) { config, presets ->
        when (val preset = config.preset) {
            is PresetConfig.BuiltIn -> preset.key.preset
            is PresetConfig.Custom -> presets[preset.uuid] ?: defaultPreset
        }
    }
    val currentPresetUuid = config.mapState { (it.preset as? PresetConfig.Custom)?.uuid }

    fun load() {
        try {
            createConfigDirectory()
        } catch (ex: kotlin.Exception) {
            logger.warn("Failed to create config folder: ", ex)
            return
        }
        try {
            logger.info("Reading TouchController config file")
            _config.value = jsonFormat.decodeFromString(configFile.readText())
        } catch (ex: kotlin.Exception) {
            logger.warn("Failed to read config: ", ex)
            val timeStamp = System.currentTimeMillis()
            val backupFileName = configFile.resolveSibling("${configFile.fileName}-backup-$timeStamp")
            runCatching {
                configFile.moveTo(backupFileName, overwrite = true)
            }
        }
        PresetManager.load()
        WidgetPresetManager.load()
    }

    private fun createConfigDirectory() {
        if (!configDir.exists()) {
            // Change Minecraft options
            logger.info("First startup of TouchController, turn on auto jumping")
            gameConfigEditor.submit { editor ->
                editor.autoJump = true
            }
        }
        try {
            configDir.createDirectories()
        } catch (_: java.io.IOException) {
        }
    }

    fun updateConfig(editor: GlobalConfig.() -> GlobalConfig) {
        val config = _config.updateAndGet(editor)
        createConfigDirectory()
        logger.info("Saving TouchController config file")
        configFile.writeText(jsonFormat.encodeToString(config))
    }
}
