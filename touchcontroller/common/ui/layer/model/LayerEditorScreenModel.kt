package top.fifthlight.touchcontroller.common.ui.layer.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import top.fifthlight.touchcontroller.common.config.condition.LayerConditions
import top.fifthlight.touchcontroller.common.config.layout.LayoutLayer
import top.fifthlight.touchcontroller.common.ui.layer.state.LayerEditorScreenState
import top.fifthlight.touchcontroller.common.ui.model.TouchControllerScreenModel

class LayerEditorScreenModel(
    private val initialValue: LayoutLayer,
    private val onValueChanged: (LayoutLayer) -> Unit,
) : TouchControllerScreenModel() {
    private val _uiState = MutableStateFlow(LayerEditorScreenState(initialValue))
    val uiState = _uiState.asStateFlow()

    fun editName(newName: String) {
        _uiState.getAndUpdate {
            it.copy(name = newName)
        }
    }

    fun applyChanges() {
        onValueChanged(
            uiState.value.edit(initialValue),
        )
    }

    fun editCondition(index: Int, editor: LayerConditions.Item.() -> LayerConditions.Item) {
        _uiState.getAndUpdate {
            it.copy(
                conditions = LayerConditions(
                    conditions = it.conditions.conditions.set(index, editor(it.conditions.conditions[index])),
                ),
            )
        }
    }

    fun removeCondition(index: Int) {
        _uiState.getAndUpdate {
            it.copy(
                conditions = LayerConditions(
                    conditions = it.conditions.conditions.removeAt(index),
                ),
            )
        }
    }

    fun addCondition(condition: LayerConditions.Item) {
        _uiState.getAndUpdate {
            it.copy(
                conditions = LayerConditions(
                    conditions = it.conditions.conditions.add(condition),
                ),
            )
        }
    }
}
