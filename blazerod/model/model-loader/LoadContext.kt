package top.fifthlight.blazerod.model.loader

import top.fifthlight.blazerod.model.loader.util.openChannelCaseInsensitive
import top.fifthlight.blazerod.model.loader.util.readAll
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Path

interface LoadContext {
    enum class ResourceType {
        TEXTURE,
        PLAIN_DATA,
    }

    fun loadExternalResource(
        path: String,
        type: ResourceType,
        caseInsensitive: Boolean,
        maxSize: Int,
    ): ByteBuffer

    object Empty: LoadContext {
        override fun loadExternalResource(
            path: String,
            type: ResourceType,
            caseInsensitive: Boolean,
            maxSize: Int,
        ) = throw IOException("Empty LoadContext")
    }

    class File(private val basePath: Path): LoadContext {
        override fun loadExternalResource(
            path: String,
            type: ResourceType,
            caseInsensitive: Boolean,
            maxSize: Int,
        ): ByteBuffer {
            val pathParts = path.split("/", "\\")
            val relativePath = if (pathParts.size == 1) {
                basePath.fileSystem.getPath(pathParts[0])
            } else {
                basePath.fileSystem.getPath(pathParts[0], *pathParts.subList(1, pathParts.size).toTypedArray())
            }
            val path = basePath.resolve(relativePath)
            val channel = if (caseInsensitive) {
                path.openChannelCaseInsensitive()
            } else {
                FileChannel.open(path)
            }
            return channel.use { channel ->
                val size = channel.size()
                if (size > maxSize) {
                    throw IOException("Resource $path is too large. Maximum size is $maxSize bytes, but got $size bytes.")
                }
                runCatching {
                    channel.map(FileChannel.MapMode.READ_ONLY, 0, size)
                }.getOrNull() ?: run {
                    val buffer = ByteBuffer.allocateDirect(size.toInt())
                    channel.readAll(buffer)
                    buffer.flip()
                    buffer
                }
            }
        }
    }
}
