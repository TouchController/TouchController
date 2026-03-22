package top.fifthlight.touchcontroller.common.layout.data

import top.fifthlight.data.Offset
import top.fifthlight.touchcontroller.common.gal.player.PlayerHandle

data class ContextResult(
    var forward: Float = 0f,
    var left: Float = 0f,
    var lookDirection: Offset? = null,
    var crosshairStatus: CrosshairStatus? = null,
    val inventory: InventoryResult = InventoryResult(),
    var boatLeft: Boolean = false,
    var boatRight: Boolean = false,
    var showBlockOutline: Boolean = false,
    val pendingAction: MutableList<(ContextTimer, PlayerHandle) -> Unit> = mutableListOf(),
)
