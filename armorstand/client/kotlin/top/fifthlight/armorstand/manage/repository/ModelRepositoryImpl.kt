package top.fifthlight.armorstand.manage.repository

import top.fifthlight.armorstand.manage.ModelManager
import top.fifthlight.armorstand.manage.model.ModelItem
import top.fifthlight.armorstand.util.*
import java.nio.file.Path
import java.sql.Connection

class ModelRepositoryImpl(private val conn: Connection) : ModelRepository {
    override fun upsert(path: String, name: String, lastChanged: Long, sha256: ModelHash) {
        conn.prepareStatement(
            "MERGE INTO model(path, name, lastChanged, sha256) KEY(path) VALUES(?, ?, ?, ?)"
        ).bind {
            string(path)
            string(name)
            long(lastChanged)
            bytes(sha256.hash)
        }.use { ps ->
            ps.executeUpdate()
        }
    }

    override fun exists(path: String): Boolean =
        conn.prepareStatement("SELECT 1 FROM model WHERE path = ? LIMIT 1").bind {
            string(path)
        }.use { ps ->
            ps.executeQuery().use { it.next() }
        }

    override fun count(search: String?): Int =
        conn.prepareStatement(
            if (search == null) {
                "SELECT COUNT(*) FROM model"
            } else {
                "SELECT COUNT(*) FROM model WHERE LOCATE(LOWER(?),LOWER(name))>0"
            }
        ).bind {
            if (search != null) {
                string(search)
            }
        }.withExecuted {
            next()
            getInt(1)
        }

    override fun findRange(
        search: String?,
        order: ModelManager.Order,
        ascend: Boolean,
        limit: Int,
        offset: Int,
    ): List<ModelItem> {
        val sortDirection = if (ascend) {
            "ASC"
        } else {
            "DESC"
        }
        val orderColumn = when (order) {
            ModelManager.Order.NAME -> "m.name"
            ModelManager.Order.LAST_CHANGED -> "m.lastChanged"
        }
        val where = if (search == null) {
            ""
        } else {
            "WHERE LOCATE(LOWER(?), LOWER(m.name)) > 0"
        }
        val sql = """
            SELECT m.path, m.name, m.lastChanged, m.sha256, f.path IS NOT NULL
            FROM model m
                LEFT JOIN favorite f ON m.path = f.path
            $where
            ORDER BY
                CASE WHEN f.path IS NOT NULL THEN 0 ELSE 1 END,
                CASE WHEN f.path IS NOT NULL THEN f.favorite_at END DESC,
                $orderColumn $sortDirection
            LIMIT ? OFFSET ?
        """.trimIndent()

        return conn.prepareStatement(sql).bind {
            if (search != null) {
                string(search)
            }
            int(limit)
            int(offset)
        }.mapExecuted {
            ModelItem(
                path = Path.of(getString(1)).normalize(),
                name = getString(2),
                lastChanged = getLong(3),
                hash = ModelHash(getBytes(4)),
                favorite = getBoolean(5),
            )
        }
    }

    override fun findByPath(path: String): ModelItem? = conn.prepareStatement(
        """
        SELECT m.path, m.name, m.lastChanged, m.sha256, f.path IS NOT NULL
        FROM model m
            LEFT JOIN favorite f ON m.path = f.path
        WHERE m.path = ?
        """.trimIndent()
    ).bind {
        string(path)
    }.firstExecuted {
        ModelItem(
            path = Path.of(getString(1)).normalize(),
            name = getString(2),
            lastChanged = getLong(3),
            hash = ModelHash(getBytes(4)),
            favorite = getBoolean(5),
        )
    }

    override fun findByHash(hash: ModelHash): ModelItem? = conn.prepareStatement(
        """
        SELECT m.path, m.name, m.lastChanged, m.sha256, f.path IS NOT NULL
        FROM model m
          LEFT JOIN favorite f ON m.path = f.path
        WHERE m.sha256 = ?
        """.trimIndent()
    ).bind {
        bytes(hash.hash)
    }.firstExecuted {
        ModelItem(
            path = Path.of(getString(1)).normalize(),
            name = getString(2),
            lastChanged = getLong(3),
            hash = ModelHash(getBytes(4)),
            favorite = getBoolean(5),
        )
    }

    override fun exists(path: String, hash: ModelHash) = conn.prepareStatement(
        "SELECT 1 FROM model WHERE path = ? and sha256 = ?"
    ).bind {
        string(path)
        bytes(hash.hash)
    }.exists()
}
