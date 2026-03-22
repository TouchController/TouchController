package top.fifthlight.touchcontroller.common.gal.paint

import top.fifthlight.combine.paint.Canvas
import top.fifthlight.mergetools.api.ExpectFactory

interface CrosshairRenderer {
    fun renderOuter(canvas: Canvas, radius: Int, outerRadius: Int)
    fun renderInner(canvas: Canvas, radius: Int, outerRadius: Int, initialProgress: Float, progress: Float)

    @ExpectFactory
    interface Factory {
        fun of(): CrosshairRenderer
    }
}
