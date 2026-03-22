package top.fifthlight.touchcontroller.common.config.data

import kotlinx.serialization.Serializable

@Serializable
data class TouchRingConfig(
    val radius: Int = 36,
    val outerRadius: Int = 2,
    val initialProgress: Float = .5f
)
