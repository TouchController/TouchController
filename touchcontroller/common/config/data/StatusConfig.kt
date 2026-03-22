package top.fifthlight.touchcontroller.common.config.data

import kotlinx.serialization.Serializable

@Serializable
data class StatusConfig(
    val status: Status = Status.ENABLED,
) {
    enum class Status {
        DISABLED,
        ONLY_VIEW_CLICK_TO_INTERACT,
        ONLY_VIEW_AIMING_BY_CROSSHAIR,
        ENABLED,
    }
}
