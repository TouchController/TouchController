package top.fifthlight.touchcontroller.common.ui.config.tab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.navigator.LocalNavigator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import top.fifthlight.combine.data.Text
import top.fifthlight.combine.layout.Arrangement
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.placement.fillMaxSize
import top.fifthlight.combine.modifier.placement.padding
import top.fifthlight.combine.modifier.scroll.verticalScroll
import top.fifthlight.combine.widget.layout.Column
import top.fifthlight.combine.widget.ui.Button
import top.fifthlight.combine.widget.ui.Switch
import top.fifthlight.combine.widget.ui.Text
import top.fifthlight.touchcontroller.assets.Texts
import top.fifthlight.touchcontroller.common.config.item.ItemList
import top.fifthlight.touchcontroller.common.gal.item.ItemSubclassProvider
import top.fifthlight.touchcontroller.common.gal.itemlist.DefaultItemListProvider
import top.fifthlight.touchcontroller.common.ui.component.screen.ComponentScreen
import top.fifthlight.touchcontroller.common.ui.config.model.ConfigScreenModel
import top.fifthlight.touchcontroller.common.ui.item.screen.ItemListScreen
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme
import top.fifthlight.touchcontroller.common.ui.widget.HorizontalPreferenceItem

class ItemTabs(
    private val configScreenModel: ConfigScreenModel,
) {
    val usableItemsTab = ItemTab(
        options = TabOptions(
            titleId = Texts.SCREEN_CONFIG_ITEM_USABLE_ITEMS_TITLE,
            group = TabGroup.ItemGroup,
            index = 0,
            onReset = { copy(item = item.copy(usableItems = DefaultItemListProvider.usableItems)) },
        ),
        value = configScreenModel.uiState.map { it.config.item.usableItems },
        onValueChanged = {
            configScreenModel.updateConfig { copy(item = item.copy(usableItems = it)) }
        }
    )

    val showCrosshairItemsTab = ItemTab(
        options = TabOptions(
            titleId = Texts.SCREEN_CONFIG_ITEM_SHOW_CROSSHAIR_ITEMS_TITLE,
            group = TabGroup.ItemGroup,
            index = 1,
            onReset = { copy(item = item.copy(showCrosshairItems = DefaultItemListProvider.showCrosshairItems)) },
        ),
        value = configScreenModel.uiState.map { it.config.item.showCrosshairItems },
        onValueChanged = {
            configScreenModel.updateConfig { copy(item = item.copy(showCrosshairItems = it)) }
        }
    )

    val crosshairAimingItemsTab = ItemTab(
        options = TabOptions(
            titleId = Texts.SCREEN_CONFIG_ITEM_CROSSHAIR_AIMING_ITEMS_TITLE,
            group = TabGroup.ItemGroup,
            index = 2,
            onReset = { copy(item = item.copy(crosshairAimingItems = DefaultItemListProvider.crosshairAimingItems)) },
        ),
        value = configScreenModel.uiState.map { it.config.item.crosshairAimingItems },
        onValueChanged = {
            configScreenModel.updateConfig { copy(item = item.copy(crosshairAimingItems = it)) }
        }
    )
}

class ItemTab(
    override val options: TabOptions,
    val value: Flow<ItemList>,
    val onValueChanged: (ItemList) -> Unit,
) : Tab() {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val value by value.collectAsState(null)
        Column(
            modifier = Modifier
                .padding(8)
                .verticalScroll(background = LocalTouchControllerTheme.current.background)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8),
        ) {
            value?.let { value ->
                HorizontalPreferenceItem(
                    title = Text.translatable(Texts.SCREEN_CONFIG_ITEM_WHITELIST_TITLE),
                    description = Text.translatable(Texts.SCREEN_CONFIG_ITEM_WHITELIST_DESCRIPTION),
                ) {
                    Button(
                        onClick = {
                            navigator?.push(
                                ItemListScreen(
                                    initialValue = value.whitelist,
                                    onValueChanged = { onValueChanged(value.copy(whitelist = it)) },
                                )
                            )
                        }
                    ) {
                        Text(Text.translatable(Texts.SCREEN_CONFIG_ITEM_EDIT_TITLE))
                    }
                }
                HorizontalPreferenceItem(
                    title = Text.translatable(Texts.SCREEN_CONFIG_ITEM_BLACKLIST_TITLE),
                    description = Text.translatable(Texts.SCREEN_CONFIG_ITEM_BLACKLIST_DESCRIPTION),
                ) {
                    Button(
                        onClick = {
                            navigator?.push(
                                ItemListScreen(
                                    initialValue = value.blacklist,
                                    onValueChanged = { onValueChanged(value.copy(blacklist = it)) },
                                )
                            )
                        }
                    ) {
                        Text(Text.translatable(Texts.SCREEN_CONFIG_ITEM_EDIT_TITLE))
                    }
                }
                HorizontalPreferenceItem(
                    title = Text.translatable(Texts.SCREEN_CONFIG_ITEM_COMPONENT_TITLE),
                    description = Text.translatable(Texts.SCREEN_CONFIG_ITEM_COMPONENT_DESCRIPTION),
                ) {
                    Button(
                        onClick = {
                            navigator?.push(
                                ComponentScreen(
                                    initialValue = value.components,
                                    onValueChanged = { onValueChanged(value.copy(components = it)) }
                                )
                            )
                        }
                    ) {
                        Text(Text.translatable(Texts.SCREEN_CONFIG_ITEM_EDIT_TITLE))
                    }
                }
                for (subclass in ItemSubclassProvider.itemSubclasses) {
                    HorizontalPreferenceItem(
                        title = subclass.name,
                    ) {
                        Switch(
                            value = value.subclasses.contains(subclass),
                            onValueChanged = {
                                if (it) {
                                    onValueChanged(value.copy(subclasses = value.subclasses.add(subclass)))
                                } else {
                                    onValueChanged(value.copy(subclasses = value.subclasses.remove(subclass)))
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
