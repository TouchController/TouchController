/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.layout.preset.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
import top.fifthlight.touchcontroller.common.config.PresetConfig
import top.fifthlight.touchcontroller.common.config.preset.builtin.key.BuiltinPresetKey

class ManageControlPresetsTabComponent(
    componentContext: ComponentContext,
    val presetConfig: Value<PresetConfig>,
    private val onPresetConfigChanged: (editor: PresetConfig.BuiltIn.() -> PresetConfig.BuiltIn) -> Unit,
    private val onBack: () -> Unit,
    val onOpenCustomLayout: () -> Unit,
) : ComponentContext by componentContext {
    sealed interface ChildConfig {
        data object Preset : ChildConfig

        data class TopBarEditor(val key: BuiltinPresetKey) : ChildConfig
    }

    sealed interface ChildComponent {
        data object Preset : ChildComponent

        data class TopBarEditor(val key: BuiltinPresetKey) : ChildComponent
    }

    private val navigation = StackNavigation<ChildConfig>()

    val childStack: Value<ChildStack<ChildConfig, ChildComponent>> = childStack(
        source = navigation,
        serializer = null,
        initialStack = { listOf(ChildConfig.Preset) },
        handleBackButton = true,
        childFactory = { config, _ ->
            when (config) {
                ChildConfig.Preset -> ChildComponent.Preset
                is ChildConfig.TopBarEditor -> ChildComponent.TopBarEditor(config.key)
            }
        },
    )

    fun openControlPresets() {
        onPresetConfigChanged { this }
    }

    fun updateKey(key: BuiltinPresetKey) {
        onPresetConfigChanged { copy(key = key) }
    }

    fun openTopBarEditor() {
        when (val preset = presetConfig.value) {
            is PresetConfig.BuiltIn -> navigation.pushNew(ChildConfig.TopBarEditor(preset.key))
            is PresetConfig.Custom -> {}
        }
    }

    fun onBack() {
        navigation.pop { isSuccess ->
            if (!isSuccess) {
                onBack.invoke()
            }
        }
    }
}
