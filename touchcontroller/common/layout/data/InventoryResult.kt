package top.fifthlight.touchcontroller.common.layout.data

import kotlin.collections.contentEquals
import kotlin.collections.contentHashCode
import kotlin.jvm.javaClass

data class InventoryResult(
    val slots: Array<InventorySlotStatus> = Array(9) { InventorySlotStatus() }
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InventoryResult

        return slots.contentEquals(other.slots)
    }

    override fun hashCode(): Int {
        return slots.contentHashCode()
    }
}
