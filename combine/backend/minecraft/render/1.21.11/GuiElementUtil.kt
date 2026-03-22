package top.fifthlight.combine.backend.minecraft.render.v1_21_11

import net.minecraft.client.gui.navigation.ScreenRectangle
import org.joml.Matrix3x2f
import kotlin.math.ceil

internal object GuiElementUtil {
    fun getBounds(
        x0: Float,
        y0: Float,
        x1: Float,
        y1: Float,
        matrix3x2f: Matrix3x2f,
        screenRectangle: ScreenRectangle?,
    ): ScreenRectangle {
        val translatedRectangle = ScreenRectangle(
            x0.toInt(),
            y0.toInt(),
            ceil(x1 - x0).toInt(),
            ceil(y1 - y0).toInt()
        ).transformMaxBounds(matrix3x2f)
        return screenRectangle?.intersection(translatedRectangle) ?: translatedRectangle
    }
}
