package top.fifthlight.blazerod.render.version_1_21_8.runtime.renderer.util

import org.lwjgl.system.MemoryUtil
import java.nio.ByteBuffer
import kotlin.math.max

class CpuBufferPool(
    initialCapacity: Int = 32 * 1024 * 1024,
) : AutoCloseable {
    private class BufferEntry private constructor(
        val address: Long,
        val capacity: Int,
    ) : AutoCloseable {
        constructor(size: Int) : this(
            address = MemoryUtil.nmemAlloc(size.toLong()),
            capacity = size,
        )

        var allocated: Int = 0

        fun allocate(size: Int): ByteBuffer {
            require(allocated + size <= capacity) { "Buffer overflow: trying to allocate $size bytes, but only ${capacity - allocated} bytes available" }
            val chunk = MemoryUtil.memByteBuffer(address + allocated, size)
            allocated += size
            return chunk
        }

        fun rotate() {
            allocated = 0
        }

        override fun close() {
            MemoryUtil.nmemFree(address)
        }
    }

    private val oldBuffers = mutableListOf<BufferEntry>()
    private var currentEntry = BufferEntry(initialCapacity)

    fun allocate(size: Int): ByteBuffer = if (currentEntry.allocated + size <= currentEntry.capacity) {
        currentEntry.allocate(size)
    } else {
        val newCapacity = max(size, currentEntry.capacity * 2)
        val newEntry = BufferEntry(newCapacity)
        oldBuffers.add(currentEntry)
        currentEntry = newEntry
        currentEntry.allocate(size)
    }

    fun rotate() {
        oldBuffers.forEach { it.close() }
        oldBuffers.clear()
        currentEntry.rotate()
    }

    override fun close() {
        oldBuffers.forEach { it.close() }
        currentEntry.close()
    }
}
