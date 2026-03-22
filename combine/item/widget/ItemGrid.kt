package top.fifthlight.combine.item.widget

import androidx.compose.runtime.*
import kotlinx.collections.immutable.PersistentList
import top.fifthlight.combine.item.data.Item
import top.fifthlight.combine.item.data.ItemStack
import top.fifthlight.combine.item.paint.item
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.pointer.clickableWithOffset
import top.fifthlight.combine.modifier.pointer.hoverableWithOffset
import top.fifthlight.combine.modifier.scroll.rememberScrollState
import top.fifthlight.combine.modifier.scroll.verticalScroll
import top.fifthlight.combine.paint.Colors
import top.fifthlight.combine.paint.Drawable
import top.fifthlight.combine.theme.LocalTheme
import top.fifthlight.combine.util.math.ceilDiv
import top.fifthlight.combine.widget.Canvas
import top.fifthlight.data.IntOffset
import top.fifthlight.data.IntRect
import top.fifthlight.data.IntSize

@JvmName("ItemStackGrid")
@Composable
fun ItemGrid(
    modifier: Modifier = Modifier,
    stacks: PersistentList<Pair<Item, ItemStack>>,
    background: Drawable? = LocalTheme.current.drawables.itemGridBackground,
    onStackClicked: (Item, ItemStack) -> Unit = { _, _ -> },
) {
    val gridSize = 18
    val iconSize = 16
    val iconOffset = (gridSize - iconSize) / 2

    val scrollState = rememberScrollState()

    fun calculateSize(itemCount: Int, width: Int): IntSize {
        val columns = width / gridSize

        return if (itemCount < columns) {
            IntSize(itemCount, 1)
        } else if (columns <= 0) {
            IntSize(0, 0)
        } else {
            val rows = itemCount ceilDiv columns
            IntSize(columns, rows)
        }
    }

    val scrollPosition by scrollState.progress.collectAsState()
    var width by remember { mutableIntStateOf(0) }
    var hoverPosition by remember { mutableStateOf<IntOffset?>(null) }
    Canvas(
        modifier = modifier
            .clickableWithOffset { position ->
                val size = calculateSize(stacks.size, width)
                val gridPosition = position.toIntOffset() / gridSize
                val index = gridPosition.y * size.width + gridPosition.x
                val (item, stack) = stacks.getOrNull(index) ?: return@clickableWithOffset
                onStackClicked(item, stack)
            }
            .hoverableWithOffset { hovered, position ->
                hoverPosition = when (hovered) {
                    true -> position.toIntOffset() / gridSize
                    false -> null
                    null -> if (hoverPosition == null) {
                        null
                    } else {
                        position.toIntOffset() / gridSize
                    }
                }
            }
            .verticalScroll(scrollState),
        measurePolicy = { _, constraints ->
            width = constraints.maxWidth
            val size = if (constraints.maxWidth == Int.MAX_VALUE) {
                IntSize(stacks.size * gridSize, gridSize)
            } else {
                calculateSize(stacks.size, constraints.maxWidth) * gridSize
            }
            layout(size) {}
        },
    ) { canvas, node ->
        val (columns, rows) = calculateSize(stacks.size, node.width)
        background?.draw(
            canvas = canvas,
            dstRect = IntRect(
                offset = IntOffset.ZERO,
                size = IntSize(
                    width = columns * gridSize,
                    height = rows * gridSize,
                ),
            )
        )

        val rowRange = scrollPosition / gridSize until ((scrollPosition + scrollState.viewportHeight) ceilDiv gridSize)
        canvas.item { canvas ->
            for (y in rowRange) {
                for (x in 0 until columns) {
                    val index = columns * y + x
                    val (_, stack) = stacks.getOrNull(index) ?: break
                    val offset = IntOffset(x, y) * gridSize
                    hoverPosition?.let { position ->
                        if (position.x == x && position.y == y) {
                            canvas.fillRect(
                                offset = offset + iconOffset,
                                size = IntSize(iconSize),
                                color = Colors.TRANSPARENT_WHITE,
                            )
                        }
                    }
                    canvas.drawItemStack(
                        offset = offset + iconOffset,
                        stack = stack,
                    )
                }
            }
        }
    }
}
