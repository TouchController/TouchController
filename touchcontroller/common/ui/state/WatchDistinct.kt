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

fun <T : Any> Value<T>.watchDistinct(observer: (newValue: T, oldValue: T?) -> Unit): Cancellation {
    val dispatcher = DistinctDispatcher<T> { newValue, oldValue ->
        observer(newValue, oldValue)
    }
    return subscribe(dispatcher::dispatch)
}

fun <T : Any> Value<T>.watchDistinct(
    lifecycle: Lifecycle,
    mode: ObserveLifecycleMode = ObserveLifecycleMode.START_STOP,
    observer: (newValue: T, oldValue: T?) -> Unit,
) {
    val dispatcher = DistinctDispatcher<T> { newValue, oldValue ->
        observer(newValue, oldValue)
    }
    subscribe(lifecycle, mode, dispatcher::dispatch)
}
