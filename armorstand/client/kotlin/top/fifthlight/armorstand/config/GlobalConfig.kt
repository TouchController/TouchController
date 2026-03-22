package top.fifthlight.armorstand.config

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.slf4j.LoggerFactory
import top.fifthlight.armorstand.util.GameDirectoryGetter
import top.fifthlight.blazerod.render.version_1_21_8.api.render.Renderer
import top.fifthlight.blazerod.render.version_1_21_8.api.render.RendererTypeHolder
import java.nio.file.InvalidPathException
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

@Serializable
data class GlobalConfig(
    val model: String? = null,
    val showOtherPlayerModel: Boolean = true,
    val sendModelData: Boolean = true,
    val hidePlayerShadow: Boolean = false,
    val hidePlayerArmor: Boolean = false,
    val modelScale: Float = 1f,
    val heldItemScale: Float = 0.9f,
    val heldItemOffsetX: Float = 0f,
    val heldItemOffsetY: Float = 0f,
    val heldItemOffsetZ: Float = 0f,
    val heldItemRotX: Float = -90f,
    val heldItemRotY: Float = 180f,
    val heldItemRotZ: Float = 0f,
    val thirdPersonDistanceScale: Float = 1f,
    val renderer: RendererKey = RendererKey.VERTEX_SHADER_TRANSFORM,
    val vmcUdpPort: Int = 9000,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(GlobalConfig::class.java)
    }

    // TODO: get from list, not hard coded
    @Serializable
    enum class RendererKey(
        val nameKey: String,
        val type: Renderer.Type<*, *>,
    ) {
        @SerialName("vertex")
        VERTEX_SHADER_TRANSFORM(
            nameKey = "armorstand.renderer.vertex_shader_transform",
            type = RendererTypeHolder.vertexShaderTransform,
        ),

        @SerialName("cpu")
        CPU_TRANSFORM(
            nameKey = "armorstand.renderer.cpu_transform",
            type = RendererTypeHolder.cpuTransform,
        ),

        @SerialName("compute")
        COMPUTE_SHADER_TRANSFORM(
            nameKey = "armorstand.renderer.compute_shader_transform",
            type = RendererTypeHolder.computeShaderTransform,
        ),
    }

    val modelPath by lazy {
        try {
            model?.let { Path(it) }
        } catch (ex: InvalidPathException) {
            logger.warn("Bad model path", ex)
            null
        }
    }
}

object ConfigHolder {
    private val configFile = GameDirectoryGetter.configDirectory.resolve("armorstand.json")
    private val _config = MutableStateFlow(GlobalConfig())
    val config = _config.asStateFlow()

    @OptIn(ExperimentalSerializationApi::class)
    fun read() {
        runCatching {
            _config.value = configFile.inputStream().use { Json.decodeFromStream<GlobalConfig>(it) }
        }
    }

    fun update(editor: GlobalConfig.() -> GlobalConfig) {
        save(_config.updateAndGet(editor))
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun save(config: GlobalConfig) {
        configFile.parent.createDirectories()
        configFile.outputStream().use { Json.encodeToStream<GlobalConfig>(config, it) }
    }
}
