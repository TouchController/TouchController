package top.fifthlight.aboutlibraries.config

import java.net.URI
import java.nio.file.Files
import java.nio.file.Path

data class Config(
    val repositories: List<Pair<String, URI>>,
    val coordinates: List<Coordinate>,
) {
    data class Coordinate(
        val groupId: String,
        val artifactId: String,
        val version: String,
    )
}

fun Config(path: Path): Config {
    val repositories = mutableListOf<Pair<String, URI>>()
    val coordinates = mutableListOf<Config.Coordinate>()

    for (line in Files.readAllLines(path)) {
        val content = line.trim()
        if (content.startsWith("#") || content.isEmpty()) {
            continue
        }

        val keyIndex = line.indexOf('=')
        if (keyIndex != -1) {
            val key = content.substring(0, keyIndex)
            val value = content.substring(keyIndex + 1)

            repositories.add(key to URI(value))
            continue
        }

        val parts = content.split(':')
        check(parts.size == 3) { "Bad coordinate: $content" }
        coordinates.add(Config.Coordinate(parts[0], parts[1], parts[2]))
    }

    return Config(repositories, coordinates)
}
