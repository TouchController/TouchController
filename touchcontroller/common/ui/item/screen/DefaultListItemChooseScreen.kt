package top.fifthlight.touchcontroller.common.ui.item.screen

import androidx.compose.runtime.*
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.collections.immutable.toPersistentList
import top.fifthlight.combine.data.Text
import top.fifthlight.combine.item.data.Item
import top.fifthlight.combine.item.data.ItemStackFactory
import top.fifthlight.combine.item.widget.ItemGrid
import top.fifthlight.combine.layout.Arrangement
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.drawing.border
import top.fifthlight.combine.modifier.placement.fillMaxWidth
import top.fifthlight.combine.modifier.placement.padding
import top.fifthlight.combine.widget.layout.Column
import top.fifthlight.combine.widget.ui.EditText
import top.fifthlight.touchcontroller.assets.Texts
import top.fifthlight.touchcontroller.common.gal.item.ItemProviderFactory
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme

class DefaultListItemChooseScreen(
    val onItemSelected: (Item) -> Unit,
) : Screen {
    private val allItems = ItemProviderFactory.of().allItems

    @Composable
    override fun Content() {
        var searchText by remember { mutableStateOf("") }
        val allItems = allItems.map {
            Pair(it, ItemStackFactory.create(it, 1))
        }.toPersistentList()
        val showingItems = remember(searchText, allItems) {
            if (searchText.isEmpty()) {
                allItems
            } else {
                allItems.filter { (_, stack) ->
                    stack.name.string.contains(searchText, ignoreCase = true)
                }.toPersistentList()
            }
        }
        Column(
            modifier = Modifier
                .padding(4)
                .border(LocalTouchControllerTheme.current.borderBackgroundDark),
            verticalArrangement = Arrangement.spacedBy(8),
        ) {
            EditText(
                modifier = Modifier.fillMaxWidth(),
                placeholder = Text.translatable(Texts.SCREEN_ITEM_LIST_SEARCH_PLACEHOLDER),
                value = searchText,
                onValueChanged = { searchText = it }
            )

            ItemGrid(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                stacks = showingItems,
                onStackClicked = { item, stack -> onItemSelected(item) },
            )
        }
    }
}
