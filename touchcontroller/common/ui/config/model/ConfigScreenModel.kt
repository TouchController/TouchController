/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.model

import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import top.fifthlight.touchcontroller.buildinfo.BuildInfo
import top.fifthlight.touchcontroller.common.config.GlobalConfig
import top.fifthlight.touchcontroller.common.config.holder.GlobalConfigHolder
import top.fifthlight.touchcontroller.common.ui.model.TouchControllerScreenModel
import top.fifthlight.touchcontroller.common.ui.config.state.ConfigScreenState

val LocalConfigScreenModel = compositionLocalOf<ConfigScreenModel> { error("No ConfigScreenModel") }

class ConfigScreenModel : TouchControllerScreenModel() {
    companion object {
        @Suppress("SimplifyBooleanWithConstants", "KotlinConstantConditions")
        private var developmentWarningDialog: Boolean = BuildInfo.MOD_STATE != "release"
    }

    private val _uiState = MutableStateFlow(ConfigScreenState(
        originalConfig = GlobalConfigHolder.config.value,
        developmentWarningDialog = developmentWarningDialog,
    ))
    val uiState = _uiState.asStateFlow()

    init {
        coroutineScope.launch {
            try {
                awaitCancellation()
            } finally {
                saveConfig()
            }
        }
    }

    fun closeDevelopmentDialog() {
        developmentWarningDialog = false
        _uiState.getAndUpdate {
            it.copy(developmentWarningDialog = false)
        }
    }

    fun resetConfig() {
        _uiState.getAndUpdate {
            it.copy(config = GlobalConfig.default)
        }
    }

    fun updateConfig(editor: GlobalConfig.() -> GlobalConfig) {
        _uiState.getAndUpdate {
            it.copy(config = editor(it.config))
        }
    }

    fun saveConfig() {
        val newState = _uiState.updateAndGet {
            it.copy(originalConfig = it.config)
        }
        GlobalConfigHolder.updateConfig { newState.config }
    }

    fun undoConfig() {
        _uiState.getAndUpdate {
            it.copy(config = it.originalConfig)
        }
    }
}
