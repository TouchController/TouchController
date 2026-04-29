package top.fifthlight.combine.resources.altas

import kotlinx.serialization.json.Json
import org.lwjgl.stb.STBRPContext
import org.lwjgl.stb.STBRPNode
import org.lwjgl.stb.STBRPRect
import org.lwjgl.stb.STBRectPack
import org.lwjgl.system.MemoryStack
import top.fifthlight.bazel.worker.api.Worker
import top.fifthlight.combine.resources.Metadata
import top.fifthlight.combine.resources.NinePatch
import top.fifthlight.combine.resources.NinePatchMetadata
import top.fifthlight.data.IntOffset
import top.fifthlight.data.IntSize
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_ARGB
import java.io.PrintWriter
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.jar.JarEntry
import java.util.zip.ZipOutputStream
import javax.imageio.ImageIO
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream
import kotlin.io.path.readText
import kotlin.io.path.writeText

private const val DOS_EPOCH = 315532800000L

private data class Texture(
    val identifier: String,
    val ninePatch: NinePatch?,
    val image: BufferedImage,
) {
    val size: IntSize
        get() = IntSize(
            image.width,
            image.height,
        )

    fun place(position: IntOffset) = PlacedTexture(
        identifier = identifier,
        position = position,
        size = size,
        ninePatch = ninePatch,
    )
}

fun main(vararg args: String) = object : Worker() {
    override fun handleRequest(
        out: PrintWriter,
        sandboxDir: Path?,
        vararg args: String
    ): Int {
        if (args.size < 4) {
            out.println("Usage: TextureAtlasGenerator <namespace> <prefix> <output_jar> <output_metadata> --width <width> --height <height> [--texture <identifier> <png file> <manifest json>] [--ninepatch <identifier> <png file> <manifest json>]...")
            return 1
        }

        val namespace = args[0]
        val prefix = args[1]
        val outputJar = sandboxDir?.resolve(Path.of(args[2])) ?: Path.of(args[2])
        val outputMetadata = sandboxDir?.resolve(Path.of(args[3])) ?: Path.of(args[3])

        var atlasWidth = 512
        var atlasHeight = 512
        val placedTextures = hashMapOf<String, PlacedTexture>()

        ZipOutputStream(outputJar.outputStream()).use { outStream ->
            fun entry(name: String) = JarEntry(name).apply {
                creationTime = FileTime.fromMillis(DOS_EPOCH)
                lastAccessTime = FileTime.fromMillis(DOS_EPOCH)
                lastModifiedTime = FileTime.fromMillis(DOS_EPOCH)
                timeLocal = LocalDateTime.ofEpochSecond(DOS_EPOCH / 1000, 0, ZoneOffset.UTC)
            }

            val textures = mutableListOf<Texture>()

            var i = 4
            while (i < args.size) {
                if (args.size - i < 3) {
                    out.println("Bad texture entry")
                    return 1
                }

                val identifier = args[i + 1]
                val pngFile = sandboxDir?.resolve(Path.of(args[i + 2])) ?: Path.of(args[i + 2])
                val manifestFile = sandboxDir?.resolve(Path.of(args[i + 3])) ?: Path.of(args[i + 3])

                when (val type = args[i]) {
                    "--width" -> {
                        atlasWidth = args[i + 1].toInt()
                        i += 2
                    }

                    "--height" -> {
                        atlasHeight = args[i + 1].toInt()
                        i += 2
                    }

                    "--texture" -> {
                        val manifest = Json.decodeFromString<Metadata>(manifestFile.readText())
                        if (!manifest.background) {
                            val image = ImageIO.read(pngFile.toFile())
                            textures += Texture(
                                identifier = identifier,
                                ninePatch = null,
                                image = image,
                            )
                        } else {
                            outStream.putNextEntry(entry("assets/$namespace/textures/gui/background/$prefix/$identifier.png"))
                            pngFile.inputStream().use { it.transferTo(outStream) }
                            outStream.closeEntry()
                        }
                        i += 4
                    }

                    "--ninepatch" -> {
                        val manifest = Json.decodeFromString<NinePatchMetadata>(manifestFile.readText())
                        val image = ImageIO.read(pngFile.toFile())
                        textures += Texture(
                            identifier = identifier,
                            ninePatch = manifest.ninePatch,
                            image = image,
                        )
                        i += 4
                    }

                    else -> {
                        out.println("Bad entry: $type")
                        return 1
                    }
                }
            }

            textures.sortByDescending { texture ->
                texture.size.width * texture.size.height
            }

            val outputImage = BufferedImage(atlasWidth, atlasHeight, TYPE_INT_ARGB)

            MemoryStack.stackPush().use { stack ->
                val context = STBRPContext.malloc(stack)
                val nodes = STBRPNode.malloc(atlasWidth, stack)
                STBRectPack.stbrp_init_target(context, atlasWidth, atlasHeight, nodes)

                val rectangles = STBRPRect.malloc(textures.size, stack)
                for ((index, texture) in textures.withIndex()) {
                    rectangles[index].set(index, texture.size.width, texture.size.height, 0, 0, false)
                }

                STBRectPack.stbrp_pack_rects(context, rectangles)

                val outputGraphics = outputImage.createGraphics()
                for ((index, rect) in rectangles.withIndex()) {
                    val texture = textures[index]

                    if (!rect.was_packed()) {
                        outputGraphics.dispose()
                        out.println(
                            """Failed to pack texture '${texture.identifier}'
                                |(size: ${texture.size.width}x${texture.size.height}) into atlas
                                |(size: ${atlasWidth}x${atlasHeight}).
                                |The texture does not fit or the atlas is too small.""".trimMargin(),
                        )
                        return 1
                    }

                    val position = IntOffset(
                        x = rect.x(),
                        y = rect.y(),
                    )

                    if (position.x < 0 || position.y < 0 ||
                        position.x + texture.size.width > atlasWidth ||
                        position.y + texture.size.height > atlasHeight
                    ) {
                        outputGraphics.dispose()
                        out.println(
                            """Texture '${texture.identifier}' placed at invalid position: $position
                                |with size: ${texture.size} in atlas: ${atlasWidth}x${atlasHeight}""".trimMargin(),
                        )
                        return 1
                    }

                    placedTextures[texture.identifier] = texture.place(position)
                    outputGraphics.drawImage(texture.image, position.x, position.y, null)
                }

                outputGraphics.dispose()
            }

            outStream.putNextEntry(entry("assets/$namespace/textures/gui/$prefix/atlas.png"))
            ImageIO.write(outputImage, "png", outStream)
            outStream.closeEntry()
        }

        outputMetadata.writeText(
            Json.encodeToString(
                AtlasMetadata(
                    width = atlasWidth,
                    height = atlasHeight,
                    textures = placedTextures,
                ),
            ),
        )
        return 0
    }
}.run(*args)
