package top.fifthlight.combine.modifier.placement

import top.fifthlight.combine.layout.constraints.Constraints
import top.fifthlight.combine.layout.measure.Measurable
import top.fifthlight.combine.layout.measure.MeasureResult
import top.fifthlight.combine.layout.measure.MeasureScope
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.drawing.LayoutModifierNode

sealed class IntrinsicSize {
    data object Min : IntrinsicSize()
    data object Max : IntrinsicSize()
}

fun Modifier.intrinsicSize(
    intrinsicSize: IntrinsicSize,
    matchWidth: Boolean = false,
    matchHeight: Boolean = false,
): Modifier = then(IntrinsicSizeNode(intrinsicSize, matchWidth, matchHeight))

fun Modifier.width(intrinsicSize: IntrinsicSize) = intrinsicSize(
    intrinsicSize = intrinsicSize,
    matchWidth = true,
    matchHeight = false,
)

fun Modifier.height(intrinsicSize: IntrinsicSize) = intrinsicSize(
    intrinsicSize = intrinsicSize,
    matchWidth = false,
    matchHeight = true,
)

fun Modifier.size(intrinsicSize: IntrinsicSize) = intrinsicSize(
    intrinsicSize = intrinsicSize,
    matchWidth = true,
    matchHeight = true,
)

private data class IntrinsicSizeNode(
    val intrinsicSize: IntrinsicSize,
    val matchWidth: Boolean,
    val matchHeight: Boolean,
) : LayoutModifierNode, Modifier.Node<IntrinsicSizeNode> {
    override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
        val width = when (intrinsicSize) {
            IntrinsicSize.Min -> measurable.minIntrinsicWidth(constraints.maxHeight)
            IntrinsicSize.Max -> measurable.maxIntrinsicWidth(constraints.maxHeight)
        }.coerceIn(constraints.minWidth, constraints.maxWidth)

        val height = when (intrinsicSize) {
            IntrinsicSize.Min -> measurable.minIntrinsicHeight(constraints.maxWidth)
            IntrinsicSize.Max -> measurable.maxIntrinsicHeight(constraints.maxWidth)
        }.coerceIn(constraints.minHeight, constraints.maxHeight)

        val placeable = measurable.measure(
            Constraints(
                minWidth = if (matchWidth) {
                    width
                } else {
                    constraints.minWidth
                },
                maxWidth = if (matchWidth) {
                    width
                } else {
                    constraints.maxWidth
                },
                minHeight = if (matchHeight) {
                    height
                } else {
                    constraints.minHeight
                },
                maxHeight = if (matchHeight) {
                    height
                } else {
                    constraints.maxHeight
                }
            )
        )

        return layout(placeable.width, placeable.height) {
            placeable.placeAt(0, 0)
        }
    }

    override fun MeasureScope.minIntrinsicWidth(measurable: Measurable, height: Int): Int {
        return if (matchWidth) {
            when (intrinsicSize) {
                IntrinsicSize.Min -> measurable.minIntrinsicWidth(height)
                IntrinsicSize.Max -> measurable.maxIntrinsicWidth(height)
            }
        } else {
            measurable.minIntrinsicWidth(height)
        }
    }

    override fun MeasureScope.maxIntrinsicWidth(measurable: Measurable, height: Int): Int {
        return if (matchWidth) {
            when (intrinsicSize) {
                IntrinsicSize.Min -> measurable.minIntrinsicWidth(height)
                IntrinsicSize.Max -> measurable.maxIntrinsicWidth(height)
            }
        } else {
            measurable.maxIntrinsicWidth(height)
        }
    }

    override fun MeasureScope.minIntrinsicHeight(measurable: Measurable, width: Int): Int {
        return if (matchHeight) {
            when (intrinsicSize) {
                IntrinsicSize.Min -> measurable.minIntrinsicHeight(width)
                IntrinsicSize.Max -> measurable.maxIntrinsicHeight(width)
            }
        } else {
            measurable.minIntrinsicHeight(width)
        }
    }

    override fun MeasureScope.maxIntrinsicHeight(measurable: Measurable, width: Int): Int {
        return if (matchHeight) {
            when (intrinsicSize) {
                IntrinsicSize.Min -> measurable.minIntrinsicHeight(width)
                IntrinsicSize.Max -> measurable.maxIntrinsicHeight(width)
            }
        } else {
            measurable.maxIntrinsicHeight(width)
        }
    }
}
