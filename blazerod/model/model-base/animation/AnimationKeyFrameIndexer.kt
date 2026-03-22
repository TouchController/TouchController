package top.fifthlight.blazerod.model.animation

import it.unimi.dsi.fastutil.floats.AbstractFloatList
import it.unimi.dsi.fastutil.floats.FloatList
import top.fifthlight.blazerod.model.Accessor

import java.nio.ByteOrder

interface AnimationKeyFrameIndexer {
    // First keyframe's time
    val startTime: Float

    // Last keyframe's time
    val lastTime: Float

    // Total indices
    val indices: Int

    data class FindResult(
        var startFrame: Int = 0,
        var endFrame: Int = 0,
        var startTime: Float = 0f,
        var endTime: Float = 0f,
    ) {
        fun clear() {
            startFrame = 0
            endFrame = 0
            startTime = 0f
            endTime = 0f
        }
    }

    // Find two keyframe for specified timestamp, return index and time
    fun findKeyFrames(time: Float, result: FindResult)
}

private fun FloatList.getLastFloat() = getFloat(lastIndex)

private fun FloatList.binarySearch(element: Float, fromIndex: Int = 0, toIndex: Int = size): Int {
    when {
        fromIndex > toIndex -> throw IllegalArgumentException("fromIndex ($fromIndex) is greater than toIndex ($toIndex).")
        fromIndex < 0 -> throw IndexOutOfBoundsException("fromIndex ($fromIndex) is less than zero.")
        toIndex > size -> throw IndexOutOfBoundsException("toIndex ($toIndex) is greater than size ($size).")
    }

    var low = fromIndex
    var high = toIndex - 1

    while (low <= high) {
        val mid = (low + high).ushr(1) // safe from overflows
        val midVal = getFloat(mid)
        val cmp = compareValues(midVal, element)

        if (cmp < 0)
            low = mid + 1
        else if (cmp > 0)
            high = mid - 1
        else
            return mid // key found
    }
    return -(low + 1)  // key not found
}

class ListAnimationKeyFrameIndexer(private val times: FloatList) : AnimationKeyFrameIndexer {
    override val startTime: Float = if (times.isEmpty()) 0f else times.first()
    override val lastTime: Float = if (times.isEmpty()) 0f else times.last()
    override val indices = times.size

    override fun findKeyFrames(time: Float, result: AnimationKeyFrameIndexer.FindResult) {
        when {
            times.isEmpty() -> {
                result.clear()
                return
            }

            times.size < 2 || time <= times.getFloat(0) -> {
                result.startFrame = 0
                result.endFrame = 0
                result.startTime = times.getFloat(0)
                result.endTime = times.getFloat(0)
                return
            }

            time >= times.getLastFloat() -> {
                result.startFrame = times.lastIndex
                result.endFrame = times.lastIndex
                result.startTime = times.getLastFloat()
                result.endTime = times.getLastFloat()
                return
            }
        }

        val maxFrames = 30
        val maxTime = 1.0f

        val searchStartIndex = result.startFrame.coerceIn(0, times.lastIndex - 1)
        val searchEndIndex = (result.startFrame + maxFrames).coerceIn(0, times.lastIndex - 1)
        var currentIndex = result.startFrame.coerceIn(0, times.lastIndex - 1)

        while (currentIndex < times.lastIndex && currentIndex < searchEndIndex && times.getFloat(currentIndex) - times.getFloat(
                searchStartIndex
            ) <= maxTime
        ) {
            if (time >= times.getFloat(currentIndex) && time < times.getFloat(currentIndex + 1)) {
                result.startFrame = currentIndex
                result.endFrame = currentIndex + 1
                result.startTime = times.getFloat(currentIndex)
                result.endTime = times.getFloat(currentIndex + 1)
                return
            }

            if (times.getFloat(currentIndex) > time) {
                break
            }

            currentIndex++
        }

        val searchResult = times.binarySearch(time)
        val foundIndex = if (searchResult >= 0) {
            searchResult
        } else {
            val insertionPoint = -searchResult - 1
            (insertionPoint - 1).coerceAtLeast(0)
        }.coerceIn(0 until times.lastIndex)

        result.startFrame = foundIndex
        result.endFrame = foundIndex + 1
        result.startTime = times.getFloat(foundIndex)
        result.endTime = times.getFloat(foundIndex + 1)
    }
}

private class FloatAccessorList(private val accessor: Accessor) : AbstractFloatList() {
    init {
        require(accessor.type == Accessor.AccessorType.SCALAR) { "Invalid accessor type: should be SCALAR, but got ${accessor.type}" }
        require(accessor.componentType == Accessor.ComponentType.FLOAT) { "Invalid component type: should be FLOAT, but got ${accessor.componentType}" }
    }

    private val slice = accessor.bufferView?.let { bufferView ->
        bufferView.buffer.buffer
            .slice(accessor.byteOffset + bufferView.byteOffset, accessor.totalByteLength)
            .order(ByteOrder.LITTLE_ENDIAN)
    }

    override val size: Int
        get() = accessor.count

    override fun getFloat(index: Int): Float = slice?.getFloat(index * 4) ?: 0f
}

class AccessorAnimationKeyFrameIndexer(private val accessor: Accessor) :
    AnimationKeyFrameIndexer by ListAnimationKeyFrameIndexer(FloatAccessorList(accessor))
