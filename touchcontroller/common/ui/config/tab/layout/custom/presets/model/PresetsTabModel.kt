/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.presets.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import org.slf4j.LoggerFactory
import top.fifthlight.touchcontroller.common.config.preset.LayoutPreset
import top.fifthlight.touchcontroller.common.config.preset.manager.PresetManager
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.model.CustomControlLayoutTabModel
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.presets.state.PresetsTabState
import top.fifthlight.touchcontroller.common.ui.model.TouchControllerScreenModel
import kotlin.uuid.Uuid

class PresetsTabModel(
    private val screenModel: CustomControlLayoutTabModel,
) : TouchControllerScreenModel() {
    private companion object {
        private val logger = LoggerFactory.getLogger(PresetsTabModel::class.java)
    }

    private val _uiState = MutableStateFlow<PresetsTabState>(PresetsTabState.Empty)
    val uiState = _uiState.asStateFlow()

    fun clearState() {
        _uiState.value = PresetsTabState.Empty
    }

    fun openCreatePresetChooseDialog() {
        _uiState.value = PresetsTabState.CreateChoose
    }


    fun openCreateEmptyPresetDialog() {
        _uiState.value = PresetsTabState.CreateEmpty()
    }

    fun updateCreatePresetState(editor: PresetsTabState.CreateEmpty.() -> PresetsTabState.CreateEmpty) {
        _uiState.getAndUpdate {
            if (it is PresetsTabState.CreateEmpty) {
                editor(it)
            } else {
                it
            }
        }
    }

    fun createPreset(state: PresetsTabState.CreateEmpty) {
        val preset = state.toPreset()
        screenModel.newPreset(preset)
        clearState()
    }

    fun openEditPresetDialog(uuid: Uuid, preset: LayoutPreset) {
        _uiState.value = PresetsTabState.Edit(
            uuid = uuid,
            name = preset.name,
            controlInfo = preset.controlInfo,
        )
    }

    fun updateEditPresetState(editor: PresetsTabState.Edit.() -> PresetsTabState.Edit) {
        _uiState.getAndUpdate {
            if (it is PresetsTabState.Edit) {
                editor(it)
            } else {
                it
            }
        }
    }

    fun editPreset(state: PresetsTabState.Edit) {
        val preset = PresetManager.presets.value[state.uuid] ?: return
        PresetManager.savePreset(state.uuid, state.edit(preset), false)
        clearState()
    }

    fun openDeletePresetBox(uuid: Uuid) {
        _uiState.value = PresetsTabState.Delete(uuid)
    }

    fun movePreset(uuid: Uuid, offset: Int) {
        PresetManager.movePreset(uuid, offset)
    }

    fun openPresetPathDialog(uuid: Uuid) {
        val path = try {
            PresetManager.presetDir.resolve("$uuid.json").toAbsolutePath()
        } catch (ex: Exception) {
            logger.error("Failed to get preset path", ex)
            null
        }
        _uiState.value = PresetsTabState.Path(path?.toString())
    }
}
