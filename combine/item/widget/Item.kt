package top.fifthlight.combine.item.widget

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import top.fifthlight.combine.item.data.Item
import top.fifthlight.combine.item.data.ItemStack
import top.fifthlight.combine.item.paint.item
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.placement.size
import top.fifthlight.combine.widget.Canvas
import top.fifthlight.data.IntOffset
import top.fifthlight.data.IntSize

@Composable
fun Item(
    modifier: Modifier = Modifier,
    size: Int = 16,
    item: Item?,
) = Item(modifier, size, remember(item) { item?.toStack() })

@Composable
fun Item(
    modifier: Modifier = Modifier,
    size: Int = 16,
    itemStack: ItemStack?,
) {
    Canvas(
        modifier = Modifier
            .size(size)
            .then(modifier),
    ) { canvas, _ ->
        canvas.item { canvas ->
            if (itemStack != null) {
                canvas.drawItemStack(IntOffset.ZERO, IntSize(size), itemStack)
            }
        }
    }
}
