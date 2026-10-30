/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.serialization

import com.ubertob.kondor.json.CharWriter
import com.ubertob.kondor.json.JString
import com.ubertob.kondor.json.JsonConverter
import com.ubertob.kondor.json.JsonStyle
import com.ubertob.kondor.json.JsonStyle.Companion.appendObjectValue
import com.ubertob.kondor.json.NamedAppender
import com.ubertob.kondor.json.ObjectNodeConverter
import com.ubertob.kondor.json.jsonnode.JsonNodeObject
import com.ubertob.kondor.json.jsonnode.JsonNodeString
import com.ubertob.kondor.json.jsonnode.asObjFieldMap

const val JSON_SCHEMA_DRAFT = "http://json-schema.org/draft-07/schema#"

class JObjWithSchema<T : Any>(
    private val uri: String,
    private val wrapped: ObjectNodeConverter<T>,
) : ObjectNodeConverter<T> by wrapped {
    override fun fieldAppenders(valueObject: T) = listOf<NamedAppender>(
        $$"$schema" to { style, offset ->
            JString.appendValue(this, style, offset, uri)
        },
    ) + wrapped.fieldAppenders(valueObject)

    override fun appendValue(app: CharWriter, style: JsonStyle, offset: Int, value: T) =
        app.appendObjectValue(style, offset, fieldAppenders(value))

    override fun schema(): JsonNodeObject = wrapped.schema()
}

fun JsonConverter<*, *>.schemaObj() = JsonNodeObject(
    schema().asObjFieldMap()!! + ($$"$schema" to JsonNodeString(JSON_SCHEMA_DRAFT))
)
