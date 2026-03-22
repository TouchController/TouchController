package top.fifthlight.combine.widget.layout

import androidx.compose.runtime.Composable
import top.fifthlight.combine.layout.Alignment
import top.fifthlight.combine.layout.Arrangement
import top.fifthlight.combine.layout.Layout
import top.fifthlight.combine.layout.constraints.Constraints
import top.fifthlight.combine.layout.measure.*
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.ParentDataModifierNode

interface ColumnScope {
    fun Modifier.weight(weight: Float) = then(ColumnModifier(weight = weight))
    fun Modifier.alignment(alignment: Alignment.Horizontal) = then(ColumnModifier(alignment = alignment))

    companion object : ColumnScope
}

private data class ColumnParentData(
    val weight: Float? = null,
    val alignment: Alignment.Horizontal? = null,
)

private data class ColumnModifier(
    val weight: Float? = null,
    val alignment: Alignment.Horizontal? = null,
) : ParentDataModifierNode, Modifier.Node<ColumnModifier> {
    override fun modifierParentData(parentData: Any?): ColumnParentData {
        val data = parentData as? ColumnParentData
        if (data != null) {
            return data.copy(
                weight = weight ?: data.weight,
                alignment = alignment ?: data.alignment,
            )
        }
        return ColumnParentData(
            weight = weight,
            alignment = alignment,
        )
    }
}

@Composable
fun Column(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Left,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    Layout(
        modifier = modifier,
        measurePolicy = object : MeasurePolicy {
            override fun MeasureScope.measure(measurables: List<Measurable>, constraints: Constraints): MeasureResult {
                val allSpacing = verticalArrangement.spacing * (measurables.size - 1)
                val childConstraint = constraints.copy(
                    minWidth = 0,
                    minHeight = 0,
                    maxHeight = constraints.maxHeight - allSpacing
                )

                val heights = IntArray(measurables.size)
                val yPositions = IntArray(measurables.size)
                var allHeight = 0
                var maxWidth = 0
                var totalWeight = 0f

                val placeables = Array<Placeable?>(measurables.size) { null }
                measurables.forEachIndexed { index, measurable ->
                    val parentData = measurable.parentData as? ColumnParentData
                    if (parentData?.weight != null) {
                        heights[index] = -1
                        totalWeight += parentData.weight
                    } else {
                        val placeable = measurable.measure(childConstraint)
                        heights[index] = placeable.height
                        allHeight += placeable.height
                        maxWidth = maxOf(maxWidth, placeable.width)
                        placeables[index] = placeable
                    }
                }

                val weightUnitSpace = if (totalWeight > 0f) {
                    val allSpace = constraints.maxHeight - allHeight - allSpacing
                    val unitSpace = allSpace / totalWeight
                    allHeight += constraints.maxHeight
                    unitSpace
                } else {
                    0f
                }

                for (i in heights.indices) {
                    if (heights[i] == -1) {
                        val weight = (measurables[i].parentData as ColumnParentData).weight!!
                        heights[i] = (weightUnitSpace * weight).toInt()
                        val placeable = measurables[i].measure(
                            constraints = childConstraint.copy(
                                minHeight = heights[i],
                                maxHeight = heights[i],
                            )
                        )
                        maxWidth = maxOf(maxWidth, placeable.width)
                        placeables[i] = placeable
                    }
                }

                val width = maxWidth.coerceIn(constraints.minWidth, constraints.maxWidth)
                val height = (allHeight + allSpacing).coerceIn(constraints.minHeight, constraints.maxHeight)

                verticalArrangement.arrangeVertically(
                    totalSize = height,
                    sizes = heights,
                    outPositions = yPositions
                )

                return layout(width, height) {
                    placeables.forEachIndexed { index, placeable ->
                        val parentData = measurables[index].parentData as? ColumnParentData
                        val alignment = parentData?.alignment ?: horizontalAlignment
                        placeable!!.placeAt(alignment.align(placeable.width, width), yPositions[index])
                    }
                }
            }

            override fun MeasureScope.minIntrinsicWidth(measurables: List<Measurable>, height: Int): Int {
                var intrinsicWidth = 0
                var totalWeight = 0f
                var occupiedHeight = 0

                measurables.forEachIndexed { index, measurable ->
                    val parentData = measurable.parentData as? ColumnParentData
                    if (parentData?.weight == null) {
                        intrinsicWidth = maxOf(intrinsicWidth, measurable.minIntrinsicWidth(height))
                        occupiedHeight += measurable.minIntrinsicHeight(height)
                        if (index > 0) {
                            occupiedHeight += verticalArrangement.spacing
                        }
                    } else {
                        totalWeight += parentData.weight
                    }
                }

                if (totalWeight > 0f) {
                    val remainingHeight = (height - occupiedHeight).coerceAtLeast(0)
                    val weightUnitHeight = remainingHeight / totalWeight

                    measurables.forEach { measurable ->
                        val parentData = measurable.parentData as? ColumnParentData
                        if (parentData?.weight != null) {
                            val weightedChildHeight = (weightUnitHeight * parentData.weight).toInt()
                            intrinsicWidth = maxOf(intrinsicWidth, measurable.minIntrinsicWidth(weightedChildHeight))
                        }
                    }
                }

                return intrinsicWidth
            }

            override fun MeasureScope.maxIntrinsicWidth(measurables: List<Measurable>, height: Int): Int {
                var intrinsicWidth = 0
                var totalWeight = 0f
                var occupiedHeight = 0

                measurables.forEachIndexed { index, measurable ->
                    val parentData = measurable.parentData as? ColumnParentData
                    if (parentData?.weight == null) {
                        intrinsicWidth = maxOf(intrinsicWidth, measurable.maxIntrinsicWidth(height))
                        occupiedHeight += measurable.maxIntrinsicHeight(height)
                        if (index > 0) {
                            occupiedHeight += verticalArrangement.spacing
                        }
                    } else {
                        totalWeight += parentData.weight
                    }
                }

                if (totalWeight > 0f) {
                    val remainingHeight = (height - occupiedHeight).coerceAtLeast(0)
                    val weightUnitHeight = remainingHeight / totalWeight

                    measurables.forEach { measurable ->
                        val parentData = measurable.parentData as? ColumnParentData
                        if (parentData?.weight != null) {
                            val weightedChildHeight = (weightUnitHeight * parentData.weight).toInt()
                            intrinsicWidth = maxOf(intrinsicWidth, measurable.maxIntrinsicWidth(weightedChildHeight))
                        }
                    }
                }

                return intrinsicWidth
            }

            override fun MeasureScope.minIntrinsicHeight(measurables: List<Measurable>, width: Int): Int {
                var sum = 0
                var spacing = 0
                measurables.forEachIndexed { index, measurable ->
                    val parentData = measurable.parentData as? ColumnParentData
                    if (parentData?.weight == null) {
                        sum += measurable.minIntrinsicHeight(width)
                        if (index > 0) {
                            spacing += verticalArrangement.spacing
                        }
                    }
                }
                return sum + spacing
            }

            override fun MeasureScope.maxIntrinsicHeight(measurables: List<Measurable>, width: Int): Int {
                var sum = 0
                var spacing = 0
                measurables.forEachIndexed { index, measurable ->
                    val parentData = measurable.parentData as? ColumnParentData
                    if (parentData?.weight == null) {
                        sum += measurable.maxIntrinsicHeight(width)
                        if (index > 0) {
                            spacing += verticalArrangement.spacing
                        }
                    }
                }
                return sum + spacing
            }
        },
        content = {
            ColumnScope.content()
        }
    )
}
