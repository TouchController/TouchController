package top.fifthlight.touchcontroller.resources.generator

import com.squareup.kotlinpoet.*
import kotlinx.serialization.json.Json
import top.fifthlight.bazel.worker.api.Worker
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
        val languageFile = sandboxDir?.resolve(Path.of(args[0])) ?: Path.of(args[0])
        val outputFile = sandboxDir?.resolve(Path.of(args[1])) ?: Path.of(args[1])
        val className = args[2]
        val packageName = args[3]
        val map: Map<String, String> = Json.decodeFromString(languageFile.readText())

        val textsBuilder = TypeSpec.objectBuilder("Texts")

        for ((key, value) in map) {
            if (!key.startsWith("touchcontroller.")) {
                continue
            }
            val strippedKey = key.removePrefix("touchcontroller.")
            val transformedKey = strippedKey.uppercase().replace('.', '_')

            textsBuilder.addProperty(
                PropertySpec
                    .builder(transformedKey, ClassName("top.fifthlight.combine.data", "Identifier"))
                    .addKdoc("Translation text: %L", value)
                    .initializer("Identifier.Namespaced(%S, %S)", "touchcontroller", strippedKey)
                    .build()
            )
        }

        val texts = textsBuilder.build()
        val file = FileSpec
            .builder(packageName, className)
            .addAnnotation(
                AnnotationSpec
                    .builder(Suppress::class)
                    .addMember("%S", "RedundantVisibilityModifier")
                    .build()
            )
            .addType(texts)
            .build()
        buildString {
            file.writeTo(this)
        }.let {
            outputFile.writeText(it)
        }
        return 0
    }
}.run(*args)
