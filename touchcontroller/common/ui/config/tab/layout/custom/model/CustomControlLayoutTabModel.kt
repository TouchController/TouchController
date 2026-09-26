/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.model

import kotlinx.collections.immutable.plus
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import top.fifthlight.touchcontroller.common.config.PresetConfig
import top.fifthlight.touchcontroller.common.config.layout.ControllerLayout
import top.fifthlight.touchcontroller.common.config.layout.LayoutLayer
import top.fifthlight.touchcontroller.common.config.preset.LayoutPreset
import top.fifthlight.touchcontroller.common.config.preset.builtin.key.BuiltinPresetKey
import top.fifthlight.touchcontroller.common.config.preset.manager.PresetManager
import top.fifthlight.touchcontroller.common.config.widget.WidgetPresetManager
import top.fifthlight.touchcontroller.common.control.ControllerWidget
import top.fifthlight.touchcontroller.common.ui.config.model.ConfigScreenModel
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.state.CustomControlLayoutTabState
import top.fifthlight.touchcontroller.common.ui.model.TouchControllerScreenModel
import top.fifthlight.touchcontroller.common.util.state.combineStates
import top.fifthlight.touchcontroller.common.util.uuid.fastRandomUuid
import kotlin.uuid.Uuid

class CustomControlLayoutTabModel(
    private val configScreenModel: ConfigScreenModel,
) : TouchControllerScreenModel() {
    private val pageState = MutableStateFlow(CustomControlLayoutTabState.Enabled.PageState())
    val uiState =
        combineStates(configScreenModel.uiState, PresetManager.presets, pageState) { uiState, presets, selectState ->
            val config = uiState.config
            when (val preset = config.preset) {
                is PresetConfig.BuiltIn -> CustomControlLayoutTabState.Disabled
                is PresetConfig.Custom -> {
                    val selectedPreset = selectState.editState?.undoStack?.currentItem
                    val selectedLayer = selectedPreset?.layout?.getOrNull(selectState.selectedLayerIndex)
                    val selectedWidget = selectedLayer?.widgets?.getOrNull(selectState.selectedWidgetIndex)
                    CustomControlLayoutTabState.Enabled(
                        allPresets = presets,
                        selectedPresetUuid = preset.uuid,
                        selectedPreset = selectedPreset,
                        selectedLayer = selectedLayer,
                        selectedWidget = selectedWidget,
                        pageState = selectState,
                    )
                }
            }
        }

    private fun CustomControlLayoutTabState.Enabled.EditState.save() {
        PresetManager.savePreset(presetUuid, undoStack.currentItem, create = false)
    }

    init {
        coroutineScope.launch {
            configScreenModel.uiState.collectLatest { uiState ->
                val config = uiState.config
                val presetConfig = (config.preset as? PresetConfig.Custom) ?: return@collectLatest
                val selectedPresetUuid = presetConfig.uuid
                pageState.getAndUpdate { pageState ->
                    val editState = pageState.editState
                    if (selectedPresetUuid == null) {
                        editState?.save()
                        pageState.copy(editState = null)
                    } else if (editState != null) {
                        if (editState.presetUuid != selectedPresetUuid) {
                            editState.save()
                            val selectedPreset =
                                PresetManager.presets.map { it[selectedPresetUuid] }.first { it != null }!!
                            pageState.copy(
                                editState = CustomControlLayoutTabState.Enabled.EditState(
                                    presetUuid = selectedPresetUuid,
                                    undoStack = CustomControlLayoutTabState.Enabled.UndoStack(selectedPreset),
                                )
                            )
                        } else {
                            pageState
                        }
                    } else {
                        val selectedPreset = PresetManager.presets.map { it[selectedPresetUuid] }.first { it != null }!!
                        pageState.copy(
                            editState = CustomControlLayoutTabState.Enabled.EditState(
                                presetUuid = selectedPresetUuid,
                                undoStack = CustomControlLayoutTabState.Enabled.UndoStack(selectedPreset),
                            )
                        )
                    }
                }
            }
        }
        coroutineScope.launch {
            try {
                awaitCancellation()
            } finally {
                pageState.value.editState?.let { editState ->
                    PresetManager.savePreset(
                        uuid = editState.presetUuid,
                        editState.undoStack.currentItem,
                    )
                }
            }
        }
    }

    fun enableCustomLayout() {
        val prevPresetKey = when (val config = configScreenModel.uiState.value.config.preset) {
            is PresetConfig.BuiltIn -> config.key
            is PresetConfig.Custom -> return
        }
        val firstPreset = PresetManager.presets.value.orderedEntries.firstOrNull()
        if (firstPreset == null) {
            val newPreset = prevPresetKey.preset
            val newPresetUuid = Uuid.random()
            PresetManager.savePreset(newPresetUuid, newPreset)
            configScreenModel.updateConfig {
                copy(preset = PresetConfig.Custom(uuid = newPresetUuid))
            }
        } else {
            configScreenModel.updateConfig {
                copy(preset = PresetConfig.Custom(firstPreset.first))
            }
        }
    }

    fun setShowSideBar(showSideBar: Boolean, autoToggle: Boolean = false) {
        pageState.getAndUpdate {
            it.copy(
                showSideBar = showSideBar,
                sideBarAutoToggle = autoToggle,
            )
        }
    }

    fun setMoveLocked(moveLocked: Boolean) {
        pageState.getAndUpdate { it.copy(moveLocked = moveLocked) }
    }

    fun setHighlight(highlight: Boolean) {
        pageState.getAndUpdate { it.copy(highlight = highlight) }
    }

    fun undo() {
        pageState.getAndUpdate { pageState ->
            pageState.editState?.let { editState ->
                pageState.copy(
                    editState = editState.copy(
                        undoStack = editState.undoStack.undo(),
                    )
                )
            } ?: pageState
        }
    }

    fun redo() {
        pageState.getAndUpdate { pageState ->
            pageState.editState?.let { editState ->
                pageState.copy(
                    editState = editState.copy(
                        undoStack = editState.undoStack.redo(),
                    )
                )
            } ?: pageState
        }
    }

    fun editPreset(saveUndoStack: Boolean = true, editor: LayoutPreset.() -> LayoutPreset) {
        val uiState = uiState.value as? CustomControlLayoutTabState.Enabled ?: return
        val preset = uiState.selectedPreset ?: return
        val newPreset = editor(preset)
        pageState.getAndUpdate { pageState ->
            pageState.editState?.let { editState ->
                if (saveUndoStack) {
                    pageState.copy(
                        editState = editState.copy(
                            undoStack = editState.undoStack + newPreset,
                        )
                    )
                } else {
                    pageState.copy(
                        editState = editState.copy(
                            undoStack = CustomControlLayoutTabState.Enabled.UndoStack(newPreset),
                        )
                    )
                }
            } ?: pageState
        }
    }

    fun selectPreset(uuid: Uuid) {
        pageState.getAndUpdate {
            it.copy(
                selectedLayerIndex = 0,
                selectedWidgetIndex = -1
            )
        }
        configScreenModel.updateConfig {
            copy(preset = PresetConfig.Custom(uuid))
        }
    }

    fun newPreset(preset: LayoutPreset? = null) {
        pageState.getAndUpdate {
            it.copy(
                selectedLayerIndex = 0,
                selectedWidgetIndex = -1
            )
        }
        val uuid = fastRandomUuid()
        val preset = preset ?: BuiltinPresetKey.DEFAULT.preset.copy(
            name = "New preset"
        )
        PresetManager.savePreset(uuid, preset)
        configScreenModel.updateConfig {
            copy(preset = PresetConfig.Custom(uuid = uuid))
        }
    }

    fun savePreset() {
        val editState = pageState.value.editState ?: return
        editState.save()
    }

    fun deletePreset(uuid: Uuid) {
        PresetManager.removePreset(uuid)
        pageState.getAndUpdate {
            it.copy(
                selectedLayerIndex = 0,
                selectedWidgetIndex = -1
            )
        }
        configScreenModel.updateConfig {
            val preset = preset
            if (preset is PresetConfig.Custom && preset.uuid == uuid) {
                copy(preset = PresetConfig.Custom())
            } else {
                this
            }
        }
    }

    fun copyWidget(widget: ControllerWidget) {
        pageState.getAndUpdate { it.copy(copiedWidget = widget) }
    }

    fun selectWidget(index: Int) {
        pageState.getAndUpdate { it.copy(selectedWidgetIndex = index) }
    }

    fun editLayer(action: LayoutLayer.() -> LayoutLayer) {
        val uiState = uiState.value as? CustomControlLayoutTabState.Enabled ?: return
        val selectedPreset = uiState.selectedPreset ?: return
        val selectedLayerIndex =
            uiState.pageState.selectedLayerIndex.takeIf { it in selectedPreset.layout.indices } ?: return
        editPreset {
            copy(
                layout = ControllerLayout(
                    layout.set(selectedLayerIndex, action(layout[selectedLayerIndex]))
                )
            )
        }
    }

    fun deleteLayer(index: Int) {
        pageState.getAndUpdate {
            it.copy(
                selectedWidgetIndex = -1,
                selectedLayerIndex = -1,
            )
        }
        editPreset {
            copy(layout = ControllerLayout(layout.removeAt(index)))
        }
    }

    fun selectLayer(index: Int) {
        pageState.getAndUpdate {
            it.copy(
                selectedWidgetIndex = -1,
                selectedLayerIndex = index,
            )
        }
    }

    fun editWidget(index: Int, widget: ControllerWidget) {
        editLayer { copy(widgets = widgets.set(index, widget)) }
    }

    fun newWidget(widget: ControllerWidget): Int {
        val newWidget = widget.newId()
        var index = 0
        editLayer { copy(widgets = widgets.add(newWidget)).also { index = it.widgets.size - 1 } }
        return index
    }

    fun deleteWidget(index: Int) {
        pageState.getAndUpdate {
            it.copy(
                selectedWidgetIndex = -1
            )
        }
        editLayer { copy(widgets = widgets.removeAt(index)) }
    }

    fun addWidgetPreset(widget: ControllerWidget) {
        WidgetPresetManager.save(WidgetPresetManager.presets.value + widget)
    }
}
