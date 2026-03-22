package top.fifthlight.touchcontroller.common.layout.widget

import top.fifthlight.combine.paint.Color
import top.fifthlight.combine.paint.Texture
import top.fifthlight.data.*
import top.fifthlight.touchcontroller.common.layout.Context

private fun calcDstRect(
    srcRect: IntRect,
    dstSize: IntSize,
    padding: IntPadding,
): Rect {
    if (padding == IntPadding.ZERO) {
        return Rect(offset = Offset.ZERO, size = dstSize.toSize())
    }
    if (srcRect.size.width == 0 || srcRect.size.height == 0) {
        return Rect(offset = Offset.ZERO, size = Size.ZERO)
    }

    val topRadio = padding.top.toFloat() / srcRect.size.height.toFloat()
    val bottomRadio = padding.bottom.toFloat() / srcRect.size.height.toFloat()
    val leftRadio = padding.left.toFloat() / srcRect.size.width.toFloat()
    val rightRadio = padding.right.toFloat() / srcRect.size.width.toFloat()
    return Rect(
        offset = Offset(
            x = dstSize.width * leftRadio,
            y = dstSize.height * topRadio,
        ),
        size = Size(
            width = dstSize.width * (1 - leftRadio - rightRadio),
            height = dstSize.height * (1 - topRadio - bottomRadio),
        )
    )
}

fun Context.Texture(
    texture: Texture,
    srcRect: IntRect = IntRect(IntOffset.ZERO, texture.size),
    padding: IntPadding = IntPadding.ZERO,
) {
    if (opacity == 1f) {
        drawQueue.enqueue { canvas ->
            texture.draw(
                canvas = canvas,
                dstRect = calcDstRect(
                    srcRect = srcRect,
                    dstSize = size,
                    padding = padding,
                ),
                srcRect = (srcRect + padding).toRect(),
            )
        }
    } else {
        val color = Color(((0xFF * opacity).toInt() shl 24) or 0xFFFFFF)
        drawQueue.enqueue { canvas ->
            texture.draw(
                canvas = canvas,
                dstRect = calcDstRect(
                    srcRect = srcRect,
                    dstSize = size,
                    padding = padding,
                ),
                srcRect = (srcRect + padding).toRect(),
                tint = color,
            )
        }
    }
}

fun Context.Texture(
    texture: Texture,
    srcRect: IntRect = IntRect(IntOffset.ZERO, texture.size),
    padding: IntPadding = IntPadding.ZERO,
    tint: Color,
) {
    if (opacity == 1f) {
        drawQueue.enqueue { canvas ->
            texture.draw(
                canvas = canvas,
                dstRect = calcDstRect(
                    srcRect = srcRect,
                    dstSize = size,
                    padding = padding,
                ),
                srcRect = (srcRect + padding).toRect(),
                tint = tint
            )
        }
    } else {
        val colorWithoutAlpha = tint.value and 0xFFFFFF
        val colorWithAlpha = Color(((0xFF * opacity).toInt() shl 24) or colorWithoutAlpha)
        drawQueue.enqueue { canvas ->
            texture.draw(
                canvas = canvas,
                dstRect = calcDstRect(
                    srcRect = srcRect,
                    dstSize = size,
                    padding = padding,
                ),
                srcRect = (srcRect + padding).toRect(),
                tint = colorWithAlpha,
            )
        }
    }
}
