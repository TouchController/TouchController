package top.fifthlight.blazerod.model.formats

import top.fifthlight.blazerod.model.loader.*
import top.fifthlight.blazerod.model.loader.util.readRemaining
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.*
import kotlin.io.path.extension

object ModelFileLoaders {
    private var initialized = false

    @JvmStatic
    fun initialize() = synchronized(this) {
        if (initialized) {
            return@synchronized
        }
        initialized = true
        val allLoaders = ServiceLoader.load(ModelFileLoader::class.java)
        loaders = allLoaders.mapNotNull {
            it.initialize()
            it.takeIf { it.available }
        }
    }

    lateinit var loaders: List<ModelFileLoader>
        private set

    private val embedThumbnailLoaders by lazy {
        loaders.filter { ModelFileLoader.Ability.EMBED_THUMBNAIL in it.abilities }
    }

    private val markerFileLoaders by lazy {
        buildMap {
            loaders.forEach { loader ->
                loader.markerFiles.forEach { (markerFile, ability) ->
                    put(markerFile.lowercase(), loader)
                }
            }
        }
    }

    private val probeBytes by lazy {
        loaders
            .asSequence()
            .mapNotNull { it.probeLength }
            .maxOrNull()
    }

    private fun probeByContent(loaders: List<ModelFileLoader>, path: Path) = probeBytes?.let { probeBytes ->
        FileChannel.open(path, StandardOpenOption.READ).use { channel ->
            val buffer = ByteBuffer.allocate(probeBytes)
            channel.readRemaining(buffer)
            buffer.flip()

            for (loader in loaders) {
                val probeLength = loader.probeLength ?: continue
                buffer.position(0)
                if (buffer.remaining() >= probeLength) {
                    if (loader.probe(buffer)) {
                        return@use loader
                    }
                }
            }
            null
        }
    }

    private fun probeLoader(loaders: List<ModelFileLoader>, path: Path): ModelFileLoader? {
        // First try marker file
        val fileName = path.fileName.toString().lowercase()
        markerFileLoaders[fileName]?.let { return it }
        val (probableLoaders, unprobableLoaders) = loaders.partition { it.probeLength != null }

        // Second try probe by content
        probeByContent(probableLoaders, path)?.let { return it }

        // Third try probe by extension
        val extension = path.extension.lowercase()
        for (loader in unprobableLoaders) {
            if (extension in loader.extensions) {
                return loader
            }
        }
        return null
    }

    @JvmOverloads
    fun probeAndLoad(
        path: Path,
        context: LoadContext = LoadContext.File(path.parent ?: error("no base path: $path")),
        param: LoadParam = LoadParam(),
    ): LoadResult? {
        val loader = probeLoader(loaders, path)
        return loader?.load(path, context, param)
    }

    @JvmOverloads
    fun probeAndLoad(
        path: Path,
        param: LoadParam,
        context: LoadContext = LoadContext.File(path.parent ?: error("no base path: $path")),
    ): LoadResult? {
        val loader = probeLoader(loaders, path)
        return loader?.load(path, context, param)
    }

    @JvmOverloads
    fun getEmbedThumbnail(
        path: Path,
        context: LoadContext = LoadContext.Empty,
    ): ThumbnailResult? {
        val loader = probeLoader(embedThumbnailLoaders, path)
        return loader?.getThumbnail(path, context)
    }
}
