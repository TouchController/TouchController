package top.fifthlight.combine.resources.kotlin

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.path
import com.github.ajalt.clikt.parsers.CommandLineParser
import com.squareup.kotlinpoet.*
import top.fifthlight.bazel.worker.api.Worker
import java.io.PrintWriter
import java.nio.file.Path
import kotlin.io.path.writeText

private class Generator : CliktCommand() {
    val output: Path by option("--output").path().required()
    val packageName: String by option("--package").required()
    val className: String by option("--class_name").required()
    val textures: List<String> by option("--texture").multiple()
    val backgrounds: List<String> by option("--background").multiple()
    val ninePatches: List<String> by option("--ninepatch").multiple()

    override fun run() {
        val classSpec = TypeSpec.interfaceBuilder(className)
            .apply {
                for (texture in textures) {
                    addProperty(
                        PropertySpec
                            .builder(texture, ClassName("top.fifthlight.combine.paint", "Texture"))
                            .addModifiers(KModifier.ABSTRACT)
                            .build()
                    )
                }
                for (ninePatch in ninePatches) {
                    addProperty(
                        PropertySpec
                            .builder(ninePatch, ClassName("top.fifthlight.combine.paint", "Texture"))
                            .addModifiers(KModifier.ABSTRACT)
                            .build()
                    )
                }
                for (background in backgrounds) {
                    addProperty(
                        PropertySpec
                            .builder(background, ClassName("top.fifthlight.combine.paint", "BackgroundTexture"))
                            .addModifiers(KModifier.ABSTRACT)
                            .build()
                    )
                }
                addType(
                    TypeSpec.interfaceBuilder("Factory")
                        .addAnnotation(ClassName("top.fifthlight.mergetools.api", "ExpectFactory"))
                        .addFunction(
                            FunSpec.builder("of")
                                .addModifiers(KModifier.ABSTRACT)
                                .returns(ClassName(packageName, className))
                                .build()
                        )
                        .build()
                )
                addType(
                    TypeSpec.companionObjectBuilder()
                        .addSuperinterface(
                            superinterface = ClassName(packageName, className),
                            delegate = CodeBlock.of("${className}Factory.of()")
                        )
                        .build()
                )
            }.build()

        val file = FileSpec
            .builder(packageName, className)
            .addAnnotation(
                AnnotationSpec
                    .builder(Suppress::class)
                    .addMember("%S", "RedundantVisibilityModifier")
                    .build()
            )
            .addType(classSpec)
            .build()
        output.writeText(buildString { file.writeTo(this) })
    }
}

fun main(vararg args: String) = object : Worker() {
    override fun handleRequest(
        out: PrintWriter,
        sandboxDir: Path?,
        vararg args: String
    ): Int {
        CommandLineParser.parseAndRun(Generator(), args.toList()) { command ->
            try {
                command.run()
                return 0
            } catch (e: CliktError) {
                command.echoFormattedHelp(e)
                return e.statusCode
            }
        }
        return 0
    }
}.run(*args)
