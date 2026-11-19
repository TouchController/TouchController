/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.state.test

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import top.fifthlight.touchcontroller.common.ui.state.subscribeDistinct
import top.fifthlight.touchcontroller.common.ui.state.watchDistinct
import java.util.concurrent.atomic.AtomicInteger

class SubscribeDistinctTest {
    @Test
    fun testSkipsEqualValues() {
        val value = MutableValue(1)
        val counter = AtomicInteger()
        val cancellation = value.subscribeDistinct { counter.incrementAndGet() }

        value.value = 1 // equal: filtered
        value.value = 2
        value.value = 2 // equal: filtered
        cancellation.cancel()
        value.value = 3

        assertEquals(2, counter.get())
    }

    @Test
    fun testWatchReportsPreviousValueAndSkipsEqualValues() {
        val value = MutableValue(1)
        val events = mutableListOf<Pair<Int, Int?>>()
        value.watchDistinct { newValue, oldValue -> events += newValue to oldValue }

        value.value = 1
        value.value = 2
        value.value = 2
        value.value = 3

        assertEquals(listOf(1 to null, 2 to 1, 3 to 2), events)
    }

    @Test
    fun testWatchStopsAfterCancellation() {
        val value = MutableValue(1)
        val events = mutableListOf<Int>()
        val cancellation = value.watchDistinct { newValue, _ -> events += newValue }
        cancellation.cancel()

        value.value = 2

        assertEquals(listOf(1), events)
    }

    @Test
    fun testWatchObservesOnlyWhileStarted() {
        val lifecycle = LifecycleRegistry()
        val value = MutableValue(0)
        val events = mutableListOf<Pair<Int, Int?>>()
        value.watchDistinct(lifecycle) { newValue, oldValue -> events += newValue to oldValue }

        lifecycle.onCreate()
        lifecycle.onStart()
        value.value = 1
        lifecycle.onStop()
        value.value = 2
        lifecycle.onStart()

        assertEquals(listOf(0 to null, 1 to 0, 2 to 1), events)
    }
}
