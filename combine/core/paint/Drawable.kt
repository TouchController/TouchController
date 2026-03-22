package top.fifthlight.combine.paint

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.PersistentList
import top.fifthlight.data.*
import kotlin.math.max

interface Drawable {
    val size: IntSize
    val padding: IntPadding

    fun draw(canvas: Canvas, dstRect: IntRect, tint: Color = Colors.WHITE)

    object Empty : Drawable {
        override val size: IntSize
            get() = IntSize.ZERO
        override val padding: IntPadding
            get() = IntPadding.ZERO

        override fun draw(
            canvas: Canvas,
            dstRect: IntRect,
            tint: Color,
        ) = Unit
    }
}

@Immutable
data class LayeredDrawable(
    val layers: PersistentList<Drawable>,
) : Drawable {
    override val size: IntSize = run {
        var maxWidth = 0
        var maxHeight = 0
        for (layer in layers) {
            val size = layer.size
            maxWidth = max(maxWidth, size.width)
            maxHeight = max(maxHeight, size.height)
        }
        IntSize(maxWidth, maxHeight)
    }

    override val padding: IntPadding = run {
        var maxLeft = 0
        var maxTop = 0
        var maxRight = 0
        var maxBottom = 0
        for (layer in layers) {
            val padding = layer.padding
            maxLeft = max(maxLeft, padding.left)
            maxTop = max(maxTop, padding.top)
            maxRight = max(maxRight, padding.right)
            maxBottom = max(maxBottom, padding.bottom)
        }
        IntPadding(maxLeft, maxTop, maxRight, maxBottom)
    }

    override fun draw(canvas: Canvas, dstRect: IntRect, tint: Color) {
        for (layer in layers) {
            layer.draw(canvas, dstRect, tint)
        }
    }
}

@Immutable
data class PaddingDrawable(
    val drawable: Drawable,
    val extraPadding: IntPadding,
) : Drawable {
    override val size = drawable.size + extraPadding.size
    override val padding = drawable.padding + extraPadding

    override fun draw(canvas: Canvas, dstRect: IntRect, tint: Color) = drawable.draw(
        canvas = canvas,
        dstRect = dstRect + extraPadding,
        tint = tint
    )
}

@Immutable
data class ColorDrawable(val color: Color) : Drawable {
    override val size: IntSize
        get() = IntSize.ZERO
    override val padding: IntPadding
        get() = IntPadding.ZERO

    override fun draw(canvas: Canvas, dstRect: IntRect, tint: Color) {
        canvas.fillRect(
            offset = dstRect.offset,
            size = dstRect.size,
            color = color * tint,
        )
    }
}

@Immutable
data class BackgroundTextureDrawable(
    val backgroundTexture: BackgroundTexture,
    val scale: Float,
) : Drawable {
    override val size: IntSize
        get() = backgroundTexture.size
    override val padding: IntPadding
        get() = backgroundTexture.padding

    override fun draw(canvas: Canvas, dstRect: IntRect, tint: Color) {
        backgroundTexture.draw(canvas, dstRect, tint, scale)
    }
}

@Immutable
data class GradientDrawable(
    val colors: PersistentList<Color>,
) : Drawable {
    override val size
        get() = IntSize.ZERO
    override val padding
        get() = IntPadding.ZERO

    init {
        require(colors.size >= 2)
    }

    override fun draw(canvas: Canvas, dstRect: IntRect, tint: Color) {
        val segments = colors.size - 1
        val segmentWidth = dstRect.size.width.toFloat() / segments

        for (index in 0 until segments) {
            val start = colors[index]
            val end = colors[(index + 1) % colors.size]
            canvas.fillGradientRect(
                offset = dstRect.offset.toOffset() + Offset(x = index * segmentWidth, y = 0f),
                size = Size(segmentWidth, dstRect.size.height.toFloat()),
                leftTopColor = start,
                rightTopColor = end,
            )
        }
    }
}
