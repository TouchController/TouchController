package top.fifthlight.touchcontroller.common.layout.widget.game

import top.fifthlight.combine.paint.Colors
import top.fifthlight.combine.paint.drawCenteredText
import top.fifthlight.combine.paint.withTranslate
import top.fifthlight.data.IntOffset
import top.fifthlight.data.IntSize
import top.fifthlight.touchcontroller.common.layout.Context

fun Context.Pointers() {
    drawQueue.enqueue { canvas ->
        pointers.forEach { (id, pointer) ->
            canvas.withTranslate(pointer.scaledOffset) {canvas ->
                canvas.fillRect(IntOffset(-1, -1), IntSize(2, 2), Colors.WHITE)
                canvas.drawRect(IntOffset(-4, -4), IntSize(8, 8), Colors.WHITE)
                canvas.drawCenteredText(
                    offset = IntOffset(0, 9),
                    text = id.toString(),
                    color = Colors.WHITE,
                )
            }
        }
    }
}
