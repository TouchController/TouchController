/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.serialization

import com.ubertob.kondor.json.JStringRepresentable
import java.util.*
import kotlin.enums.EnumEntries
import kotlin.enums.enumEntries

class EnumByNameBuilder<E : Enum<E>>(
    enumClass: Class<E>,
    private val enumEntries: EnumEntries<E>,
) {
    private val enumName = enumClass.simpleName
    private val byValue = EnumMap<E, String>(enumClass)
    private val byString = HashMap<String, E>()

    infix fun String.encodes(value: E) {
        val existingName = byValue.put(value, this)
        require(existingName == null) { "$enumName.$value is already bound with name $existingName" }
        val existingEnum = byString.put(this, value)
        require(existingEnum == null) { "$this is already bound with enum value $existingEnum" }
    }

    infix fun List<String>.encodes(value: E) {
        val primaryName = firstOrNull()
        requireNotNull(primaryName) { "No primary value for $enumName.$value" }
        val existingName = byValue.put(value, primaryName)
        require(existingName == null) { "$enumName.$value is already bound with name $existingName" }
        forEach { name ->
            val existingEnum = byString.put(name, value)
            require(existingEnum == null) { "$name is already bound with enum value $existingEnum" }
        }
    }

    fun build(): EnumByNameTable<E> {
        val missing = enumEntries.filterNot { it in byValue }
        require(missing.isEmpty()) { "Bad enum table for $enumName: missing entries $missing" }
        return EnumByNameTable(byValue.toMap(), byString.toMap())
    }
}

data class EnumByNameTable<E : Enum<E>>(
    val byValue: Map<E, String>,
    val byString: Map<String, E>,
)

inline fun <reified E : Enum<E>> enumByName(
    crossinline builder: EnumByNameBuilder<E>.() -> Unit,
): JStringRepresentable<E> {
    val enumClass = E::class.java
    val enumName = enumClass.simpleName
    val table = EnumByNameBuilder(enumClass, enumEntries<E>()).apply(builder).build()
    return object : JStringRepresentable<E>() {
        override val cons: (String) -> E = { name ->
            table.byString[name] ?: error("Unknown $enumName value: $name")
        }

        override val render: (E) -> String = { value ->
            requireNotNull(table.byValue[value]) { "Unregistered $enumName value: $value" }
        }
    }
}
