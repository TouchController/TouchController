package top.fifthlight.armorstand.manage.repository

import top.fifthlight.armorstand.manage.ModelManager
import top.fifthlight.armorstand.manage.model.ModelItem
import top.fifthlight.armorstand.util.ModelHash

interface ModelRepository {
    fun upsert(path: String, name: String, lastChanged: Long, sha256: ModelHash)
    fun exists(path: String): Boolean
    fun count(search: String? = null): Int
    fun findRange(
        search: String?,
        order: ModelManager.Order,
        ascend: Boolean,
        limit: Int,
        offset: Int,
    ): List<ModelItem>

    fun findByPath(path: String): ModelItem?
    fun findByHash(hash: ModelHash): ModelItem?
    fun exists(path: String, hash: ModelHash): Boolean
}
