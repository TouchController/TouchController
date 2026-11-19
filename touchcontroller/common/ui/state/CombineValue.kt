/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.state

import com.arkivanov.decompose.Cancellation
import com.arkivanov.decompose.value.Value

// Combine for Decompose Value, with identity-based caching and distinct emission like Value.map
private class CombinedValue<out R : Any>(
    private val upstreams: List<Value<Any>>,
    private val transform: (List<Any>) -> R,
) : Value<R>() {
    private val lock = Any()
    private var lastUpstreamValues: Array<Any?> = arrayOfNulls(upstreams.size)
    private var initialized = false
    private var lastResult: R? = null

    override val value: R
        get() = synchronized(lock) { compute(upstreams.map { it.value }) }

    private fun compute(values: List<Any>): R {
        var changed = !initialized
        for (i in values.indices) {
            if (!initialized || values[i] !== lastUpstreamValues[i]) {
                lastUpstreamValues[i] = values[i]
                changed = true
            }
        }
        if (changed) {
            lastResult = transform(values)
            initialized = true
        }
        @Suppress("UNCHECKED_CAST")
        return lastResult as R
    }

    override fun subscribe(observer: (R) -> Unit): Cancellation {
        val cancellations = upstreams.map { upstream ->
            upstream.subscribe { observer(synchronized(lock) { compute(upstreams.map { it.value }) }) }
        }
        return Cancellation { cancellations.forEach { it.cancel() } }
    }
}

@Suppress("UNCHECKED_CAST")
fun <T1 : Any, T2 : Any, R : Any> combineValues(flow1: Value<T1>, flow2: Value<T2>, transform: (a: T1, b: T2) -> R): Value<R> {
    val upstreams = listOf(flow1, flow2)
    return CombinedValue(upstreams) { transform(it[0] as T1, it[1] as T2) }
}

@Suppress("UNCHECKED_CAST")
fun <T1 : Any, T2 : Any, T3 : Any, R : Any> combineValues(flow1: Value<T1>, flow2: Value<T2>, flow3: Value<T3>, transform: (a: T1, b: T2, c: T3) -> R): Value<R> {
    val upstreams = listOf(flow1, flow2, flow3)
    return CombinedValue(upstreams) { transform(it[0] as T1, it[1] as T2, it[2] as T3) }
}
