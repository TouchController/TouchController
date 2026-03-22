package top.fifthlight.blazerod.render.common.util.gpushaderpool

import com.mojang.blaze3d.buffers.GpuBuffer
import com.mojang.blaze3d.buffers.GpuBufferSlice
import com.mojang.blaze3d.buffers.GpuFence
import com.mojang.blaze3d.systems.RenderSystem
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap
import top.fifthlight.blazerod.render.common.util.math.lcm
import top.fifthlight.blazerod.render.common.util.math.roundUpToMultiple
import top.fifthlight.blazerod.render.version_1_21_8.extension.*
import java.nio.ByteBuffer
import java.util.TreeSet
import kotlin.collections.ArrayDeque

sealed class GpuShaderDataPool(
    protected val usage: Int,
    protected val extraUsage: Int,
) : AutoCloseable {
    companion object {
        @JvmStatic
        fun create(
            usage: Int,
            extraUsage: Int,
            alignment: Int,
            supportSlicing: Boolean,
        ) = if (supportSlicing) {
            Sliced(
                usage = usage,
                extraUsage = extraUsage,
                alignment = alignment,
            )
        } else {
            Pooled(
                usage = usage,
                extraUsage = extraUsage,
            )
        }
    }

    abstract val supportSlicing: Boolean

    abstract fun allocate(size: Int): GpuBufferSlice
    abstract fun rotate()

    class Sliced(
        usage: Int,
        extraUsage: Int,
        initialCapacity: Int = 512 * 1024,
        private val alignment: Int,
    ) : GpuShaderDataPool(usage, extraUsage) {
        override val supportSlicing: Boolean
            get() = true

        private val oldBuffers = mutableListOf<SlicedMappableRingBuffer>()
        private var size = 0
        private var capacity = initialCapacity
        private var buffer: SlicedMappableRingBuffer? = null

        private fun growCapacity(newCapacity: Int) {
            val buffer = buffer ?: error("Gpu shader data pool not initialized")
            val newBuffer = SlicedMappableRingBuffer(
                nameSupplier = { "Sliced GPU buffer pool" },
                usage = usage or GpuBuffer.USAGE_MAP_WRITE,
                extraUsage = extraUsage,
                size = newCapacity,
                alignment = alignment,
            )
            oldBuffers.add(buffer)
            this.buffer = newBuffer
            size = 0
            capacity = newCapacity
        }

        override fun allocate(size: Int): GpuBufferSlice {
            require(size > 0) { "Size must be positive" }
            val buffer = buffer ?: run {
                // Initialize a new buffer
                capacity = maxOf(capacity, size)
                SlicedMappableRingBuffer(
                    nameSupplier = { "Sliced GPU buffer pool" },
                    usage = usage or GpuBuffer.USAGE_MAP_WRITE,
                    extraUsage = extraUsage,
                    size = capacity,
                    alignment = alignment,
                ).also {
                    this.buffer = it
                }
            }

            val offset = if (alignment == 0 || alignment == 1) {
                this.size
            } else {
                this.size roundUpToMultiple alignment
            }
            return if (offset + size > capacity) {
                growCapacity(maxOf(capacity * 2, size))
                this.size = size
                this.buffer!!.getBlocking().slice(0, size)
            } else {
                this.size = offset + size
                buffer.getBlocking().slice(offset, size)
            }
        }

        override fun rotate() {
            oldBuffers.forEach { it.close() }
            oldBuffers.clear()
            buffer?.rotate()
            size = 0
        }

        override fun close() {
            oldBuffers.forEach { it.close() }
            oldBuffers.clear()
            buffer?.close()
        }
    }

    class Pooled(
        usage: Int,
        extraUsage: Int,
    ) : GpuShaderDataPool(
        usage = usage,
        extraUsage = extraUsage,
    ) {
        override val supportSlicing: Boolean
            get() = false

        private class BufferItem(
            val buffer: GpuBuffer? = null,
            val bufferSize: Int,
            var lastUsedFrame: Int,
        ) : AutoCloseable {
            constructor(
                buffer: GpuBuffer,
                lastUsedFrame: Int,
            ): this(
                buffer = buffer,
                bufferSize = buffer.size,
                lastUsedFrame = lastUsedFrame,
            )

            constructor(bufferSize: Int): this(
                buffer = null,
                bufferSize = bufferSize,
                lastUsedFrame = -1,
            )

            companion object {
                private var nextId: Long = 0
            }
            val id: Long = nextId++

            override fun close() {
                buffer?.close()
            }
        }

        companion object {
            private const val MAX_BUFFER_KEEP_FRAMES = 600
        }

        private val availableBuffers =
            TreeSet<BufferItem>(compareBy<BufferItem> {
                it.bufferSize
            }.thenComparator { a, b ->
                b.id.compareTo(a.id)
            })

        private class FrameData(
            var fence: GpuFence? = null,
            val buffers: MutableList<BufferItem> = mutableListOf(),
        )

        private val allFrameData = Int2ReferenceOpenHashMap<FrameData>().also { it[0] = FrameData() }
        private val waitingFrameData = ArrayDeque<FrameData>()

        private var currentFrame: Int = 0

        override fun allocate(size: Int): GpuBufferSlice {
            require(size > 0) { "Size must be positive" }
            val frameData =
                allFrameData.get(currentFrame) ?: error("No frame data of frame $currentFrame, this should not happen!")

            val foundBuffer = BufferItem(size).use {
                availableBuffers.ceiling(it)
            }
            val allocatedBuffer = if (foundBuffer != null) {
                availableBuffers.remove(foundBuffer)
                val frame = allFrameData.get(foundBuffer.lastUsedFrame)
                frame?.buffers?.remove(foundBuffer)
                foundBuffer.lastUsedFrame = currentFrame
                foundBuffer
            } else {
                BufferItem(
                    buffer = RenderSystem.getDevice().createBuffer(
                        labelGetter = { "Pooled GPU buffer" },
                        usage = usage or GpuBuffer.USAGE_MAP_WRITE,
                        extraUsage = extraUsage,
                        size = size,
                    ),
                    lastUsedFrame = currentFrame,
                )
            }

            frameData.buffers.add(allocatedBuffer)
            return allocatedBuffer.buffer!!.slice()
        }

        override fun rotate() {
            // Insert a fence at current frame
            allFrameData[currentFrame]?.let {
                it.fence = RenderSystem.getDevice().createCommandEncoder().createFence()
                waitingFrameData.addLast(it)
            }

            // Advance current frame
            currentFrame++
            FrameData().also { allFrameData.put(currentFrame, it) }

            // Wait fences and add buffer to available
            while (waitingFrameData.size >= 3) {
                val frameData = waitingFrameData.removeFirst()
                frameData.fence?.use {
                    it.awaitCompletion(Long.MAX_VALUE)
                    it.close()
                    frameData.fence = null
                }
                availableBuffers.addAll(frameData.buffers)
            }

            // Clean oldest frame data
            allFrameData.remove(currentFrame - MAX_BUFFER_KEEP_FRAMES)?.let { frameData ->
                frameData.fence?.close()
                availableBuffers.removeAll(frameData.buffers.toSet())
                frameData.buffers.forEach { it.close() }
                frameData.buffers.clear()
            }
        }

        override fun close() {
            allFrameData.values.forEach { frameData ->
                frameData.buffers.forEach { it.close() }
                frameData.fence?.close()
            }
            allFrameData.clear()

            availableBuffers.forEach { it.close() }
            availableBuffers.clear()
        }
    }
}

