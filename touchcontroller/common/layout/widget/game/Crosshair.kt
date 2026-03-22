package top.fifthlight.touchcontroller.common.layout.widget.game

import top.fifthlight.combine.paint.*
import top.fifthlight.touchcontroller.common.gal.paint.CrosshairRenderer
import top.fifthlight.touchcontroller.common.gal.paint.CrosshairRendererFactory
import top.fifthlight.touchcontroller.common.layout.Context

fun Context.Crosshair() {
    val status = result.crosshairStatus ?: return
    val crosshairRenderer: CrosshairRenderer = CrosshairRendererFactory.of()

    val config = config.touchRingConfig
    drawQueue.enqueue { canvas ->
        canvas.withTranslate(status.position * windowScaledSize) {
            if (status.breakPercent > 0f) {
                val progress = status.breakPercent * (1f - config.initialProgress) + config.initialProgress
                crosshairRenderer.renderInner(
                    canvas = canvas,
                    radius = config.radius,
                    outerRadius = config.outerRadius,
                    initialProgress = config.initialProgress,
                    progress = progress
                )
            }
            crosshairRenderer.renderOuter(
                canvas = canvas,
                radius = config.radius,
                outerRadius = config.outerRadius,
            )
        }
    }
}
