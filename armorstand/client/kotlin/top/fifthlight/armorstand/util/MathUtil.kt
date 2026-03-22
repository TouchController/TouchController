package top.fifthlight.armorstand.util

// 180 / PI
internal fun Float.toRadians() = this * 0.017453292f

internal infix fun Int.ceilDiv(other: Int) = if (this % other == 0) {
    this / other
} else {
    this / other + 1
}
