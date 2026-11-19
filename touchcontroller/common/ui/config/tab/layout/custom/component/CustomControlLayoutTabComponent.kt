/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.children.ChildNavState.Status
import com.arkivanov.decompose.router.pages.*
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.decompose.value.subscribe
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import top.fifthlight.touchcontroller.common.config.PresetConfig
import top.fifthlight.touchcontroller.common.config.preset.LayoutPreset
import top.fifthlight.touchcontroller.common.config.preset.PresetsContainer
import top.fifthlight.touchcontroller.common.config.preset.builtin.key.BuiltinPresetKey
import top.fifthlight.touchcontroller.common.control.ControllerWidget
import top.fifthlight.touchcontroller.common.util.uuid.fastRandomUuid
import kotlin.uuid.Uuid

class CustomControlLayoutTabComponent(
    componentContext: ComponentContext,
    private val presetConfig: Value<PresetConfig>,
    private val onPresetConfigChanged: (editor: PresetConfig.Custom.() -> PresetConfig.Custom) -> Unit,
    private val presets: Value<PresetsContainer>,
    private val onPresetSaved: (Uuid, LayoutPreset, create: Boolean) -> Unit,
    private val onPresetDeleted: (uuid: Uuid) -> Unit,
    private val onPresetOrderChanged: (ImmutableList<Uuid>) -> Unit,
    private val onAddWidgetPreset: (ControllerWidget) -> Unit,
    private val widgetPresets: Value<PersistentList<ControllerWidget>>,
    private val onWidgetPresetsSaved: (PersistentList<ControllerWidget>) -> Unit,
    private val onOpenControlPresets: () -> Unit,
    private val onBack: () -> Unit,
) : ComponentContext by componentContext {
    sealed interface ChildConfig {
        data object Preset : ChildConfig
        data object Custom : ChildConfig
    }

    sealed interface ChildComponent {
        data object Preset : ChildComponent
        data class Custom(val component: CustomControlLayoutScreenComponent) : ChildComponent
    }

    private val navigation = PagesNavigation<ChildConfig>()
    val childPages: Value<ChildPages<ChildConfig, ChildComponent>> = childPages(
        source = navigation,
        serializer = null,
        initialPages = {
            Pages(
                items = listOf(ChildConfig.Preset, ChildConfig.Custom),
                selectedIndex = if (presetConfig.value is PresetConfig.Custom) 1 else 0,
            )
        },
        pageStatus = { index, pages ->
            when (index) {
                pages.selectedIndex -> Status.RESUMED
                else -> Status.DESTROYED
            }
        },
        handleBackButton = false,
        childFactory = { config, context ->
            when (config) {
                ChildConfig.Preset -> ChildComponent.Preset
                ChildConfig.Custom -> ChildComponent.Custom(
                    CustomControlLayoutScreenComponent(
                        componentContext = context,
                        config = this.presetConfig,
                        onPresetConfigChanged = onPresetConfigChanged,
                        presets = presets,
                        onPresetSaved = onPresetSaved,
                        onPresetDeleted = onPresetDeleted,
                        onPresetOrderChanged = onPresetOrderChanged,
                        onBack = onBack,
                        onAddWidgetPreset = onAddWidgetPreset,
                        widgetPresets = widgetPresets,
                        onWidgetPresetsSaved = onWidgetPresetsSaved,
                    )
                )
            }
        }
    )

    init {
        presetConfig.map { it is PresetConfig.Custom }.subscribe(lifecycle) { config ->
            val target = if (config) {
                ChildConfig.Custom
            } else {
                ChildConfig.Preset
            }
            navigation.select(childPages.value.items.indexOfFirst { it.configuration == target })
        }
    }

    fun openCustomLayout() {
        var uuid = (presetConfig.value as? PresetConfig.Custom)?.uuid
            ?: presets.value.orderedEntries.firstOrNull()?.first
        if (uuid == null) {
            uuid = fastRandomUuid()
            val presetKey = (presetConfig.value as? PresetConfig.BuiltIn)?.key
            if (presetKey != null) {
                onPresetSaved(uuid, presetKey.preset, true)
            } else {
                onPresetSaved(uuid, BuiltinPresetKey.DEFAULT.preset, true)
            }
        }
        onPresetConfigChanged {
            copy(uuid = uuid)
        }
    }

    fun openControlPresets() {
        onOpenControlPresets()
    }

    fun onBack() {
        onBack.invoke()
    }
}
