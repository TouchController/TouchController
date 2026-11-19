/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.backhandler.BackDispatcher
import top.fifthlight.combine.core.screen.DismissHandler
import top.fifthlight.combine.core.screen.LocalCloseHandler

@Composable
fun BackController(
    backDispatcher: BackDispatcher,
) {
    val closeHandler = LocalCloseHandler.current
    DisposableEffect(backDispatcher, closeHandler) {
        val backCallback = object : BackCallback(
            priority = PRIORITY_MIN,
        ) {
            override fun onBack() {
                closeHandler.close()
            }
        }
        backDispatcher.register(backCallback)
        onDispose {
            backDispatcher.unregister(backCallback)
        }
    }

    DismissHandler {
        backDispatcher.back()
    }
}
