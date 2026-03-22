package top.fifthlight.touchcontroller.common.ui.item.screen

import androidx.compose.runtime.*
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import top.fifthlight.combine.data.Text
import top.fifthlight.combine.item.data.Item
import top.fifthlight.combine.item.data.ItemStack
import top.fifthlight.combine.item.widget.Item
import top.fifthlight.combine.item.widget.ItemButton
import top.fifthlight.combine.item.widget.ItemGrid
import top.fifthlight.combine.layout.Alignment
import top.fifthlight.combine.layout.Arrangement
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.drawing.background
import top.fifthlight.combine.modifier.drawing.border
import top.fifthlight.combine.modifier.placement.fillMaxHeight
import top.fifthlight.combine.modifier.placement.fillMaxWidth
import top.fifthlight.combine.modifier.placement.padding
import top.fifthlight.combine.modifier.placement.width
import top.fifthlight.combine.modifier.scroll.verticalScroll
import top.fifthlight.combine.theme.LocalTheme
import top.fifthlight.combine.widget.layout.Box
import top.fifthlight.combine.widget.layout.Column
import top.fifthlight.combine.widget.layout.FlowRow
import top.fifthlight.combine.widget.layout.Row
import top.fifthlight.combine.widget.ui.EditText
import top.fifthlight.combine.widget.ui.Text
import top.fifthlight.data.IntPadding
import top.fifthlight.data.IntSize
import top.fifthlight.touchcontroller.assets.Texts
import top.fifthlight.touchcontroller.common.gal.player.PlayerInventory
import top.fifthlight.touchcontroller.common.gal.creativetab.CreativeTabsProvider
import top.fifthlight.touchcontroller.common.ui.widget.ListButton
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme

class CreativeTabItemChooseScreen(
    private val onItemSelected: (Item) -> Unit,
    private val tabs: PersistentList<CreativeTabsProvider.CreativeTab>,
    private val playerInventory: PlayerInventory,
) : Screen {
    @Composable
    override fun Content() {
        var selectedTabIndex by remember { mutableIntStateOf(0) }
        val selectedTab = tabs.getOrNull(selectedTabIndex)
        Row {
            selectedTab?.let { tab ->
                Column(
                    modifier = Modifier
                        .padding(4)
                        .border(LocalTouchControllerTheme.current.borderBackgroundDark)
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(4),
                ) {
                    val itemStacks = tab.items.map { Pair(it.item, it) }.toPersistentList()
                    Text(selectedTab.name)

                    when (selectedTab.type) {
                        CreativeTabsProvider.CreativeTab.Type.CATEGORY -> {
                            ItemGrid(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                stacks = itemStacks,
                                onStackClicked = { item, stack -> onItemSelected(item) }
                            )
                        }

                        CreativeTabsProvider.CreativeTab.Type.SEARCH -> {
                            var searchText by remember { mutableStateOf("") }
                            val showingItems = remember(searchText, itemStacks) {
                                if (searchText.isEmpty()) {
                                    itemStacks
                                } else {
                                    itemStacks.filter { (_, stack) ->
                                        stack.name.string.contains(searchText, ignoreCase = true)
                                    }.toPersistentList()
                                }
                            }

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
                                onStackClicked = { item, stack -> onItemSelected(item) }
                            )
                        }

                        CreativeTabsProvider.CreativeTab.Type.SURVIVAL_INVENTORY -> {
                            Box(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                alignment = Alignment.Center,
                            ) {
                                val backpackBackground = LocalTheme.current.drawables.itemGridBackground
                                val backpackBackgroundModifier = backpackBackground?.let {
                                    Modifier.background(it)
                                } ?: Modifier
                                val backpackBackgroundSize = backpackBackground?.size ?: IntSize(18)
                                val padding = (backpackBackgroundSize.width - 16) / 2
                                Column(
                                    modifier = Modifier.width(backpackBackgroundSize.width * 9),
                                    verticalArrangement = Arrangement.spacedBy(4, Alignment.CenterVertically),
                                ) {
                                    @Composable
                                    fun Item(
                                        itemStack: ItemStack,
                                        modifier: Modifier = Modifier,
                                    ) {
                                        ItemButton(
                                            modifier = Modifier
                                                .padding(padding)
                                                .then(modifier),
                                            onClick = { onItemSelected(itemStack.item) },
                                            itemStack = itemStack,
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Column {
                                            Text(Text.translatable(Texts.SCREEN_ITEM_LIST_INVENTORY_ARMOR))
                                            Row(modifier = backpackBackgroundModifier) {
                                                for (index in 0 until 4) {
                                                    Item(playerInventory.armor[index])
                                                }
                                            }
                                        }
                                        playerInventory.offHand?.let { offHand ->
                                            Column {
                                                Text(Text.translatable(Texts.SCREEN_ITEM_LIST_INVENTORY_OFFHAND))
                                                Item(
                                                    modifier = backpackBackgroundModifier,
                                                    itemStack = offHand
                                                )
                                            }
                                        }
                                    }

                                    Column {
                                        Text(Text.translatable(Texts.SCREEN_ITEM_LIST_INVENTORY_MAIN))
                                        for (y in 0 until 4) {
                                            Row(modifier = backpackBackgroundModifier) {
                                                for (x in 0 until 9) {
                                                    val index = y * 9 + x
                                                    Item(playerInventory.main[index])
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } ?: run {
                Box(
                    modifier = Modifier
                        .padding(4)
                        .weight(1f)
                        .border(LocalTouchControllerTheme.current.borderBackgroundDark)
                        .fillMaxHeight(),
                ) {
                    Text(Text.translatable(Texts.SCREEN_ITEM_LIST_NO_TAB_SELECTED))
                }
            }

            FlowRow(
                modifier = Modifier
                    .padding(4)
                    .verticalScroll()
                    .border(LocalTouchControllerTheme.current.borderBackgroundDark)
                    .fillMaxHeight(),
                maxColumns = 2,
            ) {
                for ((index, tab) in tabs.withIndex()) {
                    ListButton(
                        checked = selectedTabIndex == index,
                        minSize = IntSize(16, 16),
                        padding = IntPadding.ZERO,
                        onClick = { selectedTabIndex = index }
                    ) {
                        Item(itemStack = tab.icon)
                    }
                }
            }
        }
    }
}
