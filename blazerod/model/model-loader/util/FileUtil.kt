package top.fifthlight.blazerod.model.loader.util

import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.OpenOption
import java.nio.file.Path

fun Path.openChannelCaseInsensitive(vararg options: OpenOption): FileChannel {
    if (this.toString().isBlank()) {
        throw IllegalArgumentException("Path cannot be empty.")
    }

    val normalizedPath = this.normalize()

    val startPath = if (normalizedPath.isAbsolute) {
        Path.of(this.root?.toString() ?: "/")
    } else {
        Path.of(".")
    }

    return try {
        FileChannel.open(this, *options)
    } catch (ex: Exception) {
        val resolvedPath = resolveCaseInsensitive(startPath, normalizedPath) ?: throw ex
        FileChannel.open(resolvedPath, *options)
    }
}

private fun resolveCaseInsensitive(currentBase: Path, remainingPath: Path): Path? {
    if (remainingPath.toString().isEmpty()) {
        return currentBase
    }

    val firstComponent = remainingPath.getName(0).toString()
    val nextRemainingPath = if (remainingPath.nameCount > 1) {
        remainingPath.subpath(1, remainingPath.nameCount)
    } else {
        Path.of("")
    }

    if (!Files.exists(currentBase) || !Files.isDirectory(currentBase)) {
        return null
    }

    Files.list(currentBase).use { stream ->
        val foundComponentPath = stream.filter { p ->
            p.fileName.toString().equals(firstComponent, ignoreCase = true)
        }.findFirst()

        return if (foundComponentPath.isPresent) {
            resolveCaseInsensitive(foundComponentPath.get(), nextRemainingPath)
        } else {
            null
        }
    }
}
