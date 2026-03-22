package top.fifthlight.blazerod.render.common.runtime.uniform

import com.mojang.blaze3d.buffers.GpuBufferSlice
import top.fifthlight.blazerod.render.common.layout.GpuDataLayout
import top.fifthlight.blazerod.render.common.util.uniform.UniformBufferStorage

abstract class UniformBuffer<T : UniformBuffer<T, L>, L : GpuDataLayout<L>>(
    val name: String,
) : AutoCloseable {
    protected var released = false
    private val storage = UniformBufferStorage(name, layout.totalSize, 8)

    abstract val layout: L

    init {
        require(layout.totalSize <= 65536) { "Uniform size too big: ${layout.totalSize}, maximum is 65536" }
        buffers.add(this)
    }

    fun write(block: L.() -> Unit): GpuBufferSlice {
        require(!released) { "Attempt to write a closed uniform buffer" }
        return storage.write { buffer ->
            layout.withBuffer(buffer, block)
        }
    }

    override fun close() {
        if (released) {
            return
        }
        released = true
    }

    private fun clear() = storage.clear()

    companion object {
        private val buffers = mutableListOf<UniformBuffer<*, *>>()

        fun clear() = buffers.forEach { it.clear() }
        fun close() = buffers.forEach { it.close() }
    }
}
