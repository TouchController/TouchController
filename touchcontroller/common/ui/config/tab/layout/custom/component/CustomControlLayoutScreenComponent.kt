/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.children.ChildNavState.Status
import com.arkivanov.decompose.router.pages.*
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.decompose.value.subscribe
import com.arkivanov.decompose.value.updateAndGet
import com.arkivanov.essenty.lifecycle.Lifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import top.fifthlight.touchcontroller.common.config.PresetConfig
import top.fifthlight.touchcontroller.common.config.layout.ControllerLayout
import top.fifthlight.touchcontroller.common.config.layout.LayoutLayer
import top.fifthlight.touchcontroller.common.config.preset.LayoutPreset
import top.fifthlight.touchcontroller.common.config.preset.PresetsContainer
import top.fifthlight.touchcontroller.common.control.ControllerWidget
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.layers.component.LayersTabComponent
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.presets.component.PresetsTabComponent
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.state.CustomControlLayoutTabState
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.state.UndoStack
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.tab.CustomTabComponent
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.tab.CustomTabConfig
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.widgets.component.WidgetsTabComponent
import top.fifthlight.touchcontroller.common.ui.state.subscribeDistinct
import top.fifthlight.touchcontroller.common.ui.state.watchDistinct
import top.fifthlight.touchcontroller.common.util.uuid.fastRandomUuid
import java.util.*
import kotlin.jvm.optionals.getOrNull
import kotlin.uuid.Uuid

