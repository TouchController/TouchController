package top.fifthlight.combine.util.math

infix fun Int.ceilDiv(other: Int) = if (this % other == 0) {
    this / other
} else {
    this / other + 1
}
