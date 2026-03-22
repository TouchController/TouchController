package top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.layers.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import top.fifthlight.touchcontroller.common.config.layout.ControllerLayout
import top.fifthlight.touchcontroller.common.config.layout.LayoutLayer
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.layers.state.LayersTabState
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.model.CustomControlLayoutTabModel
import top.fifthlight.touchcontroller.common.ui.model.TouchControllerScreenModel

class LayersTabModel(
    private val screenModel: CustomControlLayoutTabModel,
): TouchControllerScreenModel() {
    private val _uiState = MutableStateFlow<LayersTabState>(LayersTabState.Empty)
    val uiState = _uiState.asStateFlow()

    fun clearState() {
        _uiState.value = LayersTabState.Empty
    }

    fun createLayer(layer: LayoutLayer) {
        screenModel.editPreset {
            copy(layout = ControllerLayout(layout.layers.add(layer)))
        }
        clearState()
    }

    fun openDeleteLayerDialog(index: Int, layer: LayoutLayer) {
        _uiState.value = LayersTabState.Delete(
            index = index,
            layer = layer
        )
    }

    fun copyLayer(layer: LayoutLayer) {
        screenModel.editPreset {
            copy(layout = ControllerLayout(layout.layers.add(layer)))
        }
    }

    fun moveLayer(index: Int, offset: Int) {
        screenModel.editPreset {
            val layer = layout.layers[index]
            val newIndex = (index + offset).coerceIn(layout.layers.indices)
            val newLayers = layout.layers.removeAt(index).add(newIndex, layer)
            copy(layout = ControllerLayout(newLayers))
        }
    }
}
