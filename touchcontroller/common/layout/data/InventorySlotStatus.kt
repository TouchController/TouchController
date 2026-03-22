package top.fifthlight.touchcontroller.common.layout.data

data class InventorySlotStatus(
    var progress: Float = 0f,
    var drop: Boolean = false,
    var select: Boolean = false,
)
