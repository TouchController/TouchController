package top.fifthlight.touchcontroller.common.ui.widget

import androidx.compose.runtime.*
import kotlinx.collections.immutable.PersistentList
import kotlinx.coroutines.delay
import top.fifthlight.combine.item.data.Item
import top.fifthlight.combine.item.widget.Item
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.placement.size
import top.fifthlight.combine.widget.layout.Spacer

@Composable
fun ItemShower(
    modifier: Modifier = Modifier,
    items: PersistentList<Item>?,
) {
    if (items != null) {
        var currentItem by remember { mutableStateOf(items.randomOrNull()) }
        LaunchedEffect(items) {
            while (true) {
                delay(1000)
                currentItem = items.randomOrNull()
            }
        }
        Item(
            modifier = modifier,
            itemStack = currentItem?.toStack()
        )
    } else {
        Spacer(modifier = Modifier.size(16))
    }
}
