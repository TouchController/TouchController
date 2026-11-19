/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.status.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.subscribe
import com.arkivanov.decompose.value.update
import top.fifthlight.touchcontroller.common.config.data.StatusConfig
import top.fifthlight.touchcontroller.common.platform.provider.PlatformProvider
import top.fifthlight.touchcontroller.common.platform.warning.WarningProvider
import top.fifthlight.touchcontroller.common.ui.config.tab.status.state.StateTabState

class StatusTabComponent(
    componentContext: ComponentContext,
    config: Value<StatusConfig>,
    private val onStatusConfigChanged: (StatusConfig) -> Unit,
) : ComponentContext by componentContext {
    private val _uiState = MutableValue(StateTabState(
        config = config.value,
        currentPlatform = PlatformProvider.platform?.name,
        systemInfo = StateTabState.SystemInfo(
            system = PlatformProvider.displayName,
            arch = PlatformProvider.systemArch,
        ),
        warningMessage = if (PlatformProvider.platform == null) {
            WarningProvider.warning
        } else {
            null
        },
    ))
    val uiState: Value<StateTabState> = _uiState

    init {
        config.subscribe(lifecycle) { config ->
            _uiState.update { it.copy(config = config) }
        }
    }

    fun updateConfig(newValue: StatusConfig) {
        onStatusConfigChanged(newValue)
    }

    fun resetConfig() = onStatusConfigChanged(StatusConfig())
}
