/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.*
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.decompose.value.update
import com.arkivanov.decompose.value.updateAndGet
import com.arkivanov.essenty.lifecycle.subscribe
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import top.fifthlight.touchcontroller.common.config.GlobalConfig
import top.fifthlight.touchcontroller.common.config.PresetConfig
import top.fifthlight.touchcontroller.common.config.preset.LayoutPreset
import top.fifthlight.touchcontroller.common.config.preset.PresetsContainer
import top.fifthlight.touchcontroller.common.control.ControllerWidget
import top.fifthlight.touchcontroller.common.gal.itemlist.DefaultItemListProvider
import top.fifthlight.touchcontroller.common.ui.config.state.ConfigScreenState
import top.fifthlight.touchcontroller.common.ui.config.tab.TabComponent
import top.fifthlight.touchcontroller.common.ui.config.tab.TabConfig
import top.fifthlight.touchcontroller.common.ui.config.tab.about.component.AboutTabComponent
import top.fifthlight.touchcontroller.common.ui.config.tab.item.component.ItemTabComponent
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.component.CustomControlLayoutTabComponent
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.preset.component.ManageControlPresetsTabComponent
import top.fifthlight.touchcontroller.common.ui.config.tab.status.component.StatusTabComponent
import kotlin.uuid.Uuid

