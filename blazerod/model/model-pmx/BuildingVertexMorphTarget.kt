package top.fifthlight.blazerod.model.pmx

import java.nio.ByteBuffer
import java.nio.ByteOrder

internal class BuildingVertexMorphTarget(vertices: Int) {
    private var finished = false
    private val buffer = ByteBuffer.allocateDirect(vertices * 12).order(ByteOrder.nativeOrder())

    private fun checkNotFinished() = check(!finished) { "Already finished" }

    fun setVertex(index: Int, x: Float, y: Float, z: Float) {
        checkNotFinished()
        buffer.putFloat(index * 12, x)
        buffer.putFloat(index * 12 + 4, y)
        buffer.putFloat(index * 12 + 8, z)
    }

    fun finish(): ByteBuffer {
        checkNotFinished()
        finished = true
        return buffer
    }
}
