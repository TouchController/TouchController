package top.fifthlight.touchcontroller.common.about

import kotlinx.serialization.Serializable

@Serializable
data class Developer(
    val name: String? = null,
)

@Serializable
data class Library(
    val uniqueId: String,
    val name: String,
    val artifactVersion: String? = null,
    val description: String? = null,
    val developers: List<Developer> = listOf(),
    val licenses: List<String> = listOf(),
    val website: String? = null,
)

@Serializable
data class License(
    val content: String? = null,
    val name: String,
    val url: String? = null,
)

@Serializable
data class Libs(
    val libraries: List<Library> = listOf(),
    val licenses: Map<String, License> = mapOf(),
)
