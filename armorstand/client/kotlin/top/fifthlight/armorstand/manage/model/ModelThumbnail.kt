package top.fifthlight.armorstand.manage.model

import top.fifthlight.blazerod.model.Texture
import java.nio.file.Path

sealed class ModelThumbnail {
    data object None : ModelThumbnail()

    data class External(
        val path: Path,
    ) : ModelThumbnail()

    data class Embed(
        val offset: Long,
        val length: Long,
        val type: Texture.TextureType? = null,
    ) : ModelThumbnail()
}
