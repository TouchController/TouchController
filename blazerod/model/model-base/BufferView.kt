package top.fifthlight.blazerod.model

data class BufferView(
    val buffer: Buffer,
    val byteLength: Int,
    val byteOffset: Int,
    val byteStride: Int,
) {
    enum class Target {
        ARRAY_BUFFER,
        ELEMENT_ARRAY_BUFFER,
    }
}
