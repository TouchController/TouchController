package top.fifthlight.armorstand.manage.repository

import top.fifthlight.armorstand.manage.model.ModelThumbnail
import top.fifthlight.armorstand.util.ModelHash

interface ThumbnailRepository {
    fun existsEmbed(sha256: ModelHash): Boolean
    fun insertEmbed(sha256: ModelHash, offset: Long, length: Long, mimeType: String?)
    fun findEmbed(sha256: ModelHash): ModelThumbnail
}
