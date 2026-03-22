package top.fifthlight.armorstand.manage.database

import top.fifthlight.armorstand.manage.repository.*
import top.fifthlight.armorstand.manage.schema.SchemaManager
import java.sql.Connection

interface TransactionScope {
    val connection: Connection
    val modelRepository: ModelRepository
    val animationRepository: AnimationRepository
    val favoriteRepository: FavoriteRepository
    val thumbRepository: ThumbnailRepository
    val fileCacheRepository: FileCacheRepository
    val scanSessionRepository: ScanSessionRepository
}

interface DatabaseManager {
    suspend fun start(
        driverClass: String,
        jdbcUrl: String,
        schemaManager: SchemaManager,
    )

    suspend fun stop()

    suspend fun <T> transaction(block: suspend TransactionScope.() -> T): T
}