fun GpuShaderDataPool.Companion.ofSsbo() = GpuShaderDataPool.create(
    usage = 0,
    extraUsage = GpuBufferExt.EXTRA_USAGE_STORAGE_BUFFER,
    alignment = RenderSystem.getDevice().ssboOffsetAlignment,
    supportSlicing = true
)

fun GpuShaderDataPool.Companion.ofTbo() = RenderSystem.getDevice().let {
    GpuShaderDataPool.create(
        usage = GpuBuffer.USAGE_UNIFORM_TEXEL_BUFFER,
        extraUsage = 0,
        alignment = if (it.supportTextureBufferSlice) {
            lcm(it.textureBufferOffsetAlignment, it.uniformOffsetAlignment)
        } else {
            it.uniformOffsetAlignment
        },
        supportSlicing = it.supportTextureBufferSlice
    )
}

fun GpuShaderDataPool.write(size: Int, block: ByteBuffer.() -> Unit) = allocate(size).also {
    val commandEncoder = RenderSystem.getDevice().createCommandEncoder()
    commandEncoder.mapBuffer(it, false, true).use { mapped ->
        block(mapped.data())
    }
}

fun GpuShaderDataPool.upload(byteBuffer: ByteBuffer) = write(byteBuffer.capacity()) {
    val copied = byteBuffer.duplicate()
    copied.clear()
    put(copied)
}

fun GpuShaderDataPool.upload(byteBuffers: List<ByteBuffer>) = write(byteBuffers.sumOf { it.capacity() }) {
    byteBuffers.forEach {
        val copied = it.duplicate()
        copied.clear()
        put(copied)
    }
}
