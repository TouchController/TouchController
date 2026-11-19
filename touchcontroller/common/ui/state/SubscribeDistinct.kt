/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.state

import com.arkivanov.decompose.Cancellation
import com.arkivanov.decompose.value.ObserveLifecycleMode
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.subscribe
import com.arkivanov.essenty.lifecycle.Lifecycle

fun <T : Any> Value<T>.subscribeDistinct(observer: (T) -> Unit): Cancellation {
    val dispatcher = DistinctDispatcher<T> { value, _ -> observer(value) }
    return subscribe(dispatcher::dispatch)
}

fun <T : Any> Value<T>.subscribeDistinct(
    lifecycle: Lifecycle,
    mode: ObserveLifecycleMode = ObserveLifecycleMode.START_STOP,
    observer: (T) -> Unit,
) {
    val dispatcher = DistinctDispatcher<T> { value, _ -> observer(value) }
    subscribe(lifecycle, mode, dispatcher::dispatch)
}
