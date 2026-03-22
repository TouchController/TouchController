package top.fifthlight.combine.widget.layout

import androidx.compose.runtime.Composable
import top.fifthlight.combine.layout.Alignment
import top.fifthlight.combine.layout.Arrangement
import top.fifthlight.combine.layout.Layout
import top.fifthlight.combine.layout.constraints.Constraints
import top.fifthlight.combine.layout.measure.*
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.ParentDataModifierNode

interface RowScope {
    fun Modifier.weight(weight: Float) = then(RowModifier(weight))
    fun Modifier.alignment(alignment: Alignment.Vertical) = then(RowModifier(alignment = alignment))

    companion object : RowScope
}

private data class RowParentData(
    val weight: Float? = null,
    val alignment: Alignment.Vertical? = null,
)

private data class RowModifier(
    val weight: Float? = null,
    val alignment: Alignment.Vertical? = null,
) : ParentDataModifierNode, Modifier.Node<RowModifier> {
    override fun modifierParentData(parentData: Any?): RowParentData {
        val data = parentData as? RowParentData
        if (data != null) {
            return data.copy(
                weight = weight ?: data.weight,
                alignment = alignment ?: data.alignment,
            )
        }
        return RowParentData(
            weight = weight,
            alignment = alignment,
        )
    }
}

@Composable
fun Row(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Left,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    content: @Composable RowScope.() -> Unit = {},
) {
    Layout(
        modifier = modifier,
        measurePolicy = object : MeasurePolicy {
            override fun MeasureScope.measure(measurables: List<Measurable>, constraints: Constraints): MeasureResult {
                val allSpacing = horizontalArrangement.spacing * (measurables.size - 1)
                val childConstraint = constraints.copy(
                    minWidth = 0,
                    minHeight = 0,
                    maxWidth = constraints.maxWidth - allSpacing
                )

                val widths = IntArray(measurables.size)
                val xPositions = IntArray(measurables.size)
                var allWidth = 0
                var maxHeight = 0
                var totalWeight = 0f

                val placeables = Array<Placeable?>(measurables.size) { null }
                measurables.forEachIndexed { index, measurable ->
                    val parentData = measurable.parentData as? RowParentData
                    if (parentData?.weight != null) {
                        widths[index] = -1
                        totalWeight += parentData.weight
                    } else {
                        val placeable = measurable.measure(childConstraint)
                        widths[index] = placeable.width
                        allWidth += placeable.width
                        maxHeight = maxOf(maxHeight, placeable.height)
                        placeables[index] = placeable
                    }
                }

                val weightUnitSpace = if (totalWeight > 0f) {
                    val allSpace = constraints.maxWidth - allWidth - allSpacing
                    val unitSpace = allSpace / totalWeight
                    allWidth += constraints.maxWidth
                    unitSpace
                } else {
                    0f
                }

                for (i in widths.indices) {
                    if (widths[i] == -1) {
                        val weight = (measurables[i].parentData as RowParentData).weight!!
                        widths[i] = (weightUnitSpace * weight).toInt()
                        val placeable = measurables[i].measure(
                            constraints = childConstraint.copy(
                                minWidth = widths[i],
                                maxWidth = widths[i],
                            )
                        )
                        maxHeight = maxOf(maxHeight, placeable.height)
                        placeables[i] = placeable
                    }
                }

                val width = (allWidth + allSpacing).coerceIn(constraints.minWidth, constraints.maxWidth)
                val height = maxHeight.coerceIn(constraints.minHeight, constraints.maxHeight)

                horizontalArrangement.arrangeHorizontally(
                    totalSize = width,
                    sizes = widths,
                    outPositions = xPositions
                )

                return layout(width, height) {
                    placeables.forEachIndexed { index, placeable ->
                        val parentData = measurables[index].parentData as? RowParentData
                        val alignment = parentData?.alignment ?: verticalAlignment
                        placeable!!.placeAt(xPositions[index], alignment.align(placeable.height, height))
                    }
                }
            }

            override fun MeasureScope.minIntrinsicWidth(measurables: List<Measurable>, height: Int): Int {
                var sum = 0
                var spacing = 0
                measurables.forEachIndexed { index, measurable ->
                    val parentData = measurable.parentData as? RowParentData
                    if (parentData?.weight == null) {
                        sum += measurable.minIntrinsicWidth(height)
                        if (index > 0) {
                            spacing += horizontalArrangement.spacing
                        }
                    }
                }
                return sum + spacing
            }

            override fun MeasureScope.maxIntrinsicWidth(measurables: List<Measurable>, height: Int): Int {
                var sum = 0
                var spacing = 0
                measurables.forEachIndexed { index, measurable ->
                    val parentData = measurable.parentData as? RowParentData
                    if (parentData?.weight == null) {
                        sum += measurable.maxIntrinsicWidth(height)
                        if (index > 0) {
                            spacing += horizontalArrangement.spacing
                        }
                    }
                }
                return sum + spacing
            }

            override fun MeasureScope.minIntrinsicHeight(measurables: List<Measurable>, width: Int): Int {
                var intrinsicHeight = 0
                var totalWeight = 0f
                var occupiedWidth = 0

                measurables.forEachIndexed { index, measurable ->
                    val parentData = measurable.parentData as? RowParentData
                    if (parentData?.weight == null) {
                        intrinsicHeight = maxOf(intrinsicHeight, measurable.minIntrinsicHeight(width))
                        occupiedWidth += measurable.minIntrinsicWidth(width)
                        if (index > 0) {
                            occupiedWidth += horizontalArrangement.spacing
                        }
                    } else {
                        totalWeight += parentData.weight
                    }
                }

                if (totalWeight > 0f) {
                    val remainingWidth = (width - occupiedWidth).coerceAtLeast(0)
                    val weightUnitWidth = remainingWidth / totalWeight

                    measurables.forEach { measurable ->
                        val parentData = measurable.parentData as? RowParentData
                        if (parentData?.weight != null) {
                            val weightedChildWidth = (weightUnitWidth * parentData.weight).toInt()
                            intrinsicHeight = maxOf(intrinsicHeight, measurable.minIntrinsicHeight(weightedChildWidth))
                        }
                    }
                }

                return intrinsicHeight
            }

            override fun MeasureScope.maxIntrinsicHeight(measurables: List<Measurable>, width: Int): Int {
                var intrinsicHeight = 0
                var totalWeight = 0f
                var occupiedWidth = 0

                measurables.forEachIndexed { index, measurable ->
                    val parentData = measurable.parentData as? RowParentData
                    if (parentData?.weight == null) {
                        intrinsicHeight = maxOf(intrinsicHeight, measurable.maxIntrinsicHeight(width))
                        occupiedWidth += measurable.maxIntrinsicWidth(width)
                        if (index > 0) {
                            occupiedWidth += horizontalArrangement.spacing
                        }
                    } else {
                        totalWeight += parentData.weight
                    }
                }

                if (totalWeight > 0f) {
                    val remainingWidth = (width - occupiedWidth).coerceAtLeast(0)
                    val weightUnitWidth = remainingWidth / totalWeight

                    measurables.forEach { measurable ->
                        val parentData = measurable.parentData as? RowParentData
                        if (parentData?.weight != null) {
                            val weightedChildWidth = (weightUnitWidth * parentData.weight).toInt()
                            intrinsicHeight = maxOf(intrinsicHeight, measurable.maxIntrinsicHeight(weightedChildWidth))
                        }
                    }
                }

                return intrinsicHeight
            }
        },
        content = {
            RowScope.content()
        }
    )
}
