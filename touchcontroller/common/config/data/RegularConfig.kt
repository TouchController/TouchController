package top.fifthlight.touchcontroller.common.config.data

import kotlinx.serialization.Serializable

@Serializable
data class RegularConfig(
    val disableMouseMove: Boolean = true,
    val disableMouseClick: Boolean = true,
    val disableMouseLock: Boolean = false,
    val disableHotBarKey: Boolean = false,
    val vibration: Boolean = true,
    val quickHandSwap: Boolean = false,
)
