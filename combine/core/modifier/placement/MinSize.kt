package top.fifthlight.combine.modifier.placement

import top.fifthlight.combine.layout.constraints.Constraints
import top.fifthlight.combine.layout.measure.Measurable
import top.fifthlight.combine.layout.measure.MeasureResult
import top.fifthlight.combine.layout.measure.MeasureScope
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.drawing.LayoutModifierNode
import top.fifthlight.data.IntSize

fun Modifier.minSize(size: IntSize): Modifier = then(MinSizeNode(width = size.width, height = size.height))

fun Modifier.minSize(width: Int, height: Int): Modifier = then(MinSizeNode(width = width, height = height))

fun Modifier.minWidth(width: Int): Modifier = then(MinSizeNode(width = width))

fun Modifier.minHeight(height: Int): Modifier = then(MinSizeNode(height = height))

private data class MinSizeNode(
    val width: Int? = null,
    val height: Int? = null
) : LayoutModifierNode, Modifier.Node<MinSizeNode> {
    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val placeable = measurable.measure(constraints.copy(
            minWidth = width?.coerceAtLeast(constraints.minWidth) ?: constraints.minWidth,
            minHeight = height?.coerceAtLeast(constraints.minHeight) ?: constraints.minHeight,
        ))

        return layout(placeable.width, placeable.height) {
            placeable.placeAt(0, 0)
        }
    }

    override fun MeasureScope.minIntrinsicWidth(measurable: Measurable, height: Int): Int {
        val intrinsic = measurable.minIntrinsicWidth(height)
        return width?.coerceAtLeast(intrinsic) ?: intrinsic
    }

    override fun MeasureScope.maxIntrinsicWidth(measurable: Measurable, height: Int): Int {
        val intrinsic = measurable.maxIntrinsicWidth(height)
        return width?.coerceAtLeast(intrinsic) ?: intrinsic
    }

    override fun MeasureScope.minIntrinsicHeight(measurable: Measurable, width: Int): Int {
        val intrinsic = measurable.minIntrinsicHeight(width)
        return height?.coerceAtLeast(intrinsic) ?: intrinsic
    }

    override fun MeasureScope.maxIntrinsicHeight(measurable: Measurable, width: Int): Int {
        val intrinsic = measurable.maxIntrinsicHeight(width)
        return height?.coerceAtLeast(intrinsic) ?: intrinsic
    }
}