class ConfigComponent(
    componentContext: ComponentContext,
    initialConfig: GlobalConfig,
    private val onConfigUpdated: (GlobalConfig) -> Unit,
    private val presets: Value<PresetsContainer>,
    private val onPresetSaved: (Uuid, LayoutPreset, create: Boolean) -> Unit,
    private val onPresetDeleted: (uuid: Uuid) -> Unit,
    private val onPresetOrderChanged: (ImmutableList<Uuid>) -> Unit,
    private val onAddWidgetPreset: (ControllerWidget) -> Unit,
    private val widgetPresets: Value<PersistentList<ControllerWidget>>,
    private val onWidgetPresetsSaved: (PersistentList<ControllerWidget>) -> Unit,
    private val developmentWarningDialog: Boolean = false,
    private val onDevelopmentWarningDialogClosed: () -> Unit,
    private val onBack: () -> Unit,
) : ComponentContext by componentContext {
    init {
        lifecycle.subscribe(onDestroy = {
            saveConfig()
        })
    }

    sealed interface ChildConfig {
        data object DevelopmentWarning : ChildConfig
        data object Reset : ChildConfig
    }

    sealed interface ChildComponent {
        data object DevelopmentWarning : ChildComponent
        data object Reset : ChildComponent
    }

    private val _uiState = MutableValue(ConfigScreenState(originalConfig = initialConfig))

    private val dialogNavigation = SlotNavigation<ChildConfig>()

    val dialog: Value<ChildSlot<ChildConfig, ChildComponent>> = childSlot(
        source = dialogNavigation,
        serializer = null,
        handleBackButton = true,
        initialConfiguration = { if (developmentWarningDialog) ChildConfig.DevelopmentWarning else null },
        childFactory = { config, _ ->
            when (config) {
                ChildConfig.DevelopmentWarning -> ChildComponent.DevelopmentWarning
                ChildConfig.Reset -> ChildComponent.Reset
            }
        },
    )

    private val navigation = TabsNavigation<TabConfig>()

    val childTabs: Value<ChildTabs<TabConfig, TabComponent>> = childTabs(
        source = navigation,
        initialTabs = {
            val tabs = TabConfig.all.filter { !it.openAsScreen }
            Tabs(
                items = tabs,
                selectedIndex = tabs.indexOf(TabConfig.Regular),
            )
        },
        childFactory = { config, context ->
            when (config) {
                TabConfig.About -> TabComponent.About(
                    AboutTabComponent(
                        context,
                    )
                )

                TabConfig.Status -> TabComponent.Status(
                    StatusTabComponent(
                        componentContext = context,
                        config = _uiState.map { it.config.status },
                        onStatusConfigChanged = { status ->
                            updateConfig { copy(status = status) }
                        },
                    )
                )

                TabConfig.ManageControlPresets -> TabComponent.ManageControlPresets(
                    ManageControlPresetsTabComponent(
                        componentContext = context,
                        presetConfig = _uiState.map { it.config.preset },
                        onPresetConfigChanged = { editor ->
                            updateConfig {
                                copy(preset = editor(preset as? PresetConfig.BuiltIn ?: PresetConfig.BuiltIn()))
                            }
                        },
                        onBack = ::onBack,
                        onOpenCustomLayout = {
                            navigation.select(TabConfig.CustomControlLayout)
                        },
                    )
                )

                TabConfig.CustomControlLayout -> TabComponent.CustomControlLayout(
                    CustomControlLayoutTabComponent(
                        componentContext = context,
                        presetConfig = _uiState.map { it.config.preset },
                        onPresetConfigChanged = { editor ->
                            updateConfig {
                                copy(preset = editor(preset as? PresetConfig.Custom ?: PresetConfig.Custom()))
                            }
                        },
                        presets = presets,
                        onPresetSaved = onPresetSaved,
                        onPresetDeleted = onPresetDeleted,
                        onPresetOrderChanged = onPresetOrderChanged,
                        onAddWidgetPreset = onAddWidgetPreset,
                        widgetPresets = widgetPresets,
                        onWidgetPresetsSaved = onWidgetPresetsSaved,
                        onOpenControlPresets = {
                            navigation.select(TabConfig.ManageControlPresets)
                        },
                        onBack = ::onBack,
                    )
                )

                TabConfig.Sdl -> TabComponent.Sdl(
                    config = _uiState.map { it.config.platform.sdl },
                    onConfigChanged = { sdl ->
                        updateConfig { copy(platform = platform.copy(sdl = sdl)) }
                    },
                )

                TabConfig.Regular -> TabComponent.Regular(
                    config = _uiState.map { it.config.regular },
                    onConfigChanged = { regular ->
                        updateConfig { copy(regular = regular) }
                    },
                )

                TabConfig.Control -> TabComponent.Control(
                    config = _uiState.map { it.config.control },
                    onConfigChanged = { control ->
                        updateConfig { copy(control = control) }
                    },
                )

                TabConfig.TouchRing -> TabComponent.TouchRing(
                    config = _uiState.map { it.config.touchRing },
                    onConfigChanged = { touchRing ->
                        updateConfig { copy(touchRing = touchRing) }
                    },
                )

                TabConfig.Debug -> TabComponent.Debug(
                    config = _uiState.map { it.config.debug },
                    onConfigChanged = { debug ->
                        updateConfig { copy(debug = debug) }
                    },
                )

                TabConfig.UsableItemsTab -> TabComponent.ItemList(
                    ItemTabComponent(
                        componentContext = context,
                        itemList = _uiState.map { it.config.item.usableItems },
                        onItemListChanged = { list ->
                            updateConfig {
                                copy(item = item.copy(usableItems = list))
                            }
                        },
                        defaultItemListGetter = DefaultItemListProvider::usableItems,
                    )
                )

                TabConfig.ShowCrosshairItemsTab -> TabComponent.ItemList(
                    ItemTabComponent(
                        componentContext = context,
                        itemList = _uiState.map { it.config.item.showCrosshairItems },
                        onItemListChanged = { list ->
                            updateConfig {
                                copy(item = item.copy(showCrosshairItems = list))
                            }
                        },
                        defaultItemListGetter = DefaultItemListProvider::showCrosshairItems,
                    )
                )

                TabConfig.UsingAimingItemsTab -> TabComponent.ItemList(
                    ItemTabComponent(
                        componentContext = context,
                        itemList = _uiState.map { it.config.item.usingAimingItems },
                        onItemListChanged = { list ->
                            updateConfig {
                                copy(item = item.copy(usingAimingItems = list))
                            }
                        },
                        defaultItemListGetter = DefaultItemListProvider::usingAimingItems,
                    )
                )
            }
        },
    )

    fun selectTab(tab: TabConfig) {
        navigation.select(tab)
    }

    fun closeDevelopmentDialog() {
        dialogNavigation.dismiss()
        onDevelopmentWarningDialogClosed()
    }

    fun openResetDialog() {
        dialogNavigation.activate(ChildConfig.Reset)
    }

    fun closeResetDialog() {
        dialogNavigation.dismiss()
    }

    fun resetConfig() {
        _uiState.update {
            it.copy(config = GlobalConfig.default)
        }
    }

    fun resetLayoutSettings() {
        updateConfig { copy(preset = PresetConfig.BuiltIn()) }
    }

    fun updateConfig(editor: GlobalConfig.() -> GlobalConfig) {
        _uiState.update {
            it.copy(config = editor(it.config))
        }
    }

    fun saveConfig() {
        val newState = _uiState.updateAndGet {
            it.copy(originalConfig = it.config)
        }
        onConfigUpdated(newState.config)
    }

    fun undoConfig() {
        _uiState.update {
            it.copy(config = it.originalConfig)
        }
    }

    fun onBack() {
        if (childTabs.value.screen != null) {
            navigation.pop()
        } else {
            onBack.invoke()
        }
    }
}
