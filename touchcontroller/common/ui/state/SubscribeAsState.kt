/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.state

import androidx.compose.runtime.*
import com.arkivanov.decompose.value.Value

// Code from https://github.com/arkivanov/Decompose/blob/c9780062b59dc7af94837d6ca8239f61cf428d23/extensions-compose/src/commonMain/kotlin/com/arkivanov/decompose/extensions/compose/SubscribeAsState.kt
// Vendor this method to avoid dependencies on decompose-compose, which will drag compose.foundation
// Original copyright: arkivanov Apache-2.0

/**
 * Subscribes to the [Value] and returns Compose [State].
 *
 * @param policy a [SnapshotMutationPolicy] that will be used by Compose when comparing values.
 * Default is [structuralEqualityPolicy].
 */
@Composable
fun <T : Any> Value<T>.subscribeAsState(policy: SnapshotMutationPolicy<T> = structuralEqualityPolicy()): State<T> {
    val state = remember(this, policy) { mutableStateOf(value, policy) }

    DisposableEffect(this) {
        val disposable = subscribe { state.value = it }
        onDispose { disposable.cancel() }
    }

    return state
}
