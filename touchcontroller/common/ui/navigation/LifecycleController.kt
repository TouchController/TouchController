/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.resume

@Composable
fun LifecycleController(lifecycleRegistry: LifecycleRegistry) {
    DisposableEffect(lifecycleRegistry) {
        // There is no paused state in Minecraft
        lifecycleRegistry.resume()
        onDispose(lifecycleRegistry::destroy)
    }
}
