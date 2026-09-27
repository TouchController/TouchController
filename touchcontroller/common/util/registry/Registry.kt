/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.util.registry

interface Registry<T>: Iterable<T> {
    operator fun get(id: String): T?
    fun getId(value: T): String?

    fun keys(): Set<String>
    fun values(): Collection<T>

    override fun iterator(): Iterator<T> = values().iterator()
}

interface MutableRegistry<T> : Registry<T> {
    fun register(id: String, value: T): MutableRegistry<T>

    operator fun set(id: String, value: T) {
        register(id, value)
    }
}
