package top.fifthlight.touchcontroller.common.layout.data

data class CrosshairStatus(
    val position: top.fifthlight.data.Offset,
    val breakPercent: Float,
) {
    val positionX
        get() = position.x

    val positionY
        get() = position.y
}
