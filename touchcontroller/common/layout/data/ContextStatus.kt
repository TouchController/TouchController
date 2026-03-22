package top.fifthlight.touchcontroller.common.layout.data

import top.fifthlight.touchcontroller.common.layout.click.DoubleClickCounter
import top.fifthlight.touchcontroller.common.layout.click.DoubleClickState

data class ContextStatus(
    var dpadLeftForwardShown: Boolean = false,
    var dpadRightForwardShown: Boolean = false,
    var dpadLeftBackwardShown: Boolean = false,
    var dpadRightBackwardShown: Boolean = false,
    var dpadJumping: Boolean = false,
    val cancelFlying: DoubleClickState = DoubleClickState(),
    val sneakLocking: DoubleClickState = DoubleClickState(),
    val sneakTrigger: DoubleClickState = DoubleClickState(),
    var lastCrosshairStatus: CrosshairStatus? = null,
    var vibrate: Boolean = false,
    val quickHandSwap: DoubleClickState = DoubleClickState(
        7
    ),
    var lastDpadDirection: DPadDirection? = null,
    val doubleClickCounter: DoubleClickCounter = DoubleClickCounter(),
    var previousPresetUuid: kotlin.uuid.Uuid? = null,
    val enabledCustomConditions: MutableSet<kotlin.uuid.Uuid> = kotlin.collections.mutableSetOf(),
)
