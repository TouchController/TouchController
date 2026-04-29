package top.fifthlight.touchcontroller.resources.generator

import com.squareup.kotlinpoet.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import top.fifthlight.bazel.worker.api.Worker
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import java.io.PrintWriter
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.io.path.writeText

private data class TextureInput(
    val name: String,
    val identifier: String,
)

private data class TextureSetInput(
    val id: String,
    val metadataPath: Path,
    val textures: List<TextureInput>,
)

@Serializable
private data class TextureSetMetadata(
    @SerialName("gray_when_active")
    val grayWhenActive: Boolean = false,
    @SerialName("classic")
    val classic: Boolean = false,
    @SerialName("default")
    val default: Boolean = false,
    @SerialName("base")
    val base: String? = null,
    @SerialName("fallback")
    val fallback: Map<String, String> = mapOf(),
)

private data class TextureSetItem(
    val metadata: TextureSetMetadata,
    val textures: Map<String, String>,
)

private fun String.snakeToCamelCase(firstCharUppercase: Boolean = false) = this.split('_')
    .joinToString("") { it.replaceFirstChar { char -> char.uppercaseChar() } }
    .replaceFirstChar {
        if (firstCharUppercase) {
            it.uppercaseChar()
        } else {
            it.lowercaseChar()
        }
    }

private fun parseTextureSets(textureSets: List<TextureSetInput>) =
    textureSets.associate { set ->
        set.id to TextureSetItem(
            metadata = Json.decodeFromString<TextureSetMetadata>(set.metadataPath.readText()),
            textures = set.textures.associate { it.name to it.identifier }
        )
    }

private fun resolveTextureIdentifier(
    textureName: String,
    setId: String,
    metadataMap: Map<String, TextureSetItem>,
    visited: Set<Pair<String, String>> = emptySet(),
): String {
    val key = textureName to setId
    check(key !in visited) {
        "Circular texture reference: $textureName in texture set $setId (visited: $visited)"
    }
    val newVisited = visited + key
    val item = metadataMap[setId]
        ?: error("Texture set '$setId' not found")

    item.textures[textureName]?.let { return it }

    item.metadata.fallback[textureName]?.let { alias ->
        return resolveTextureIdentifier(alias, setId, metadataMap, newVisited)
    }

    item.metadata.base?.let { baseId ->
        return resolveTextureIdentifier(textureName, baseId, metadataMap, newVisited)
    }

    if (textureName.endsWith("_active")) {
        val baseName = textureName.removeSuffix("_active")
        return resolveTextureIdentifier(baseName, setId, metadataMap, newVisited)
    }

    error("Cannot resolve texture '$textureName' in texture set '$setId'")
}

private fun generateTextureSet(
    packageName: String,
    textureSetClass: String,
    textureItemClass: String,
    textPackage: String,
    textClass: String,
    texturePackage: String,
    textureClass: String,
    metadataMap: Map<String, TextureSetItem>,
): FileSpec {
    val textClassName = ClassName(textPackage, textClass)
    val textureClassName = ClassName(texturePackage, textureClass)

    val builtInTextureSetsName = ClassName(packageName, textureSetClass)
    val textureSetClassName = ClassName("top.fifthlight.touchcontroller.common.assets", "TextureSet")
    val textureSetsClassName = ClassName("top.fifthlight.touchcontroller.common.assets", "TextureSets")
    val builtInTextureSetsInitializerName =
        ClassName("top.fifthlight.touchcontroller.common.assets", "BuiltInTextureSetsInitializer")

    val builtInTextureItemsName = ClassName(packageName, textureItemClass)
    val textureItemClassName = ClassName("top.fifthlight.touchcontroller.common.assets", "TextureItem")
    val textureItemsClassName = ClassName("top.fifthlight.touchcontroller.common.assets", "TextureItems")
    val builtInTextureItemsInitializerName =
        ClassName("top.fifthlight.touchcontroller.common.assets", "BuiltInTextureItemsInitializer")

    val textureSetBuilder = TypeSpec.objectBuilder(builtInTextureSetsName)
        .addSuperinterface(builtInTextureSetsInitializerName)
        .addAnnotation(
            AnnotationSpec.builder(ActualImpl::class)
                .addMember("%T::class", builtInTextureSetsInitializerName)
                .build()
        )
        .addFunction(
            FunSpec.builder("of")
                .addAnnotation(JvmStatic::class)
                .addAnnotation(ActualConstructor::class)
                .returns(builtInTextureSetsName)
                .addCode("return this")
                .build()
        )

    var defaultTextureSet: String? = null
    for ((id, item) in metadataMap) {
        val textureSetName = id.snakeToCamelCase()

        textureSetBuilder.addProperty(
            PropertySpec.builder(textureSetName, textureSetClassName)
                .initializer(
                    """%T.register(
                        |   id = %S,
                        |   name = %T.TEXTURE_SET_%L_NAME,
                        |   title = %T.TEXTURE_SET_%L_TITLE,
                        |   grayWhenActive = %L,
                        |   classic = %L,
                        |)""".trimMargin(),
                    textureSetsClassName,
                    textureSetName,
                    textClassName,
                    id.uppercase(),
                    textClassName,
                    id.uppercase(),
                    item.metadata.grayWhenActive,
                    item.metadata.classic,
                )
                .build()
        )

        if (item.metadata.default) {
            check(defaultTextureSet == null) {
                "Multiple default texture sets: $id vs $defaultTextureSet."
            }
            defaultTextureSet = id
            textureSetBuilder.addFunction(
                FunSpec.builder("register")
                    .addModifiers(KModifier.OVERRIDE)
                    .addCode("%T.registerFallback(%N)", textureSetsClassName, textureSetName)
                    .build()
            )
        }
    }

    val allTextures = metadataMap.values
        .flatMap { it.textures.keys }
        .distinct()
        .sorted()

    val sortedSetIds = metadataMap.keys.sorted()

    val textureItemBuilder = TypeSpec.objectBuilder(builtInTextureItemsName)
        .addSuperinterface(builtInTextureItemsInitializerName)
        .addAnnotation(
            AnnotationSpec.builder(ActualImpl::class)
                .addMember("%T::class", builtInTextureItemsInitializerName)
                .build()
        )
        .addFunction(
            FunSpec.builder("of")
                .addAnnotation(JvmStatic::class)
                .addAnnotation(ActualConstructor::class)
                .returns(builtInTextureItemsName)
                .addCode("return this")
                .build()
        )
        .addFunction(
            FunSpec.builder("register")
                .addModifiers(KModifier.OVERRIDE)
                .build()
        )

    requireNotNull(defaultTextureSet) { "No default texture set" }

    for (texture in allTextures) {
        val propertyName = texture.snakeToCamelCase()

        textureItemBuilder.addProperty(
            PropertySpec.builder(propertyName, textureItemClassName)
                .initializer(
                    "%T.register(%S, %S, get = %L)",
                    textureItemsClassName,
                    texture,
                    texture,
                    buildCodeBlock {
                        beginControlFlow("")
                        beginControlFlow("when (%N)", "it")
                        for (setId in sortedSetIds) {
                            val identifier = resolveTextureIdentifier(texture, setId, metadataMap)
                            val setPropertyName = setId.snakeToCamelCase()
                            addStatement(
                                "%T.%L -> %T.%L",
                                builtInTextureSetsName,
                                setPropertyName,
                                textureClassName,
                                identifier,
                            )
                        }

                        val identifier = resolveTextureIdentifier(texture, defaultTextureSet, metadataMap)
                        addStatement("else -> %T.%L", textureClassName, identifier)

                        endControlFlow()
                        endControlFlow()
                    }.toString(), // Call toString() here to workaround KotlinPoet
                )
                .build()
        )
    }

    return FileSpec.builder(packageName, "TextureSets")
        .addAnnotation(
            AnnotationSpec.builder(Suppress::class)
                .addMember("%S", "RedundantVisibilityModifier")
                .build()
        )
        .addType(textureSetBuilder.build())
        .addType(textureItemBuilder.build())
        .build()
}

