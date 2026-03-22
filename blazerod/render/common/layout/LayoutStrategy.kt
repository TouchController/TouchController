package top.fifthlight.blazerod.render.common.layout

import top.fifthlight.blazerod.render.common.util.math.roundUpToMultiple

sealed interface LayoutStrategy {
    val scalarAlign: Int
    val scalarSize: Int

    val vec2Align: Int
    val vec2Size: Int

    val vec3Align: Int
    val vec3Size: Int

    val vec4Align: Int
    val vec4Size: Int

    val mat2Align: Int
    val mat2Size: Int
    val mat2Padding: Boolean

    val mat3Align: Int
    val mat3Size: Int
    val mat3Padding: Boolean

    val mat4Align: Int
    val mat4Size: Int

    val arrayAlignment: Int?

    fun arrayAlignmentOf(baseAlign: Int, baseSize: Int) = arrayAlignment?.let { arrayAlignment ->
        baseAlign roundUpToMultiple arrayAlignment
    } ?: baseAlign

    fun arrayStrideOf(baseAlign: Int, baseSize: Int) = baseSize roundUpToMultiple arrayAlignmentOf(baseAlign, baseSize)

    object Std140LayoutStrategy : LayoutStrategy {
        override val scalarAlign: Int
            get() = 4
        override val scalarSize: Int
            get() = 4
        override val vec2Align: Int
            get() = 8
        override val vec2Size: Int
            get() = 8
        override val vec3Align: Int
            get() = 16
        override val vec3Size: Int
            get() = 12
        override val vec4Align: Int
            get() = 16
        override val vec4Size: Int
            get() = 16
        override val mat2Align: Int
            get() = 16
        override val mat2Size: Int
            get() = 32
        override val mat2Padding: Boolean
            get() = true
        override val mat3Align: Int
            get() = 16
        override val mat3Size: Int
            get() = 48
        override val mat3Padding: Boolean
            get() = true
        override val mat4Align: Int
            get() = 16
        override val mat4Size: Int
            get() = 64

        override val arrayAlignment: Int
            get() = 16
    }

    object Std430LayoutStrategy : LayoutStrategy {
        override val scalarAlign: Int
            get() = 4
        override val scalarSize: Int
            get() = 4
        override val vec2Align: Int
            get() = 8
        override val vec2Size: Int
            get() = 8
        override val vec3Align: Int
            get() = 16
        override val vec3Size: Int
            get() = 12
        override val vec4Align: Int
            get() = 16
        override val vec4Size: Int
            get() = 16
        override val mat2Align: Int
            get() = 8
        override val mat2Size: Int
            get() = 16
        override val mat2Padding: Boolean
            get() = false
        override val mat3Align: Int
            get() = 16
        override val mat3Size: Int
            get() = 48
        override val mat3Padding: Boolean
            get() = true
        override val mat4Align: Int
            get() = 16
        override val mat4Size: Int
            get() = 64

        override val arrayAlignment: Int?
            get() = null
    }

    object PackedLayoutStrategy : LayoutStrategy {
        override val scalarAlign: Int
            get() = 4
        override val scalarSize: Int
            get() = 4
        override val vec2Align: Int
            get() = 4
        override val vec2Size: Int
            get() = 8
        override val vec3Align: Int
            get() = 4
        override val vec3Size: Int
            get() = 12
        override val vec4Align: Int
            get() = 4
        override val vec4Size: Int
            get() = 16
        override val mat2Align: Int
            get() = 4
        override val mat2Size: Int
            get() = 32
        override val mat2Padding: Boolean
            get() = true
        override val mat3Align: Int
            get() = 4
        override val mat3Size: Int
            get() = 48
        override val mat3Padding: Boolean
            get() = false
        override val mat4Align: Int
            get() = 4
        override val mat4Size: Int
            get() = 64

        override val arrayAlignment: Int
            get() = 1
    }
}
