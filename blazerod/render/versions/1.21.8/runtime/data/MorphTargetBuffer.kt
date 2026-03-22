package top.fifthlight.blazerod.render.common.runtime.data

import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.ints.IntSet
import top.fifthlight.blazerod.render.common.BlazeRod
import top.fifthlight.blazerod.render.common.util.refcount.AbstractRefCount
import top.fifthlight.blazerod.render.common.util.cowbuffer.CowBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MorphTargetBuffer(
    private val positionTargets: Int,
    private val colorTargets: Int,
    private val texCoordTargets: Int,
) : CowBuffer.Content<MorphTargetBuffer>, AbstractRefCount() {
    companion object {
        private const val INDICES_ENTRY_SIZE = 12 + 12 * BlazeRod.MAX_ENABLED_MORPH_TARGETS
    }

    override val typeId: String
        get() = "morph_target_buffer"

    private val _positionChannel = WeightChannelImpl(
        channelIndex = 0,
        weightOffset = 0,
        targetsSize = positionTargets,
    )
    val positionChannel: WeightChannel
        get() = _positionChannel

    private val _colorChannel = WeightChannelImpl(
        channelIndex = 1,
        weightOffset = positionTargets,
        targetsSize = colorTargets,
    )
    val colorChannel: WeightChannel
        get() = _colorChannel

    private val _texCoordChannel = WeightChannelImpl(
        channelIndex = 2,
        weightOffset = positionTargets + colorTargets,
        targetsSize = texCoordTargets,
    )
    val texCoordChannel: WeightChannel
        get() = _texCoordChannel

    // MorphWeights
    val weightsBuffer: ByteBuffer =
        ByteBuffer.allocateDirect(4 * (positionTargets + colorTargets + texCoordTargets)).order(ByteOrder.nativeOrder())

    // MorphTargetIndices
    val indicesBuffer: ByteBuffer =
        ByteBuffer.allocateDirect(INDICES_ENTRY_SIZE).order(ByteOrder.nativeOrder())

    interface WeightChannel {
        fun uploadIndices()
        operator fun set(index: Int, weight: Float)
        fun keySet(): IntSet
        operator fun get(index: Int): Float
    }

    private inner class WeightChannelImpl private constructor(
        private val channelByteOffset: Int,
        private val weightByteOffset: Int,
        private val enabledIndices: IntSet,
        private val targetsSize: Int,
    ) : WeightChannel {
        constructor(
            channelIndex: Int,
            weightOffset: Int,
            targetsSize: Int,
        ) : this(
            channelByteOffset = channelIndex * 4,
            weightByteOffset = weightOffset * 4,
            enabledIndices = IntOpenHashSet(targetsSize),
            targetsSize = targetsSize,
        )

        private var indicesDirty: Boolean = false

        override fun uploadIndices() {
            if (!indicesDirty) {
                return
            }
            indicesDirty = false
            indicesBuffer.putInt(
                channelByteOffset,
                enabledIndices.size.coerceAtMost(BlazeRod.MAX_ENABLED_MORPH_TARGETS)
            )
            val iterator = enabledIndices.intIterator()
            var index = 0
            while (iterator.hasNext() && index < BlazeRod.MAX_ENABLED_MORPH_TARGETS) {
                indicesBuffer.putInt(12 + index * 12 + channelByteOffset, iterator.nextInt())
                index++
            }
        }

        override operator fun set(index: Int, weight: Float) {
            check(index in 0 until targetsSize) { "Invalid target index: $index, should be in [0, $targetsSize)" }
            weightsBuffer.putFloat(weightByteOffset + index * 4, weight)
            if (weight == 0f) {
                if (enabledIndices.remove(index)) {
                    indicesDirty = true
                }
            } else {
                if (enabledIndices.add(index)) {
                    indicesDirty = true
                }
            }
        }

        override fun keySet() = enabledIndices

        override operator fun get(index: Int) = weightsBuffer.getFloat(weightByteOffset + index * 4)

        fun copyTo(target: WeightChannelImpl) {
            enabledIndices.forEach {
                target[it] = this[it]
            }
        }
    }

    fun uploadIndices() {
        positionChannel.uploadIndices()
        colorChannel.uploadIndices()
        texCoordChannel.uploadIndices()
    }

    override fun copy() = MorphTargetBuffer(
        positionTargets = positionTargets,
        colorTargets = colorTargets,
        texCoordTargets = texCoordTargets,
    ).also {
        _positionChannel.copyTo(it._positionChannel)
        _colorChannel.copyTo(it._colorChannel)
        _texCoordChannel.copyTo(it._texCoordChannel)
    }

    override fun onClosed() = Unit
}
