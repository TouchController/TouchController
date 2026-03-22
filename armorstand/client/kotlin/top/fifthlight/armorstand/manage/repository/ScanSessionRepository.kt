package top.fifthlight.armorstand.manage.repository

import top.fifthlight.armorstand.util.ModelHash

interface ScanSessionRepository {
    // CREATE TEMP TABLE ...
    fun open()

    // DROP TEMP TABLE ...
    fun close()
    fun markFileSha(sha256: ModelHash)
    fun markModelPath(path: String)
    fun markMarkerModelPath(path: String)
    fun markAnimationPath(path: String)
    fun markThumbnailSha(sha256: ModelHash)
    fun isMarkerModelMarked(path: String): Boolean
    fun isThumbnailMarked(sha256: ModelHash): Boolean

    // DELETE ... WHERE NOT EXISTS(...)
    fun cleanup()
}