class CustomControlLayoutScreenComponent(
    componentContext: ComponentContext,
    config: Value<PresetConfig>,
    private val onPresetConfigChanged: (editor: PresetConfig.Custom.() -> PresetConfig.Custom) -> Unit,
    private val presets: Value<PresetsContainer>,
    private val onPresetSaved: (Uuid, LayoutPreset, create: Boolean) -> Unit,
    private val onPresetDeleted: (uuid: Uuid) -> Unit,
    private val onPresetOrderChanged: (ImmutableList<Uuid>) -> Unit,
    private val onAddWidgetPreset: (ControllerWidget) -> Unit,
    private val widgetPresets: Value<PersistentList<ControllerWidget>>,
    private val onWidgetPresetsSaved: (PersistentList<ControllerWidget>) -> Unit,
    private val onBack: () -> Unit,
) : ComponentContext by componentContext {
    private val _uiState = MutableValue(
        CustomControlLayoutTabState(
            allPresets = presets.value,
        )
    )
    val uiState: Value<CustomControlLayoutTabState> = _uiState

    private val navigation = PagesNavigation<CustomTabConfig>()
    val childPages: Value<ChildPages<CustomTabConfig, CustomTabComponent>> = childPages(
        source = navigation,
        serializer = null,
        initialPages = {
            Pages(
                items = CustomTabConfig.all,
                selectedIndex = 0,
            )
        },
        pageStatus = { index, pages ->
            when (index) {
                pages.selectedIndex -> Status.RESUMED
                else -> Status.CREATED
            }
        },
        handleBackButton = false,
        childFactory = { config, context ->
            when (config) {
                CustomTabConfig.Layers -> CustomTabComponent.Layers(
                    LayersTabComponent(
                        componentContext = context,
                        selectedPreset = uiState.map { Optional.ofNullable(it.selectedPreset) },
                        selectedLayerIndex = uiState.map { it.selectedLayerIndex },
                        onLayerSelected = ::selectLayer,
                        onLayerCreated = ::createLayer,
                        onLayerEdited = ::editLayerAt,
                        onLayerCopied = ::copyLayer,
                        onLayerDeleted = ::deleteLayer,
                        onLayerMoved = ::moveLayer,
                        onCustomConditionsChanged = { conditions ->
                            editPreset {
                                copy(controlInfo = controlInfo.copy(customConditions = conditions))
                            }
                        },
                    )
                )

                CustomTabConfig.Properties -> CustomTabComponent.Properties

                CustomTabConfig.Widgets -> CustomTabComponent.Widgets(
                    WidgetsTabComponent(
                        componentContext = context,
                        widgetPresets = widgetPresets,
                        selectedWidget = uiState.map { Optional.ofNullable(it.selectedWidget) },
                        onWidgetPresetsChanged = onWidgetPresetsSaved,
                        onWidgetAdded = { newWidget(it) },
                        onAddWidgetPreset = onAddWidgetPreset,
                    )
                )

                CustomTabConfig.Presets -> CustomTabComponent.Presets(
                    PresetsTabComponent(
                        componentContext = context,
                        presets = uiState.map { it.allPresets },
                        selectedPreset = uiState.map { Pair(it.selectedPresetUuid, it.selectedPreset) },
                        onPresetSelected = ::selectPreset,
                        onPresetCreated = { preset ->
                            val uuid = fastRandomUuid()
                            onPresetSaved(uuid, preset, true)
                            selectPreset(uuid)
                        },
                        onPresetEdited = { uuid, name, controlInfo ->
                            val state = uiState.value
                            val newPreset = if (state.selectedPresetUuid == uuid) {
                                editPreset {
                                    copy(
                                        name = name,
                                        controlInfo = controlInfo,
                                    )
                                }
                            } else {
                                val preset = state.allPresets[uuid] ?: return@PresetsTabComponent
                                preset.copy(
                                    name = name,
                                    controlInfo = controlInfo,
                                )
                            }
                            newPreset?.let { onPresetSaved(uuid, it, false) }
                        },
                        onPresetDeleted = { uuid ->
                            onPresetDeleted(uuid)
                            updateState {
                                if (selectedPresetUuid == uuid) {
                                    copy(
                                        selectedPresetUuid = null,
                                        undoStack = null,
                                    )
                                } else {
                                    this
                                }
                            }
                        },
                        onPresetOrderChanged = onPresetOrderChanged,
                    )
                )
            }
        }
    )

    init {
        config.map { Optional.ofNullable((it as? PresetConfig.Custom)?.uuid) }.subscribeDistinct(lifecycle) {
            updateState {
                copy(
                    selectedPresetUuid = it.getOrNull(),
                )
            }
        }

        presets.subscribe(lifecycle) { presets ->
            updateState { copy(allPresets = presets) }
        }

        uiState.subscribe(lifecycle) { state ->
            val uuid = state.selectedPresetUuid
            val presetConfig = config.value
            if (presetConfig is PresetConfig.Custom && presetConfig.uuid != uuid) {
                onPresetConfigChanged { copy(uuid = uuid) }
            }
        }

        uiState.map { Pair(it.selectedPresetUuid, it.selectedPreset) }.watchDistinct { (newUuid, _), old ->
            val (oldUuid, oldPreset) = old ?: Pair(null, null)
            if (oldPreset != null && (oldUuid != newUuid)) {
                onPresetSaved(oldUuid!!, oldPreset, false)
            }
        }

        lifecycle.subscribe(object : Lifecycle.Callbacks {
            override fun onDestroy() {
                val state = _uiState.value
                val preset = state.selectedPreset ?: return
                onPresetSaved(state.selectedPresetUuid!!, preset, false)
            }
        })
    }

    private fun updateState(updater: CustomControlLayoutTabState.() -> CustomControlLayoutTabState) =
        _uiState.updateAndGet { original ->
            val new = updater(original)
            val changedPreset = new.selectedPresetUuid != original.selectedPresetUuid
            val newNull = new.selectedPreset != null
            val originalNull = original.selectedPreset != null

            new.copy(
                undoStack = if (changedPreset || (newNull != originalNull)) {
                    new.selectedPreset?.let { UndoStack(it) }
                } else {
                    new.undoStack ?: new.selectedPreset?.let { UndoStack(it) }
                }
            )
        }

    private fun editPreset(
        deselectLayer: Boolean = false,
        deselectWidget: Boolean = false,
        editor: LayoutPreset.() -> LayoutPreset,
    ): LayoutPreset? = updateState {
        val preset = selectedPreset ?: return@updateState this
        val newPreset = editor(preset)
        copy(
            allPresets = PresetsContainer(
                presets = allPresets.put(selectedPresetUuid!!, newPreset),
                order = allPresets.order,
            ),
            selectedLayerIndex = if (deselectLayer) -1 else selectedLayerIndex,
            selectedWidgetIndex = if (deselectWidget) -1 else selectedWidgetIndex,
            undoStack = undoStack?.plus(newPreset) ?: UndoStack(newPreset),
        )
    }.selectedPreset

    private fun editLayer(
        deselectWidget: Boolean = false,
        editor: LayoutLayer.() -> LayoutLayer,
    ) {
        editPreset(deselectWidget = deselectWidget) {
            val uiState = uiState.value
            val selectedPreset = uiState.selectedPreset ?: return@editPreset this
            val selectedLayerIndex = uiState.selectedLayerIndex.takeIf {
                it in selectedPreset.layout.indices
            } ?: return@editPreset this
            copy(
                layout = ControllerLayout(
                    layout.set(selectedLayerIndex, editor(layout[selectedLayerIndex]))
                ),
            )
        }
    }

    fun selectLayer(index: Int) {
        updateState {
            copy(
                selectedLayerIndex = index,
                selectedWidgetIndex = -1,
            )
        }
    }

    fun createLayer(layer: LayoutLayer) {
        editPreset {
            copy(layout = ControllerLayout(layout.layers.add(layer)))
        }
    }

    fun copyLayer(layer: LayoutLayer) {
        editPreset {
            copy(layout = ControllerLayout(layout.layers.add(layer)))
        }
    }

    fun moveLayer(index: Int, offset: Int) {
        val layers = uiState.value.selectedPreset?.layout?.layers ?: return
        if (index !in layers.indices) {
            return
        }
        val newIndex = (index + offset).coerceIn(layers.indices)
        val layer = layers[index]
        editPreset {
            copy(layout = ControllerLayout(layout.layers.removeAt(index).add(newIndex, layer)))
        }
        selectLayer(newIndex)
    }

    fun deleteLayer(index: Int) {
        val layers = uiState.value.selectedPreset?.layout?.layers ?: return
        if (index !in layers.indices) {
            return
        }
        editPreset(deselectLayer = true, deselectWidget = true) {
            copy(layout = ControllerLayout(layout.layers.removeAt(index)))
        }
    }

    fun editLayerAt(index: Int, layer: LayoutLayer) {
        editPreset {
            copy(layout = ControllerLayout(layout.set(index, layer)))
        }
    }

    fun setShowSideBar(showSideBar: Boolean, autoToggle: Boolean = false) {
        updateState {
            copy(
                showSideBar = showSideBar,
                sideBarAutoToggle = autoToggle,
            )
        }
    }

    fun setMoveLocked(moveLocked: Boolean) {
        updateState { copy(moveLocked = moveLocked) }
    }

    fun setHighlight(highlight: Boolean) {
        updateState { copy(highlight = highlight) }
    }

    fun undo() {
        updateState { copy(undoStack = undoStack?.undo()) }
    }

    fun redo() {
        updateState { copy(undoStack = undoStack?.redo()) }
    }

    fun addWidgetPreset(widget: ControllerWidget) {
        onAddWidgetPreset(widget)
    }

    fun newWidget(widget: ControllerWidget): Int {
        val newWidget = widget.newId()
        var index = 0
        editLayer(deselectWidget = true) { copy(widgets = widgets.add(newWidget)).also { index = it.widgets.size - 1 } }
        updateState { copy(selectedWidgetIndex = index) }
        return index
    }

    fun deleteWidget(index: Int) {
        editLayer(deselectWidget = true) { copy(widgets = widgets.removeAt(index)) }
    }

    fun selectWidget(index: Int) {
        updateState { copy(selectedWidgetIndex = index) }
    }

    fun editWidget(index: Int, widget: ControllerWidget) {
        editLayer { copy(widgets = widgets.set(index, widget)) }
    }

    fun copyWidget(widget: ControllerWidget) {
        updateState { copy(copiedWidget = widget) }
    }

    fun selectPreset(uuid: Uuid) {
        updateState {
            if (uuid == selectedPresetUuid) {
                return@updateState this
            }
            copy(
                selectedPresetUuid = uuid,
                selectedLayerIndex = 0,
                selectedWidgetIndex = -1,
                undoStack = null,
            )
        }
    }

    fun selectTab(config: CustomTabConfig) {
        setShowSideBar(true)
        navigation.select(childPages.value.items.indexOfFirst { it.configuration == config })
    }

    fun onBack() {
        onBack.invoke()
    }
}
