package top.fifthlight.combine.resources.generator

import kotlinx.serialization.json.Json
import top.fifthlight.bazel.worker.api.Worker
import top.fifthlight.combine.resources.Metadata
import top.fifthlight.combine.resources.NinePatchMetadata
import top.fifthlight.data.IntSize
import java.io.PrintWriter
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.io.path.writeText

object MetadataGeneratorWorker : Worker() {
    @JvmStatic
    fun main(vararg args: String) = run(*args)

    override fun handleRequest(
        out: PrintWriter,
        sandboxDir: Path?,
        args: Array<String>,
    ): Int {
        val mode = args[0]
        val (options, arguments) = args.partition { it.startsWith("--") }
        val inputFile = sandboxDir?.resolve(Path.of(arguments[1])) ?: Path.of(arguments[1])
        val outputFile = sandboxDir?.resolve(Path.of(arguments[2])) ?: Path.of(arguments[2])
        val image = ImageIO.read(inputFile.toFile())
        val imageSize = IntSize(image.width, image.height)
        when (mode) {
            "texture" -> {
                val background = options.contains("--background")
                val metadata = Metadata(
                    size = imageSize,
                    background = background,
                )
                outputFile.writeText(Json.encodeToString(metadata))
            }

            "ninepatch" -> {
                val ninePatch = NinePatch(image)
                val croppedImage = image.getSubimage(1, 1, image.width - 2, image.height - 2)
                val (compressedNinePatch, compressedImage) = compressNinePatch(ninePatch, croppedImage)
                val compressedOutputFile = sandboxDir?.resolve(Path.of(arguments[3])) ?: Path.of(arguments[3])
                val metadata = NinePatchMetadata(
                    size = imageSize - 2,
                    ninePatch = compressedNinePatch,
                )
                outputFile.writeText(Json.encodeToString(metadata))
                ImageIO.write(compressedImage, "PNG", compressedOutputFile.toFile())
            }

            else -> throw IllegalArgumentException("Bad mode: $mode")
        }

        return 0
    }
}
