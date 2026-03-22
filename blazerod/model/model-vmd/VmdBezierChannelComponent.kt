package top.fifthlight.blazerod.model.vmd

import it.unimi.dsi.fastutil.bytes.ByteList
import top.fifthlight.blazerod.model.animation.AnimationChannelComponent
import kotlin.math.abs

private object VmdBezierResolver {
    fun resolve(p1X: UByte, p1Y: UByte, p2X: UByte, p2Y: UByte, delta: Float): Float {
        val x0 = 0f
        val y0 = 0f
        val x1 = p1X.toFloat() / 127f
        val y1 = p1Y.toFloat() / 127f
        val x2 = p2X.toFloat() / 127f
        val y2 = p2Y.toFloat() / 127f
        val x3 = 1f
        val y3 = 1f

        val targetX = delta

        fun bezierX(t: Float): Float {
            val t_ = 1 - t
            return t_ * t_ * t_ * x0 +
                    3 * t_ * t_ * t * x1 +
                    3 * t_ * t * t * x2 +
                    t * t * t * x3
        }

        fun bezierY(t: Float): Float {
            val t_ = 1 - t
            return t_ * t_ * t_ * y0 +
                    3 * t_ * t_ * t * y1 +
                    3 * t_ * t * t * y2 +
                    t * t * t * y3
        }

        var low = 0f
        var high = 1f
        var t = 0f

        for (i in 0 until 100) {
            t = (low + high) / 2f
            val currentX = bezierX(t)

            if (abs(currentX - targetX) < 1e-6f) {
                break
            }

            if (currentX < targetX) {
                low = t
            } else {
                high = t
            }
        }

        return bezierY(t)
    }
}

class VmdBezierChannelComponent(
    val values: ByteList,
    val frames: Int,
    val channels: Int,
    // Normal motion order: x0, x1, y0, y1
    // Camera order: x0, y0, x1, y1
    val cameraOrder: Boolean,
) : AnimationChannelComponent<VmdBezierChannelComponent, VmdBezierChannelComponent.Type> {
    init {
        require(values.size == frames * 4 * channels) { "Invalid VMD bezier value size: expect ${frames * 4 * channels} bytes, but got ${values.size} bytes" }
    }

    object Type :
        AnimationChannelComponent.Type<VmdBezierChannelComponent, Type> {
        override val name: String
            get() = "vmd_bezier_channel"
    }

    override val type: Type
        get() = Type

    fun getDelta(frame: Int, channel: Int, delta: Float): Float {
        require(frame in 0 until frames) { "Invalid frame index" }
        require(channel in 0 until channels) { "Invalid channel index" }
        val dataOffset = frame * channels * 4 + channel * 4
        return if (cameraOrder) {
            val p1X = values.getByte(dataOffset).toUByte()
            val p2X = values.getByte(dataOffset + 1).toUByte()
            val p1Y = values.getByte(dataOffset + 2).toUByte()
            val p2Y = values.getByte(dataOffset + 3).toUByte()
            VmdBezierResolver.resolve(p1X, p1Y, p2X, p2Y, delta)
        } else {
            val p1X = values.getByte(dataOffset).toUByte()
            val p1Y = values.getByte(dataOffset + 1).toUByte()
            val p2X = values.getByte(dataOffset + 2).toUByte()
            val p2Y = values.getByte(dataOffset + 3).toUByte()
            VmdBezierResolver.resolve(p1X, p1Y, p2X, p2Y, delta)
        }
    }
}
