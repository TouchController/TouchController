package top.fifthlight.touchcontroller.common.config.data

import kotlinx.serialization.Serializable

@Serializable
data class DebugConfig(
    val showPointers: Boolean = false,
    val enableTouchEmulation: Boolean = false,
)
