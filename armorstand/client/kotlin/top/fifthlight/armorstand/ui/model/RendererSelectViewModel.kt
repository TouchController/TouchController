package top.fifthlight.armorstand.ui.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import top.fifthlight.armorstand.config.ConfigHolder
import top.fifthlight.armorstand.config.GlobalConfig
import top.fifthlight.armorstand.ui.state.RendererSelectScreenState

class RendererSelectViewModel(scope: CoroutineScope) : ViewModel(scope) {
    private val _uiState = MutableStateFlow(
        RendererSelectScreenState(
            currentRenderer = ConfigHolder.config.value.renderer,
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        scope.launch {
            ConfigHolder.config.collect {
                _uiState.value = RendererSelectScreenState(
                    currentRenderer = it.renderer,
                )
            }
        }
    }

    fun setCurrentRenderer(key: GlobalConfig.RendererKey) {
        ConfigHolder.update {
            copy(renderer = key)
        }
    }
}
