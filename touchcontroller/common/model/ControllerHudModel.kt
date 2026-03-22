package top.fifthlight.touchcontroller.common.model

import top.fifthlight.touchcontroller.common.layout.data.ContextResult
import top.fifthlight.touchcontroller.common.layout.data.ContextStatus
import top.fifthlight.touchcontroller.common.layout.data.ContextTimer
import top.fifthlight.touchcontroller.common.layout.queue.DrawQueue

sealed class ControllerHudModel {
    var result = ContextResult()
    val status = ContextStatus()
    val timer = ContextTimer()
    var pendingDrawQueue: DrawQueue? = null

    companion object Global: ControllerHudModel()
}
