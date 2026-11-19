/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.state

internal class DistinctDispatcher<T : Any>(private val observer: (newValue: T, oldValue: T?) -> Unit) {
    private val lock = Any()
    private var lastValue: T? = null
    private var initialized = false

    fun dispatch(value: T) {
        val oldValue = synchronized(lock) {
            if (initialized && lastValue == value) {
                return
            }
            val previous = lastValue
            lastValue = value
            initialized = true
            previous
        }
        observer(value, oldValue)
    }
}
