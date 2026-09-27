/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.aboutlibraries.checker

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.path
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import top.fifthlight.aboutlibraries.config.Config
import java.nio.file.Files
import java.nio.file.Path

@Serializable
private data class MavenArtifact(
    val version: String,
)

@Serializable
private data class MavenInstall(
    val artifacts: Map<String, MavenArtifact> = emptyMap(),
)

class LibrariesConfChecker : CliktCommand() {
    val conf: Path by option().path().required().help("Path to libraries.conf")
    val mavenLock: Path by option().path().required().help("Path to maven_install.json")

    override fun run() {
        val format = Json { ignoreUnknownKeys = true }
        val mavenInstall = format.decodeFromString<MavenInstall>(Files.readString(mavenLock))
        val coordinates = Config(conf).coordinates

        val problems = mutableListOf<String>()
        for ((groupId, artifactId, version) in coordinates) {
            val uniqueId = "$groupId:$artifactId"
            val artifact = mavenInstall.artifacts[uniqueId]
            when {
                artifact == null -> problems.add(
                    "$uniqueId: not found in ${mavenLock.fileName}"
                )

                artifact.version != version -> problems.add(
                    "$uniqueId: libraries.conf declares $version, " +
                            "but ${mavenLock.fileName} resolves ${artifact.version}"
                )
            }
        }

        if (problems.isNotEmpty()) {
            throw CliktError(
                buildString {
                    appendLine("libraries.conf is out of sync with ${mavenLock.fileName}:")
                    problems.forEach { appendLine("  $it") }
                }.trimEnd()
            )
        }

        echo("Checked ${coordinates.size} libraries against ${mavenLock.fileName}")
    }
}

fun main(args: Array<String>) = LibrariesConfChecker().main(args)
