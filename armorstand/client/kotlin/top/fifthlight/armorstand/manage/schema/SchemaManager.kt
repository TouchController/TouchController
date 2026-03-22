package top.fifthlight.armorstand.manage.schema

import java.sql.Connection

interface SchemaManager {
    fun maintainSchema(conn: Connection)
}
