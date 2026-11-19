/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.layer.tab.custom.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.collections.immutable.plus
import top.fifthlight.touchcontroller.common.config.condition.CustomLayerConditionKey
import top.fifthlight.touchcontroller.common.config.condition.LayerConditions
import top.fifthlight.touchcontroller.common.config.preset.LayoutPreset
import top.fifthlight.touchcontroller.common.config.preset.info.CustomCondition
import top.fifthlight.touchcontroller.common.config.preset.info.LayerCustomConditions
import top.fifthlight.touchcontroller.common.ui.layer.tab.custom.state.CustomTabState
import java.util.*
import kotlin.jvm.optionals.getOrNull

class CustomTabComponent(
    componentContext: ComponentContext,
    val preset: Value<Optional<LayoutPreset>>,
    private val onCustomConditionsChanged: (LayerCustomConditions) -> Unit,
    private val onConditionAdded: (LayerConditions.Key) -> Unit,
) : ComponentContext by componentContext {
    private val _uiState = MutableValue(CustomTabState())
    val uiState: Value<CustomTabState> = _uiState

    fun addCondition() {
        val preset = preset.value.getOrNull() ?: return
        onCustomConditionsChanged(
            LayerCustomConditions(
                preset.controlInfo.customConditions.conditions + CustomCondition()
            )
        )
    }

    fun openEditConditionDialog(index: Int, condition: CustomCondition) {
        _uiState.value = _uiState.value.copy(
            editState = CustomTabState.EditState(
                index = index,
                name = condition.name,
            )
        )
    }

    fun closeEditConditionDialog() {
        _uiState.value = _uiState.value.copy(editState = null)
    }

    fun editCondition(index: Int, newCondition: CustomCondition) {
        val preset = preset.value.getOrNull() ?: return
        onCustomConditionsChanged(
            LayerCustomConditions(
                preset.controlInfo.customConditions.conditions.set(index, newCondition)
            )
        )
    }

    fun removeCondition(index: Int) {
        val preset = preset.value.getOrNull() ?: return
        onCustomConditionsChanged(
            LayerCustomConditions(
                preset.controlInfo.customConditions.conditions.removeAt(index)
            )
        )
    }

    fun updateEditState(editor: CustomTabState.EditState.() -> CustomTabState.EditState) {
        _uiState.value = _uiState.value.copy(
            editState = _uiState.value.editState?.editor()
        )
    }

    fun selectCondition(condition: CustomCondition) {
        onConditionAdded(CustomLayerConditionKey(condition.uuid))
    }
}
