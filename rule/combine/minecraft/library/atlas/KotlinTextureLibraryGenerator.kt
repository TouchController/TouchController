package top.fifthlight.combine.resources.atlas

import com.squareup.kotlinpoet.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import top.fifthlight.bazel.worker.api.Worker
import top.fifthlight.combine.resources.Metadata
import top.fifthlight.combine.resources.NinePatch
import top.fifthlight.data.IntOffset
import top.fifthlight.data.IntSize
import java.io.PrintWriter
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.io.path.writeText

@Serializable
private data class AtlasMetadata(
    val width: Int,
    val height: Int,
    val textures: Map<String, PlacedTexture>,
)

@Serializable
private data class PlacedTexture(
    val identifier: String,
    val position: IntOffset,
    val size: IntSize,
    val ninePatch: NinePatch?,
)

fun main(vararg args: String) = object : Worker() {
    override fun handleRequest(
        out: PrintWriter,
        sandboxDir: Path?,
        vararg args: String
    ): Int {
        var outputFile: Path? = null
        var packageName: String? = null
        var className: String? = null
        var prefix: String? = null
        var namespace: String? = null
        var atlasMetadataPath: Path? = null
        var outputPackage: String? = null

        data class TextureEntry(
            val identifier: String,
            val metadataPath: Path,
        )

        val textures = mutableListOf<TextureEntry>()

        var i = 0
        while (i < args.size) {
            when (args[i]) {
                "--output" -> {
                    if (++i >= args.size) {
                        out.println("Missing value for --output"); return 1
                    }
                    outputFile = sandboxDir?.resolve(Path.of(args[i])) ?: Path.of(args[i])
                }

                "--package" -> {
                    if (++i >= args.size) {
                        out.println("Missing value for --package"); return 1
                    }
                    packageName = args[i]
                }

                "--class-name" -> {
                    if (++i >= args.size) {
                        out.println("Missing value for --class-name"); return 1
                    }
                    className = args[i]
                }

                "--prefix" -> {
                    if (++i >= args.size) {
                        out.println("Missing value for --prefix"); return 1
                    }
                    prefix = args[i]
                }

                "--namespace" -> {
                    if (++i >= args.size) {
                        out.println("Missing value for --namespace"); return 1
                    }
                    namespace = args[i]
                }

                "--atlas-metadata" -> {
                    if (++i >= args.size) {
                        out.println("Missing value for --atlas-metadata"); return 1
                    }
                    val raw = args[i]
                    atlasMetadataPath = sandboxDir?.resolve(Path.of(raw)) ?: Path.of(raw)
                }

                "--output-package" -> {
                    if (++i >= args.size) {
                        out.println("Missing value for --output-package"); return 1
                    }
                    outputPackage = args[i]
                }

                "--texture" -> {
                    if (i + 4 > args.size) {
                        out.println("Incomplete --texture entry"); return 1
                    }
                    val identifier = args[i + 1]
                    var metadataPath = Path.of(args[i + 3])
                    if (sandboxDir != null) {
                        metadataPath = sandboxDir.resolve(metadataPath)
                    }
                    textures.add(TextureEntry(identifier, metadataPath))
                    i += 3
                }

                else -> {
                    out.println("Unknown argument: ${args[i]}")
                    return 1
                }
            }
            i++
        }

        requireNotNull(outputFile) { "--output required" }
        requireNotNull(packageName) { "--package required" }
        requireNotNull(className) { "--class-name required" }
        requireNotNull(prefix) { "--prefix required" }
        requireNotNull(namespace) { "--namespace required" }
        requireNotNull(atlasMetadataPath) { "--atlas-metadata required" }

        val resolvedOutputPackage = outputPackage ?: packageName

        val atlasMetadata = Json.decodeFromString<AtlasMetadata>(atlasMetadataPath.readText())

        val classSpecBuilder = TypeSpec.objectBuilder(className + "Impl")
            .addSuperinterface(ClassName(packageName, className))
            .addAnnotation(
                AnnotationSpec.builder(ClassName("top.fifthlight.mergetools.api", "ActualImpl"))
                    .addMember("$packageName.$className::class")
                    .build()
            )
            .addFunction(
                FunSpec.builder("of")
                    .addAnnotation(JvmStatic::class)
                    .addAnnotation(
                        AnnotationSpec.builder(ClassName("top.fifthlight.mergetools.api", "ActualConstructor")).build()
                    )
                    .returns(ClassName(packageName, className))
                    .addCode("return %LImpl", className)
                    .build()
            )

        classSpecBuilder.addProperty(
            PropertySpec.builder("atlasTexture", ClassName("top.fifthlight.combine.core.paint", "Texture"))
                .initializer(
                    "TextureFactory.create(%S, %S, %L, %L, IntPadding.ZERO)",
                    namespace,
                    "textures/gui/$prefix/atlas.png",
                    atlasMetadata.width,
                    atlasMetadata.height,
                )
                .build()
        )
        classSpecBuilder.addProperty(
            PropertySpec.builder("atlas", ClassName("top.fifthlight.combine.core.util.atlas", "AtlasTexture"))
                .initializer("AtlasTexture(atlasTexture)")
                .build()
        )

        for ((_, placed) in atlasMetadata.textures) {
            val propertySpec = if (placed.ninePatch != null) {
                PropertySpec
                    .builder(placed.identifier, ClassName("top.fifthlight.combine.core.paint", "Texture"))
                    .addModifiers(KModifier.OVERRIDE)
                    .initializer(
                        """%T(
                          |    texture = atlas.createPart(
                          |        IntOffset(%L, %L),
                          |        IntSize(%L, %L),
                          |        IntPadding(%L, %L, %L, %L),
                          |    ),
                          |    scaleArea = IntRect(
                          |        offset = IntOffset(%L, %L),
                          |        size = IntSize(%L, %L),
                          |    ),
                          |)
                        """.trimMargin(),
                        ClassName("top.fifthlight.combine.core.util.ninepatch", "NinePatchTexture"),
                        placed.position.x, placed.position.y,
                        placed.size.width, placed.size.height,
                        placed.ninePatch.padding.left, placed.ninePatch.padding.top,
                        placed.ninePatch.padding.right, placed.ninePatch.padding.bottom,
                        placed.ninePatch.scaleArea.offset.x, placed.ninePatch.scaleArea.offset.y,
                        placed.ninePatch.scaleArea.size.width, placed.ninePatch.scaleArea.size.height,
                    )
                    .build()
            } else {
                PropertySpec
                    .builder(placed.identifier, ClassName("top.fifthlight.combine.core.paint", "Texture"))
                    .addModifiers(KModifier.OVERRIDE)
                    .initializer(
                        "atlas.createPart(IntOffset(%L, %L), IntSize(%L, %L))",
                        placed.position.x, placed.position.y,
                        placed.size.width, placed.size.height,
                    )
                    .build()
            }
            classSpecBuilder.addProperty(propertySpec)
        }

        for (texture in textures) {
            val metadata = Json.decodeFromString<Metadata>(texture.metadataPath.readText())
            classSpecBuilder.addProperty(
                PropertySpec.builder(
                    texture.identifier,
                    ClassName("top.fifthlight.combine.core.paint", "BackgroundTexture")
                ).addModifiers(KModifier.OVERRIDE).initializer(
                    "BackgroundTextureFactory.create(%S, %S, %L, %L)",
                    namespace,
                    "textures/gui/$prefix/${texture.identifier}.png",
                    metadata.size.width,
                    metadata.size.height,
                ).build()
            )
        }

        val file = FileSpec
            .builder(resolvedOutputPackage, className)
            .addAnnotation(
                AnnotationSpec
                    .builder(Suppress::class)
                    .addMember("%S", "RedundantVisibilityModifier")
                    .build()
            )
            .addImport("top.fifthlight.combine.core.paint", "BackgroundTextureFactory")
            .addImport("top.fifthlight.combine.core.paint", "TextureFactory")
            .addImport("top.fifthlight.combine.core.util.atlas", "AtlasTexture")
            .addImport("top.fifthlight.combine.core.util.ninepatch", "NinePatchTexture")
            .addImport("top.fifthlight.data", "IntOffset")
            .addImport("top.fifthlight.data", "IntPadding")
            .addImport("top.fifthlight.data", "IntRect")
            .addImport("top.fifthlight.data", "IntSize")
            .addType(classSpecBuilder.build())
            .build()
        outputFile.writeText(buildString { file.writeTo(this) })
        return 0
    }
}.run(*args)
