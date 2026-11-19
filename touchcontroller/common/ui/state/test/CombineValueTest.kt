/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.state.test

import com.arkivanov.decompose.value.MutableValue
import org.junit.Assert.assertEquals
import org.junit.Test
import top.fifthlight.touchcontroller.common.ui.state.combineValues
import java.util.concurrent.atomic.AtomicInteger

private data class Box(val value: Int)

class CombineValueTest {
    @Test
    fun testInitialValueIsTransformed() {
        val combined = combineValues(MutableValue(1), MutableValue("a")) { i, s -> "$s$i" }
        assertEquals("a1", combined.value)
    }

    @Test
    fun testThreeUpstreams() {
        val combined = combineValues(
            MutableValue(1),
            MutableValue(2),
            MutableValue(3),
        ) { a, b, c -> a + b + c }
        assertEquals(6, combined.value)
    }

    @Test
    fun testRecomputeAfterUpstreamChange() {
        val v1 = MutableValue(1)
        val v2 = MutableValue("a")
        val combined = combineValues(v1, v2) { i, s -> "$s$i" }
        assertEquals("a1", combined.value)

        v1.value = 2
        assertEquals("a2", combined.value)

        v2.value = "b"
        assertEquals("b2", combined.value)
    }

    @Test
    fun testTransformNotInvokedWhenUpstreamIdentityUnchanged() {
        val counter = AtomicInteger()
        val v = MutableValue(Box(1))
        val combined = combineValues(v, MutableValue("x")) { b, s ->
            counter.incrementAndGet()
            "${b.value}$s"
        }
        assertEquals("1x", combined.value)
        assertEquals(1, counter.get())

        // Same instance: identity cache skips transform
        assertEquals("1x", combined.value)
        assertEquals(1, counter.get())

        // Structurally equal but different instance: recompute
        v.value = Box(1)
        assertEquals("1x", combined.value)
        assertEquals(2, counter.get())
    }

    @Test
    fun testSubscriptionReceivesCombinedUpdates() {
        val v1 = MutableValue(1)
        val v2 = MutableValue(10)
        val combined = combineValues(v1, v2) { a, b -> a + b }

        val results = mutableListOf<Int>()
        val cancellation = combined.subscribe { results.add(it) }

        v1.value = 2
        v2.value = 20
        cancellation.cancel()
        v1.value = 3

        // Each upstream emits once on subscription
        assertEquals(listOf(11, 11, 12, 22), results)
    }
}
