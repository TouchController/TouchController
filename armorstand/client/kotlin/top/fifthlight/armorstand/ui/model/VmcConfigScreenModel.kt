package top.fifthlight.armorstand.ui.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import top.fifthlight.armorstand.config.ConfigHolder
import top.fifthlight.armorstand.ui.state.VmcConfigScreenState
import top.fifthlight.armorstand.vmc.VmcMarionetteManager

class VmcConfigScreenModel(scope: CoroutineScope) : ViewModel(scope) {
    private val _uiState = MutableStateFlow(VmcConfigScreenState())
    val uiState = _uiState.asStateFlow()

    init {
        scope.apply {
            launch {
                ConfigHolder.config.collect {
                    _uiState.value = _uiState.value.copy(
                        portNumber = it.vmcUdpPort,
                    )
                }
            }
            launch {
                VmcMarionetteManager.state.collect {
                    _uiState.value = _uiState.value.copy(state = it)
                }
            }
        }
    }

    fun startVmcClient() {
        VmcMarionetteManager.start(uiState.value.portNumber)
    }

    fun stopVmcClient() {
        VmcMarionetteManager.stop()
    }
}
