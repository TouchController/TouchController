package top.fifthlight.combine.paint

import top.fifthlight.combine.data.Text
import top.fifthlight.combine.input.pointer.PointerIcon
import top.fifthlight.combine.text.TextMeasurer
import top.fifthlight.data.*
import kotlin.math.max
import kotlin.math.min

interface Canvas {
    fun pushState()
    fun popState()
    fun translate(x: Int, y: Int)
    fun translate(x: Float, y: Float)
    fun rotate(degrees: Float)
    fun scale(x: Float, y: Float)
    fun fillRect(offset: IntOffset = IntOffset.ZERO, size: IntSize = IntSize.ZERO, color: Color)
    fun fillGradientRect(
        offset: Offset = Offset.ZERO,
        size: Size = Size.ZERO,
        leftTopColor: Color,
        leftBottomColor: Color = leftTopColor,
        rightTopColor: Color,
        rightBottomColor: Color = rightTopColor,
    )

    fun drawRect(offset: IntOffset = IntOffset.ZERO, size: IntSize = IntSize.ZERO, color: Color)
    fun drawText(offset: IntOffset, text: String, color: Color)
    fun drawText(offset: IntOffset, width: Int, text: String, color: Color)
    fun drawText(offset: IntOffset, text: Text, color: Color)
    fun drawText(offset: IntOffset, width: Int, text: Text, color: Color)

    fun pushClip(absoluteArea: IntRect, relativeArea: IntRect)
    fun popClip()

    fun requestPointerIcon(pointer: PointerIcon) = Unit
}

class ClipStack {
    private val clipStack = arrayListOf<IntRect>()

    fun pushClip(rect: IntRect): IntRect {
        var rect = rect
        clipStack.lastOrNull()?.let { lastRect ->
            val x1 = max(rect.left, lastRect.left)
            val y1 = max(rect.top, lastRect.top)
            val x2 = min(rect.right, lastRect.right)
            val y2 = min(rect.bottom, lastRect.bottom)
            rect = IntRect(
                offset = IntOffset(
                    x = x1,
                    y = y1,
                ),
                size = IntSize(
                    width = (x2 - x1).coerceAtLeast(0),
                    height = (y2 - y1).coerceAtLeast(0),
                ),
            )
        }
        clipStack.add(rect)
        return rect
    }

    fun popClip(): IntRect? {
        clipStack.removeLastOrNull()
        return clipStack.lastOrNull()
    }
}

inline fun Canvas.withState(crossinline block: () -> Unit) {
    pushState()
    block()
    popState()
}

fun Canvas.translate(offset: IntOffset) = translate(offset.x, offset.y)
fun Canvas.translate(offset: Offset) = translate(offset.x, offset.y)

inline fun Canvas.withTranslate(x: Int, y: Int, crossinline block: (canvas: Canvas) -> Unit) {
    translate(x, y)
    try {
        block(this)
    } finally {
        translate(-x, -y)
    }
}

inline fun Canvas.withTranslate(x: Float, y: Float, crossinline block: (canvas: Canvas) -> Unit) {
    translate(x, y)
    try {
        block(this)
    } finally {
        translate(-x, -y)
    }
}

inline fun Canvas.withTranslate(offset: IntOffset, crossinline block: (canvas: Canvas) -> Unit) {
    translate(offset)
    try {
        block(this)
    } finally {
        translate(-offset)
    }
}

inline fun Canvas.withTranslate(offset: Offset, crossinline block: (canvas: Canvas) -> Unit) {
    translate(offset)
    try {
        block(this)
    } finally {
        translate(-offset)
    }
}

fun Canvas.scale(scale: Float) = scale(scale, scale)

inline fun Canvas.withScale(scale: Float, crossinline block: (canvas: Canvas) -> Unit) {
    pushState()
    scale(scale)
    try {
        block(this)
    } finally {
        popState()
    }
}

inline fun Canvas.withScale(x: Float, y: Float, crossinline block: (canvas: Canvas) -> Unit) {
    pushState()
    scale(x, y)
    try {
        block(this)
    } finally {
        popState()
    }
}

fun Canvas.drawCenteredText(
    offset: IntOffset = IntOffset.ZERO,
    text: String,
    color: Color,
) {
    val size = TextMeasurer.measure(text)
    drawText(offset - size / 2, text, color)
}

fun Canvas.drawCenteredText(
    offset: IntOffset = IntOffset.ZERO,
    text: Text,
    color: Color,
) {
    val size = TextMeasurer.measure(text)
    drawText(offset - size / 2, text, color)
}
