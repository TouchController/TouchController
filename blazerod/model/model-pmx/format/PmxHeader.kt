package top.fifthlight.blazerod.model.pmx.format

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

internal data class PmxHeader(
    val version: Float,
    val globals: PmxGlobals,
    val modelNameLocal: String,
    val modelNameUniversal: String,
    val commentLocal: String,
    val commentUniversal: String,
)

internal data class PmxGlobals(
    val textEncoding: TextEncoding,
    val additionalVec4Count: Int,
    val vertexIndexSize: Int,
    val textureIndexSize: Int,
    val materialIndexSize: Int,
    val boneIndexSize: Int,
    val morphIndexSize: Int,
    val rigidBodyIndexSize: Int,
) {
    enum class TextEncoding(
        val charset: Charset,
    ) {
        UTF16LE(StandardCharsets.UTF_16LE),
        UTF8(StandardCharsets.UTF_8),
    }
}
