package top.fifthlight.touchcontroller.common.ui.layer.tab.custom.model

import kotlinx.collections.immutable.plus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import top.fifthlight.touchcontroller.common.config.preset.LayoutPreset
import top.fifthlight.touchcontroller.common.config.preset.info.CustomCondition
import top.fifthlight.touchcontroller.common.config.preset.info.LayerCustomConditions
import top.fifthlight.touchcontroller.common.ui.layer.tab.LayerConditionTabContext
import top.fifthlight.touchcontroller.common.ui.model.TouchControllerScreenModel
import top.fifthlight.touchcontroller.common.ui.layer.tab.custom.state.CustomTabState

class CustomTabModel(
    private val context: LayerConditionTabContext,
) : TouchControllerScreenModel() {
    private val _uiState = MutableStateFlow(CustomTabState())
    val uiState = _uiState.asStateFlow()

    fun addCondition(preset: LayoutPreset) {
        context.onCustomConditionsChanged(
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

    fun editCondition(preset: LayoutPreset, index: Int, newCondition: CustomCondition) {
        context.onCustomConditionsChanged(
            LayerCustomConditions(
                preset.controlInfo.customConditions.conditions.set(index, newCondition)
            )
        )
    }

    fun removeCondition(preset: LayoutPreset, index: Int) {
        context.onCustomConditionsChanged(
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
}
