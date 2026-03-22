package top.fifthlight.data

import kotlinx.serialization.Serializable

private class IntRectIterator(
    private val rect: IntRect,
) : Iterator<IntOffset> {
    private var currentX = rect.left
    private var currentY = rect.top

    override fun next(): IntOffset {
        if (!hasNext()) throw NoSuchElementException()

        val nextOffset = IntOffset(currentX, currentY)

        currentX++
        if (currentX >= rect.right) {
            currentX = rect.left
            currentY++
        }

        return nextOffset
    }

    override fun hasNext(): Boolean {
        return currentY < rect.bottom && currentX < rect.right
    }
}

@Serializable
data class IntRect(
    val offset: IntOffset = IntOffset.ZERO,
    val size: IntSize = IntSize.ZERO,
) : Iterable<IntOffset> {
    val left
        get() = offset.x
    val top
        get() = offset.y
    val right
        get() = offset.x + size.width
    val bottom
        get() = offset.y + size.height
    val xRange
        get() = left until right
    val yRange
        get() = top until bottom

    companion object {
        val ZERO = IntRect(offset = IntOffset.ZERO, size = IntSize.ZERO)
    }

    operator fun plus(padding: IntPadding) = IntRect(
        offset = IntOffset(
            x = offset.x + padding.left,
            y = offset.y + padding.top,
        ),
        size = IntSize(
            width = size.width - padding.width,
            height = size.height - padding.height,
        )
    )

    fun toRect() = Rect(
        offset = offset.toOffset(),
        size = size.toSize()
    )

    override fun iterator(): Iterator<IntOffset> = IntRectIterator(this)
}
