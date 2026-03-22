package top.fifthlight.blazerod.model

data class Texture(
    val name: String? = null,
    val bufferView: BufferView? = null,
    val type: TextureType? = null,
    val sampler: Sampler,
) {
    enum class TextureType(
        val mimeType: String,
        val magic: ByteArray,
    ) {
        PNG("image/png", byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)),
        JPEG("image/jpeg", byteArrayOf(0xFF.toByte(), 0xD8.toByte())),
    }

    data class Sampler(
        val magFilter: MagFilter = MagFilter.LINEAR,
        val minFilter: MinFilter = MinFilter.NEAREST,
        val wrapS: WrapMode = WrapMode.REPEAT,
        val wrapT: WrapMode = WrapMode.REPEAT,
    ) {
        enum class MagFilter {
            NEAREST,
            LINEAR
        }

        enum class MinFilter {
            NEAREST,
            LINEAR,
            NEAREST_MIPMAP_NEAREST,
            LINEAR_MIPMAP_NEAREST,
            NEAREST_MIPMAP_LINEAR,
            LINEAR_MIPMAP_LINEAR,
        }

        enum class WrapMode {
            REPEAT,
            MIRRORED_REPEAT,
            CLAMP_TO_EDGE
        }
    }
}
