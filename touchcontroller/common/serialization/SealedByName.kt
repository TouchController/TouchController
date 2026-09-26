/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.serialization

import com.ubertob.kondor.json.JSealed
import com.ubertob.kondor.json.ObjectNodeConverter
import kotlin.reflect.KClass

class SealedByNameEntry<T : Any>(
    val clazz: KClass<T>,
    val converter: ObjectNodeConverter<T>,
)

class SealedByNameBuilder<T : Any> {
    private val entries = mutableListOf<Registered<T>>()
    private val byName = HashMap<String, ObjectNodeConverter<out T>>()

    inline fun <reified X : T> subtype(converter: ObjectNodeConverter<X>) =
        SealedByNameEntry(X::class, converter)

    infix fun <X : T> String.encodes(entry: SealedByNameEntry<X>) {
        listOf(this) encodes entry
    }

    infix fun <X : T> List<String>.encodes(entry: SealedByNameEntry<X>) {
        val primaryName = firstOrNull()
        requireNotNull(primaryName) { "No primary type name for ${entry.clazz.simpleName}" }
        require(entries.none { it.clazz.java == entry.clazz.java }) {
            "${entry.clazz.simpleName} is already declared"
        }
        entries += Registered(entry.clazz, primaryName)
        forEach { name ->
            val existing = byName.put(name, entry.converter)
            require(existing == null) { "$name is already bound with converter $existing" }
        }
    }

    internal fun build(): SealedByNameTable<T> {
        require(entries.isNotEmpty()) { "No sealed subtypes declared" }
        return SealedByNameTable(entries.toList(), byName.toMap())
    }
}

internal class Registered<T : Any>(
    val clazz: KClass<out T>,
    val name: String,
)

internal class SealedByNameTable<T : Any>(
    private val entries: List<Registered<T>>,
    val byName: Map<String, ObjectNodeConverter<out T>>,
) {
    fun nameOf(value: T): String =
        entries.firstOrNull { it.clazz.java.isInstance(value) }?.name
            ?: error("No type name declared for ${value.javaClass.simpleName}")
}

fun <T : Any> sealedByName(
    discriminatorFieldName: String = "type",
    builder: SealedByNameBuilder<T>.() -> Unit,
): JSealed<T> {
    return object : JSealed<T>() {
        private val table = SealedByNameBuilder<T>().apply(builder).build()
        override val discriminatorFieldName = discriminatorFieldName
        override val subConverters = table.byName
        override fun extractTypeName(obj: T): String = table.nameOf(obj)
    }
}
