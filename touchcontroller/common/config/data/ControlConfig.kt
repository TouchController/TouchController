package top.fifthlight.touchcontroller.common.config.data

import kotlinx.serialization.Serializable

@Serializable
data class ControlConfig(
    val viewMovementSensitivity: Float = 495f,
    val viewHoldDetectThreshold: Int = 2,
    val viewHoldDetectTicks: Int = 5,
    val creativeBreakDetectTicks: Int = 10,
)
