/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.state.test

import org.junit.Assert.assertEquals
import org.junit.Test
import top.fifthlight.touchcontroller.common.ui.state.ImmutableValue

class ImmutableValueTest {
    @Test
    fun testExposesValue() {
        assertEquals(42, ImmutableValue(42).value)
    }

    @Test
    fun testSubscribeEmitsCurrentValueOnce() {
        val results = mutableListOf<String>()
        val cancellation = ImmutableValue("hello").subscribe { results.add(it) }
        cancellation.cancel()

        assertEquals(listOf("hello"), results)
    }
}
