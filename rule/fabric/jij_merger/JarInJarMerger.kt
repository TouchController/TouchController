package top.fifthlight.fabazel.jijmerger

import kotlinx.serialization.json.*
import top.fifthlight.bazel.worker.api.Worker
import java.io.ByteArrayOutputStream
import java.io.PrintWriter
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.jar.JarEntry
import java.util.jar.JarInputStream
import java.util.jar.JarOutputStream
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

private const val DOS_EPOCH = 315532800000L

private fun JarEntry.clearTime() {
    creationTime = FileTime.fromMillis(DOS_EPOCH)
    lastAccessTime = FileTime.fromMillis(DOS_EPOCH)
    lastModifiedTime = FileTime.fromMillis(DOS_EPOCH)
    timeLocal = LocalDateTime.ofEpochSecond(DOS_EPOCH / 1000, 0, ZoneOffset.UTC)
}

fun main(args: Array<String>) = object : Worker() {
    override fun handleRequest(
        out: PrintWriter,
        sandboxDir: Path?,
        vararg args: String
    ): Int {
        val inputJar = sandboxDir?.resolve(Path.of(args[0])) ?: Path.of(args[0])
        val outputJar = sandboxDir?.resolve(Path.of(args[1])) ?: Path.of(args[1])
        val entries = args.slice(2 until args.size)
            .chunked(2)
            .map { (name, filePath) ->
                val (name, version) = name.split(":")
                Triple(name, version.takeIf { it != "=" }, sandboxDir?.resolve(Path.of(filePath)) ?: Path.of(filePath))
            }

        JarOutputStream(outputJar.outputStream()).use { output ->
            for ((name, version, path) in entries) {
                try {
                    val entry = JarEntry("META-INF/jars/${name}.jar").also { it.clearTime() }
                    output.putNextEntry(entry)
                    if (version == null) {
                        path.inputStream().use { it.transferTo(output) }
                    } else {
                        val newJarStream = ByteArrayOutputStream()
                        JarOutputStream(newJarStream).use { newOutput ->
                            val fabricModJsonEntry = JarEntry("fabric.mod.json").also { it.clearTime() }
                            val fabricModJsonText = Json.encodeToString(
                                FabricModJson(
                                    schemaVersion = 1,
                                    id = name,
                                    version = version,
                                    name = name,
                                    custom = mapOf(
                                        "fabric-loom:generated" to true
                                    )
                                )
                            )
                            newOutput.putNextEntry(fabricModJsonEntry)
                            newOutput.writer().apply {
                                write(fabricModJsonText)
                                flush()
                            }
                            newOutput.closeEntry()
                            JarInputStream(path.inputStream()).use { input ->
                                var entry: JarEntry? = input.nextJarEntry
                                while (entry != null) {
                                    try {
                                        newOutput.putNextEntry(entry)
                                        input.transferTo(newOutput)
                                        newOutput.flush()
                                    } finally {
                                        newOutput.closeEntry()
                                        input.closeEntry()
                                    }
                                    entry = input.nextJarEntry
                                }
                            }
                        }
                        output.write(newJarStream.toByteArray())
                    }
                } finally {
                    output.closeEntry()
                }
            }

            JarInputStream(inputJar.inputStream()).use { input ->
                var entry: JarEntry? = input.nextJarEntry
                while (entry != null) {
                    try {
                        entry.clearTime()
                        output.putNextEntry(entry)
                        if (entry.name.equals("fabric.mod.json", true)) {
                            val content = input.reader().readText()
                            val element = Json.parseToJsonElement(content).jsonObject
                            val jars = JsonArray(entries.map { (name) ->
                                Json.encodeToJsonElement(NestedJarEntry(file = "META-INF/jars/$name.jar"))
                            })
                            val finalElement = JsonObject(element + Pair("jars", jars))
                            val modified = Json.encodeToString(finalElement)
                            output.writer().apply {
                                write(modified)
                                flush()
                            }
                        } else {
                            input.transferTo(output)
                            output.flush()
                        }
                    } finally {
                        output.closeEntry()
                        input.closeEntry()
                    }
                    entry = input.nextJarEntry
                }
            }
        }
        return 0
    }
}.run(*args)
