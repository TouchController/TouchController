package top.fifthlight.touchcontroller.common.config.preset.info

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class PresetControlInfo(
    val splitControls: Boolean = false,
    val disableTouchGesture: Boolean = false,
    val disableCrosshair: Boolean = true,
    val customConditions: LayerCustomConditions = LayerCustomConditions(),
)
