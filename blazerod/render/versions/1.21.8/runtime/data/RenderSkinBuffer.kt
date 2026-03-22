package top.fifthlight.blazerod.render.common.runtime.data

import org.joml.Matrix4f
import org.joml.Matrix4fc
import top.fifthlight.blazerod.render.common.util.refcount.AbstractRefCount
import top.fifthlight.blazerod.render.common.util.cowbuffer.CowBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class RenderSkinBuffer(
    val jointSize: Int,
) : CowBuffer.Content<RenderSkinBuffer>,
    AbstractRefCount() {
    companion object {
        private val IDENTITY = Matrix4f()
        const val MAT4X4_SIZE = 4 * 4 * 4
    }

    override val typeId: String
        get() = "render_skin_buffer"

    val buffer: ByteBuffer = ByteBuffer.allocateDirect(jointSize * 2 * MAT4X4_SIZE).order(ByteOrder.nativeOrder())

    fun clear() {
        repeat(jointSize * 2) {
            IDENTITY.get(it * MAT4X4_SIZE, buffer)
        }
        buffer.rewind()
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

    override fun copy(): RenderSkinBuffer = RenderSkinBuffer(jointSize).also {
        it.buffer.clear()
        buffer.clear()
        it.buffer.put(buffer)
        it.buffer.clear()
    }

    override fun onClosed() = Unit
}
