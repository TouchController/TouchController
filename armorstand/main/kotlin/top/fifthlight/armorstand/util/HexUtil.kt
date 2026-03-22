package top.fifthlight.armorstand.util

fun ByteArray.toHexString() = buildString(size * 2) {
    for (byte in this@toHexString) {
        val high = ((byte.toInt() + 256) shr 4) and 0x0F
        val low = (byte.toInt() + 256) and 0x0F
        if (high < 10) {
            append('0' + high)
        } else {
            append('a' + high - 10)
        }
        if (low < 10) {
            append('0' + low)
        } else {
            append('a' + low - 10)
        }
    }
}
