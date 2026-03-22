package top.fifthlight.combine.input.pointer

@JvmInline
value class PointerIcon private constructor(internal val value: Int) {
    companion object {
        val Arrow = PointerIcon(0)
        val Edit = PointerIcon(1)
        val Crosshair = PointerIcon(2)
        val PointingHand = PointerIcon(3)
        val ResizeVertical = PointerIcon(4)
        val ResizeHorizonal = PointerIcon(5)
        val ResizeAll = PointerIcon(6)
        val NotAllowed = PointerIcon(7)
    }

    override fun toString(): String =
        when (this) {
            Arrow -> "Arrow"
            Edit -> "Edit"
            Crosshair -> "Crosshair"
            PointingHand -> "PointingHand"
            ResizeVertical -> "ResizeVertical"
            ResizeHorizonal -> "ResizeHorizonal"
            ResizeAll -> "ResizeAll"
            NotAllowed -> "NotAllowed"
            else -> "Unknown"
        }
}
