package top.fifthlight.touchcontroller.resources.generator

import com.squareup.kotlinpoet.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import top.fifthlight.bazel.worker.api.Worker
import java.io.PrintWriter
import java.nio.file.Path
import kotlin.io.path.writeText

private fun generateEmptyTexture(
    packageName: String,
    className: String,
    textPackage: String,
    textClass: String,
    texturePackage: String,
    textureClass: String,
    identifiers: List<String>,
): FileSpec {
    val textClassName = ClassName(textPackage, textClass)
    val textureClassName = ClassName(texturePackage, textureClass)
    val textureTypeName = ClassName("top.fifthlight.combine.paint", "Texture")
    val identifierTypeName = ClassName("top.fifthlight.combine.data", "Identifier")

    val emptyTextureType = TypeSpec.enumBuilder(className)
        .addAnnotation(Serializable::class)
        .primaryConstructor(
            FunSpec
                .constructorBuilder()
                .addParameter(
                    ParameterSpec
                        .builder("texture", textureTypeName)
                        .build()
                )
                .addParameter(
                    ParameterSpec
                        .builder("nameId", identifierTypeName)
                        .build()
                )
                .build()
        )
        .addProperty(
            PropertySpec
                .builder("texture", textureTypeName)
                .initializer("texture")
                .build()
        )
        .addProperty(
            PropertySpec
                .builder("nameId", identifierTypeName)
                .initializer("nameId")
                .build()
        )

    for (identifier in identifiers.sorted()) {
        val enumName = identifier.uppercase()
        emptyTextureType.addEnumConstant(
            enumName,
            TypeSpec
                .anonymousClassBuilder()
                .addAnnotation(
                    AnnotationSpec
                        .builder(SerialName::class)
                        .addMember("%S", identifier)
                        .build()
                )
                .addSuperclassConstructorParameter("%T.%L", textureClassName, identifier)
                .addSuperclassConstructorParameter("%T.EMPTY_TEXTURE_%L", textClassName, enumName)
                .build()
        )
    }

    return FileSpec
        .builder(packageName, className)
        .addAnnotation(
            AnnotationSpec
                .builder(Suppress::class)
                .addMember("%S", "RedundantVisibilityModifier")
                .build()
        )
        .addImport("top.fifthlight.data", "IntSize", "IntOffset", "IntRect", "IntPadding")
        .addType(emptyTextureType.build())
        .build()
}

private fun run(
    output: Path,
    packageName: String,
    className: String,
    textPackage: String,
    textClass: String,
    texturePackage: String,
    textureClass: String,
    identifiers: List<String>,
) {
    val fileSpec = generateEmptyTexture(
        packageName = packageName,
        className = className,
        textPackage = textPackage,
        textClass = textClass,
        texturePackage = texturePackage,
        textureClass = textureClass,
        identifiers = identifiers,
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
        var className: String? = null
        var texturePackage: String? = null
        var textureClass: String? = null
        var textPackage: String? = null
        var textClass: String? = null
        val identifiers = mutableListOf<String>()

        var i = 0

        fun nextArg() = args[i++]
        while (i in args.indices) {
            when (val arg = nextArg()) {
                "--output" -> output = sandboxDir?.resolve(Path.of(nextArg())) ?: Path.of(nextArg())
                "--package" -> packageName = nextArg()
                "--class_name" -> className = nextArg()
                "--texture_package" -> texturePackage = nextArg()
                "--texture_class" -> textureClass = nextArg()
                "--text_package" -> textPackage = nextArg()
                "--text_class" -> textClass = nextArg()
                "--identifier" -> identifiers.add(nextArg())
                else -> {
                    out.println("Bad argument: $arg")
                    return 1
                }
            }
        }

        run(
            output = requireNotNull(output) { "No output" },
            packageName = requireNotNull(packageName) { "No package name" },
            className = requireNotNull(className) { "No class name" },
            textPackage = requireNotNull(textPackage) { "No text package name" },
            textClass = requireNotNull(textClass) { "No text class" },
            texturePackage = requireNotNull(texturePackage) { "No texture package name" },
            textureClass = requireNotNull(textureClass) { "No texture class" },
            identifiers = identifiers,
        )
        return 0
    }
}.run(*args)