private fun run(
    output: Path,
    packageName: String,
    textureSetClass: String,
    textureItemClass: String,
    textPackage: String,
    textClass: String,
    texturePackage: String,
    textureClass: String,
    textureSets: List<TextureSetInput>,
) {
    val metadataMap = parseTextureSets(textureSets)
    val fileSpec = generateTextureSet(
        packageName = packageName,
        textureSetClass = textureSetClass,
        textureItemClass = textureItemClass,
        textPackage = textPackage,
        textClass = textClass,
        texturePackage = texturePackage,
        textureClass = textureClass,
        metadataMap = metadataMap,
    )
    output.writeText(buildString { fileSpec.writeTo(this) })
}

fun main(vararg args: String) = object : Worker() {
    override fun handleRequest(
        out: PrintWriter,
        sandboxDir: Path?,
        vararg args: String
    ): Int {
        var output: Path? = null
        var packageName: String? = null
        var textureSetClassName: String? = null
        var textureItemClassName: String? = null
        var texturePackage: String? = null
        var textureClass: String? = null
        var textPackage: String? = null
        var textClass: String? = null
        val textureSets = mutableListOf<TextureSetInput>()

        var currentSet: Pair<String, Path>? = null
        val currentTextures = mutableListOf<TextureInput>()

        fun flushCurrentSet() {
            val set = currentSet ?: return
            textureSets.add(
                TextureSetInput(
                    set.first,
                    set.second,
                    currentTextures.toList()
                )
            )
            currentTextures.clear()
            currentSet = null
        }

        var i = 0

        fun nextArg() = args[i++]
        while (i in args.indices) {
            when (val arg = nextArg()) {
                "--output" -> output = sandboxDir?.resolve(Path.of(nextArg())) ?: Path.of(nextArg())
                "--package" -> packageName = nextArg()
                "--texture_set_class_name" -> textureSetClassName = nextArg()
                "--texture_item_class_name" -> textureItemClassName = nextArg()
                "--texture_package" -> texturePackage = nextArg()
                "--texture_class" -> textureClass = nextArg()
                "--text_package" -> textPackage = nextArg()
                "--text_class" -> textClass = nextArg()
                "--set" -> {
                    flushCurrentSet()
                    currentSet = Pair(nextArg(), sandboxDir?.resolve(Path.of(nextArg())) ?: Path.of(nextArg()))
                }

                "--texture" -> {
                    val (name, identifier) = nextArg().split(':', limit = 2)
                    currentTextures.add(TextureInput(name, identifier))
                }

                else -> {
                    out.println("Bad argument: $arg")
                    return 1
                }
            }
        }

        flushCurrentSet()
        run(
            output = requireNotNull(output) { "No output" },
            packageName = requireNotNull(packageName) { "No package name" },
            textureSetClass = requireNotNull(textureSetClassName) { "No texture set class name" },
            textureItemClass = requireNotNull(textureItemClassName) { "No texture item class name" },
            textPackage = requireNotNull(textPackage) { "No text package name" },
            textClass = requireNotNull(textClass) { "No text class" },
            texturePackage = requireNotNull(texturePackage) { "No texture package name" },
            textureClass = requireNotNull(textureClass) { "No texture class" },
            textureSets = textureSets,
        )
        return 0
    }
}.run(*args)
