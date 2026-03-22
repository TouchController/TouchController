package top.fifthlight.armorstand.manage.repository

import top.fifthlight.armorstand.manage.AnimationItem
import top.fifthlight.armorstand.util.ModelHash

interface AnimationRepository {
    fun upsert(path: String, name: String, lastChanged: Long, sha256: ModelHash)
    fun exists(path: String): Boolean
    fun findAll(): List<AnimationItem>
}
