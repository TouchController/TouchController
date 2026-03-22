package top.fifthlight.combine.widget.layout

import androidx.compose.runtime.Composable
import top.fifthlight.combine.layout.Layout
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.data.IntOffset
import kotlin.math.max

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    maxColumns: Int = Int.MAX_VALUE,
    horizontalSpacing: Int = 0,
    expandColumnWidth: Boolean = false,
    content: @Composable () -> Unit = {},
) {
    Layout(
        modifier = modifier,
        measurePolicy = { measurables, constraints ->
            val childConstraint = if (expandColumnWidth) {
                val width = (constraints.maxWidth - (maxColumns - 1) * horizontalSpacing) / maxColumns
                constraints.copy(
                    minWidth = width,
                    maxWidth = width,
                    minHeight = 0,
                )
            } else {
                constraints.copy(minWidth = 0, minHeight = 0)
            }

            val childPositions = Array(measurables.size) { IntOffset.ZERO }
            var cursorPosition = IntOffset(0, 0)
            var maxWidth = 0
            var rowMaxHeight = 0
            var column = 0
            var row = 0

            val placeables = measurables.mapIndexed { index, measurable ->
                val placeable = measurable.measure(childConstraint)
                val spacing = if (column < maxColumns - 1) {
                    horizontalSpacing
                } else {
                    0
                }
                if (placeable.width + cursorPosition.left + spacing > constraints.maxWidth || column >= maxColumns) {
                    // Break line
                    cursorPosition = IntOffset(0, cursorPosition.y + rowMaxHeight)
                    rowMaxHeight = 0
                    column = 0
                    row++
                }
                column++
                childPositions[index] = cursorPosition
                cursorPosition = IntOffset(cursorPosition.x + placeable.width + horizontalSpacing, cursorPosition.y)
                maxWidth = max(maxWidth, cursorPosition.x)
                rowMaxHeight = max(rowMaxHeight, placeable.height)
                placeable
            }

            val width = maxWidth.coerceIn(constraints.minWidth, constraints.maxWidth)
            val height = (cursorPosition.y + rowMaxHeight).coerceIn(constraints.minHeight, constraints.maxHeight)

            layout(width, height) {
                placeables.forEachIndexed { index, placeable ->
                    placeable.placeAt(childPositions[index])
                }
            }
        },
        content = {
            content()
        }
    )
}
