package top.fifthlight.combine.resources.vanilla

import com.squareup.kotlinpoet.*
import kotlinx.serialization.json.Json
import top.fifthlight.bazel.worker.api.Worker
import top.fifthlight.combine.resources.Metadata
import top.fifthlight.combine.resources.NinePatchMetadata
import java.io.PrintWriter
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.io.path.writeText

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
        var outputPackage: String? = null

        data class TextureEntry(
            val identifier: String,
            val metadataPath: Path,
        )

        val textures = mutableListOf<TextureEntry>()
        val ninePatchTextures = mutableListOf<TextureEntry>()

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

                "--ninepatch" -> {
                    if (i + 4 > args.size) {
                        out.println("Incomplete --ninepatch entry"); return 1
                    }
                    val identifier = args[i + 1]
                    var metadataPath = Path.of(args[i + 3])
                    if (sandboxDir != null) {
                        metadataPath = sandboxDir.resolve(metadataPath)
                    }
                    ninePatchTextures.add(TextureEntry(identifier, metadataPath))
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

        val resolvedOutputPackage = outputPackage ?: packageName

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

        for (texture in textures) {
            val metadata = Json.decodeFromString<Metadata>(texture.metadataPath.readText())
            if (metadata.background) {
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
            } else {
                classSpecBuilder.addProperty(
                    PropertySpec.builder(
                        texture.identifier,
                        ClassName("top.fifthlight.combine.core.paint", "Texture")
                    ).addModifiers(KModifier.OVERRIDE).initializer(
                        "TextureFactory.createSprite(%S, %S, %L, %L, IntPadding.ZERO)",
                        namespace,
                        "$prefix/${texture.identifier}",
                        metadata.size.width,
                        metadata.size.height,
                    ).build()
                )
            }
        }

        for (ninePatch in ninePatchTextures) {
            val metadata = Json.decodeFromString<NinePatchMetadata>(ninePatch.metadataPath.readText())
            classSpecBuilder.addProperty(
                PropertySpec
                    .builder(ninePatch.identifier, ClassName("top.fifthlight.combine.core.paint", "Texture"))
                    .addModifiers(KModifier.OVERRIDE)
                    .initializer(
                        "TextureFactory.createSprite(%S, %S, %L, %L, IntPadding(%L, %L, %L, %L))",
                        namespace,
                        "$prefix/${ninePatch.identifier}",
                        metadata.size.width,
                        metadata.size.height,
                        metadata.ninePatch.padding.left,
                        metadata.ninePatch.padding.top,
                        metadata.ninePatch.padding.right,
                        metadata.ninePatch.padding.bottom,
                    )
                    .build()
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
            .addImport("top.fifthlight.combine.core.paint", "TextureFactory")
            .addImport("top.fifthlight.combine.core.paint", "BackgroundTextureFactory")
            .addImport("top.fifthlight.data", "IntPadding")
            .addType(classSpecBuilder.build())
            .build()
        outputFile.writeText(buildString { file.writeTo(this) })
        return 0
    }
}.run(*args)
