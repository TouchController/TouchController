package top.fifthlight.combine.widget.layout

import androidx.compose.runtime.Composable
import top.fifthlight.combine.layout.Alignment
import top.fifthlight.combine.layout.Layout
import top.fifthlight.combine.layout.constraints.Constraints
import top.fifthlight.combine.layout.measure.Measurable
import top.fifthlight.combine.layout.measure.MeasurePolicy
import top.fifthlight.combine.layout.measure.MeasureResult
import top.fifthlight.combine.layout.measure.MeasureScope
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.ParentDataModifierNode
import top.fifthlight.data.IntSize

interface BoxScope {
    fun Modifier.alignment(alignment: Alignment) = then(BoxWeightModifier(alignment))

    companion object : BoxScope
}

private data class BoxParentData(
    val alignment: Alignment,
)

private data class BoxWeightModifier(
    val alignment: Alignment,
) : ParentDataModifierNode, Modifier.Node<BoxWeightModifier> {
    override fun modifierParentData(parentData: Any?): BoxParentData {
        val data = parentData as? BoxParentData
        if (data != null) {
            return data
        }
        return BoxParentData(alignment)
    }
}

@Composable
fun Box(
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.TopLeft,
    content: @Composable BoxScope.() -> Unit = {},
) {
    Layout(
        modifier = modifier,
        measurePolicy = object : MeasurePolicy {
            override fun MeasureScope.measure(measurables: List<Measurable>, constraints: Constraints): MeasureResult {
                val childConstraint = constraints.copy(minWidth = 0, minHeight = 0)
                val placeables = measurables.map { it.measure(childConstraint) }

                val width =
                    (placeables.maxOfOrNull { it.width } ?: 0).coerceIn(constraints.minWidth, constraints.maxWidth)
                val height =
                    (placeables.maxOfOrNull { it.height } ?: 0).coerceIn(constraints.minHeight, constraints.maxHeight)

                return layout(width, height) {
                    placeables.forEachIndexed { index, placeable ->
                        val measurable = measurables[index]
                        val parentData = measurable.parentData as? BoxParentData
                        val placeableAlignment = parentData?.alignment ?: alignment
                        val position = placeableAlignment.align(
                            IntSize(placeable.width, placeable.height),
                            IntSize(width, height)
                        )
                        placeable.placeAt(position.x, position.y)
                    }
                }
            }

            override fun MeasureScope.minIntrinsicWidth(measurables: List<Measurable>, height: Int): Int =
                measurables.maxOfOrNull {
                    it.minIntrinsicWidth(height)
                } ?: 0

            override fun MeasureScope.maxIntrinsicWidth(measurables: List<Measurable>, height: Int): Int =
                measurables.maxOfOrNull {
                    it.maxIntrinsicWidth(height)
                } ?: 0

            override fun MeasureScope.minIntrinsicHeight(measurables: List<Measurable>, width: Int): Int =
                measurables.maxOfOrNull {
                    it.minIntrinsicHeight(width)
                } ?: 0

            override fun MeasureScope.maxIntrinsicHeight(measurables: List<Measurable>, width: Int): Int =
                measurables.maxOfOrNull {
                    it.maxIntrinsicHeight(width)
                } ?: 0
        },
        content = {
            BoxScope.content()
        }
    )
}
