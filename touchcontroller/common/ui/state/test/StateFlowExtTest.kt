/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.state.test

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Test
import top.fifthlight.touchcontroller.common.ui.state.asValue
import kotlin.time.Duration.Companion.seconds

class StateFlowExtTest {
    @Test
    fun testReadsCurrentState() {
        val flow = MutableStateFlow("initial")
        assertEquals("initial", flow.asValue(CoroutineScope(Dispatchers.Default)).value)
    }

    @Test
    fun testSubscribesToUpdates() = runBlocking {
        val flow = MutableStateFlow("a")
        val scope = CoroutineScope(Dispatchers.Default)
        val value = flow.asValue(scope)

        val results = mutableListOf<String>()
        val cancellation = value.subscribe { synchronized(results) { results.add(it) } }

        withTimeout(10.seconds) {
            while (results.isEmpty()) {
                yield()
            }
        }
        flow.value = "b"
        withTimeout(10.seconds) {
            while (results.size < 2) {
                yield()
            }
        }
        cancellation.cancel()
        flow.value = "c"

        synchronized(results) {
            assertEquals("a", results.first())
            assertEquals("b", results.last())
        }
    }
}
