/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.state

import com.arkivanov.decompose.Cancellation
import com.arkivanov.decompose.value.Value

data class ImmutableValue<out T: Any>(override val value: T): Value<T>() {
    private object NoopCancellation: Cancellation {
        override fun cancel() = Unit
    }

    override fun subscribe(observer: (T) -> Unit): Cancellation {
        observer(value)
        return NoopCancellation
    }
}
