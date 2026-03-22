package top.fifthlight.touchcontroller.common.ui.config.tab.layout.preset.model

import top.fifthlight.touchcontroller.common.config.PresetConfig
import top.fifthlight.touchcontroller.common.config.preset.builtin.key.BuiltinPresetKey
import top.fifthlight.touchcontroller.common.ext.mapState
import top.fifthlight.touchcontroller.common.ui.config.model.ConfigScreenModel
import top.fifthlight.touchcontroller.common.ui.model.TouchControllerScreenModel

class ManageControlPresetsTabModel(
    private val configScreenModel: ConfigScreenModel,
) : TouchControllerScreenModel() {
    val presetConfig = configScreenModel.uiState.mapState {
        when (val preset = it.config.preset) {
            is PresetConfig.BuiltIn -> preset
            is PresetConfig.Custom -> null
        }
    }

    fun update(config: PresetConfig.BuiltIn) {
        configScreenModel.updateConfig {
            copy(preset = config)
        }
    }

    fun updateKey(key: BuiltinPresetKey) {
        configScreenModel.updateConfig {
            when (val preset = preset) {
                is PresetConfig.BuiltIn -> {
                    copy(preset = preset.copy(key = key))
                }

                else -> this
            }
        }
    }
}
