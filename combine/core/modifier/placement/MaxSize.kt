package top.fifthlight.combine.modifier.placement

import top.fifthlight.combine.layout.constraints.Constraints
import top.fifthlight.combine.layout.measure.Measurable
import top.fifthlight.combine.layout.measure.MeasureResult
import top.fifthlight.combine.layout.measure.MeasureScope
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.drawing.LayoutModifierNode

fun Modifier.maxSize(width: Int, height: Int): Modifier = then(MaxSizeNode(width = width, height = height))

fun Modifier.maxWidth(width: Int): Modifier = then(MaxSizeNode(width = width))

fun Modifier.maxHeight(height: Int): Modifier = then(MaxSizeNode(height = height))

private data class MaxSizeNode(
    val width: Int? = null,
    val height: Int? = null
) : LayoutModifierNode, Modifier.Node<MaxSizeNode> {
    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val placeable = measurable.measure(
            constraints.copy(
                maxWidth = width?.coerceAtMost(constraints.maxWidth) ?: constraints.maxWidth,
                maxHeight = height?.coerceAtMost(constraints.maxHeight) ?: constraints.maxHeight,
            )
        )

        return layout(placeable.width, placeable.height) {
            placeable.placeAt(0, 0)
        }
    }

    override fun MeasureScope.minIntrinsicWidth(measurable: Measurable, height: Int): Int {
        val intrinsic = measurable.minIntrinsicWidth(height)
        return width?.coerceAtMost(intrinsic) ?: intrinsic
    }

    override fun MeasureScope.maxIntrinsicWidth(measurable: Measurable, height: Int): Int {
        val intrinsic = measurable.maxIntrinsicWidth(height)
        return width?.coerceAtMost(intrinsic) ?: intrinsic
    }

    override fun MeasureScope.minIntrinsicHeight(measurable: Measurable, width: Int): Int {
        val intrinsic = measurable.minIntrinsicHeight(width)
        return height?.coerceAtMost(intrinsic) ?: intrinsic
    }

    override fun MeasureScope.maxIntrinsicHeight(measurable: Measurable, width: Int): Int {
        val intrinsic = measurable.maxIntrinsicHeight(width)
        return height?.coerceAtMost(intrinsic) ?: intrinsic
    }
}
