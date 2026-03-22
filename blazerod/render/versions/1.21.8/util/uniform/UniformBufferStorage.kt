package top.fifthlight.blazerod.render.common.util.uniform

import com.mojang.blaze3d.buffers.GpuBuffer
import com.mojang.blaze3d.buffers.GpuBufferSlice
import com.mojang.blaze3d.systems.RenderSystem
import org.slf4j.LoggerFactory
import top.fifthlight.blazerod.render.common.util.gpushaderpool.SlicedMappableRingBuffer
import top.fifthlight.blazerod.render.common.debug.UniformBufferTracker
import top.fifthlight.blazerod.render.common.util.math.roundUpToMultiple
import java.nio.ByteBuffer

class UniformBufferStorage(
    private val name: String,
    private val blockSize: Int,
    private var capacity: Int = 8,
) : AutoCloseable {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(UniformBufferStorage::class.java)
    }

    private val oldBuffers = mutableListOf<SlicedMappableRingBuffer>()
    private var buffer: SlicedMappableRingBuffer
    private var size = 0
    private val realBlockSize: Int
    private val alignment: Int

    init {
        val device = RenderSystem.getDevice()
        alignment = device.uniformOffsetAlignment
        realBlockSize = blockSize roundUpToMultiple alignment
        buffer = SlicedMappableRingBuffer(
            nameSupplier = { "$name (capacity $capacity)" },
            usage = GpuBuffer.USAGE_MAP_WRITE or GpuBuffer.USAGE_UNIFORM,
            size = realBlockSize * capacity,
            alignment = alignment,
        )
        UniformBufferTracker.instance?.set(name, capacity)
    }

    /**
     * Call it after each frame.
     */
    fun clear() {
        size = 0
        buffer.rotate()
        oldBuffers.forEach {
            it.close()
        }
        oldBuffers.clear()
    }

    private fun growBuffer(newCapacity: Int) {
        capacity = newCapacity
        UniformBufferTracker.instance?.set(name, newCapacity)
        size = 0
        oldBuffers.add(buffer)
        buffer = SlicedMappableRingBuffer(
            nameSupplier = { "$name (capacity $capacity)" },
            usage = GpuBuffer.USAGE_MAP_WRITE or GpuBuffer.USAGE_UNIFORM,
            size = realBlockSize * capacity,
            alignment = alignment,
        )
    }

    fun write(writer: (ByteBuffer) -> Unit): GpuBufferSlice {
        if (size >= capacity) {
            val newCapacity = capacity * 2
            LOGGER.info(
                "Resizing {}, capacity limit of {} reached during a single frame. New capacity will be {}.",
                name,
                capacity,
                newCapacity
            )
            growBuffer(newCapacity)
        }
        val currentBuffer = buffer.getBlocking()
        val currentSlice = currentBuffer.slice(size * realBlockSize, blockSize)
        RenderSystem.getDevice().createCommandEncoder().mapBuffer(currentSlice, false, true).use {
            writer(it.data())
        }
        size++
        return currentSlice
    }

    override fun close() {
        oldBuffers.forEach {
            it.close()
        }
        oldBuffers.clear()
        buffer.close()
    }
}
