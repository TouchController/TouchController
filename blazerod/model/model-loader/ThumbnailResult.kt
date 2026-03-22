package top.fifthlight.blazerod.model.loader

import top.fifthlight.blazerod.model.Texture

sealed class ThumbnailResult {
    object Unsupported : ThumbnailResult()

    object None : ThumbnailResult()

    data class Embed(
        val offset: Long,
        val length: Long,
        val type: Texture.TextureType? = null,
    ) : ThumbnailResult() {
        init {
            require(offset >= 0) { "Bad offset: $offset" }
            require(length >= 0) { "Bad length: $length" }
        }
    }
}
