/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.state

import com.arkivanov.decompose.Cancellation
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

fun <T : Any> StateFlow<T>.asValue(scope: CoroutineScope): Value<T> = object : Value<T>() {
    override val value: T get() = this@asValue.value

    override fun subscribe(observer: (T) -> Unit): Cancellation {
        val job = scope.launch { collect { observer(it) } }
        return Cancellation { job.cancel() }
    }
}
