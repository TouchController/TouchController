package top.fifthlight.blazerod.render.common.runtime.data

import org.joml.Matrix4f
import org.joml.Matrix4fc
import top.fifthlight.blazerod.render.common.util.refcount.AbstractRefCount
import top.fifthlight.blazerod.render.common.util.cowbuffer.CowBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class LocalMatricesBuffer(val primitiveNodesSize: Int) :
    CowBuffer.Content<LocalMatricesBuffer>, AbstractRefCount() {
    companion object {
        private val IDENTITY = Matrix4f()
        const val MAT4X4_SIZE = 4 * 4 * 4
    }

    override val typeId: String
        get() = "model_matrices_buffer"

    val buffer: ByteBuffer =
        ByteBuffer.allocateDirect(primitiveNodesSize * 2 * MAT4X4_SIZE).order(ByteOrder.nativeOrder())

    fun clear() {
        repeat(primitiveNodesSize * 2) {
            IDENTITY.get(it * MAT4X4_SIZE, buffer)
        }
        buffer.clear()
    }

    private val normalMatrix = Matrix4f()
    fun setMatrix(index: Int, src: Matrix4fc) {
        val basePos = index * 2 * MAT4X4_SIZE
        src.get(basePos, buffer)
        src.normal(normalMatrix)
        normalMatrix.get(basePos + MAT4X4_SIZE, buffer)
    }

    fun getPositionMatrix(index: Int, dest: Matrix4f) {
        dest.set(index * 2 * MAT4X4_SIZE, buffer)
    }

    fun getNormalMatrix(index: Int, dest: Matrix4f) {
        dest.set(index * 2 * MAT4X4_SIZE + MAT4X4_SIZE, buffer)
    }

    override fun copy(): LocalMatricesBuffer = LocalMatricesBuffer(primitiveNodesSize).also {
        it.buffer.clear()
        buffer.clear()
        it.buffer.put(buffer)
        it.buffer.clear()
    }

    override fun onClosed() = Unit
}
