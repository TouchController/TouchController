package top.fifthlight.blazerod.model.bedrock

import kotlinx.serialization.json.Json
import top.fifthlight.blazerod.model.bedrock.metadata.ModelMetadata
import top.fifthlight.blazerod.model.loader.*
import top.fifthlight.blazerod.model.loader.util.readToBuffer
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.nio.file.StandardOpenOption

class BedrockModelLoadException(message: String) : Exception(message)

class BedrockModelLoader : ModelFileLoader {
    override val abilities = setOf(ModelFileLoader.Ability.MODEL, ModelFileLoader.Ability.METADATA)
    override val markerFiles = mapOf(
        "ysm.json" to abilities,
        "model.json" to abilities,
    )
    override val probeLength: Int? = null

    override fun probe(buffer: ByteBuffer) = false

    private val format = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private fun readMetadataFile(path: Path): ModelMetadata {
        val metadataString = FileChannel.open(path, StandardOpenOption.READ).use { channel ->
            // Will any file be larger than 1MB?
            val buffer = channel.readToBuffer(readSizeLimit = 1024 * 1024)
            StandardCharsets.UTF_8.decode(buffer).toString()
        }
        return format.decodeFromString<ModelMetadata>(metadataString).also {
            if (it.spec < 2) {
                throw BedrockModelLoadException("Unsupported model spec: ${it.spec}")
            }
        }
    }

    companion object {
        private val EMPTY_LOAD_RESULT = LoadResult(
            metadata = null,
            model = null,
            animations = listOf(),
        )
    }

    override fun load(path: Path, context: LoadContext, param: LoadParam): LoadResult {
        val metadata = readMetadataFile(path)
        val (mainModel, mainAnimations) = BedrockModelJsonLoader(
            properties = metadata.properties,
            context = context,
            file = metadata.files.player,
        ).load("main") ?: return EMPTY_LOAD_RESULT

        return LoadResult(
            metadata = metadata.metadata?.toMetadata(),
            model = mainModel,
            animations = mainAnimations,
        )
    }

    override fun getMarkerFileHashes(marker: Path, directory: Path): Set<Path> {
        val metadata = readMetadataFile(marker)
        val playerModelFiles = metadata.files.player.model.values
        val playerTextureFiles = metadata.files.player.texture.mapNotNull {
            when (it) {
                is ModelMetadata.Files.Texture.Path -> it.path
                is ModelMetadata.Files.Texture.Pbr -> it.normal
            }
        }
        val playerAnimationFiles = metadata.files.player.animation?.values ?: listOf()
        return (playerModelFiles + playerTextureFiles + playerAnimationFiles).map { directory.resolve(it) }.toSet()
    }

    override fun getMetadata(path: Path, context: LoadContext): MetadataResult {
        val metadata = readMetadataFile(path)
        return metadata.metadata?.toMetadata()?.let {
            MetadataResult.Success(it)
        } ?: MetadataResult.None
    }
}
