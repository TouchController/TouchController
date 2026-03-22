package top.fifthlight.touchcontroller.common.ui.item.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.collections.immutable.PersistentList
import top.fifthlight.combine.data.Text
import top.fifthlight.combine.item.data.Item
import top.fifthlight.combine.item.widget.Item
import top.fifthlight.combine.layout.Alignment
import top.fifthlight.combine.layout.Arrangement
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.drawing.border
import top.fifthlight.combine.modifier.placement.*
import top.fifthlight.combine.modifier.scroll.verticalScroll
import top.fifthlight.combine.widget.layout.Box
import top.fifthlight.combine.widget.layout.Column
import top.fifthlight.combine.widget.layout.Row
import top.fifthlight.combine.widget.ui.Icon
import top.fifthlight.combine.widget.ui.IconButton
import top.fifthlight.combine.widget.ui.Text
import top.fifthlight.touchcontroller.assets.Texts
import top.fifthlight.touchcontroller.assets.Textures
import top.fifthlight.touchcontroller.common.ui.widget.*
import top.fifthlight.touchcontroller.common.ui.widget.navigation.AppBar
import top.fifthlight.touchcontroller.common.ui.widget.navigation.BackButton
import top.fifthlight.touchcontroller.common.ui.item.model.ItemListScreenModel
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme

class ItemListScreen(
    private val initialValue: PersistentList<Item>,
    private val onValueChanged: (PersistentList<Item>) -> Unit,
) : Screen {
    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel { ItemListScreenModel(initialValue, onValueChanged) }
        Scaffold(
            topBar = {
                AppBar(
                    modifier = Modifier.fillMaxWidth(),
                    leading = {
                        BackButton(
                            screenName = Text.translatable(Texts.SCREEN_ITEM_LIST_TITLE),
                        )
                    },
                )
            },
        ) { modifier ->
            Row(modifier) {
                Column(
                    modifier = Modifier
                        .padding(2)
                        .verticalScroll()
                        .border(LocalTouchControllerTheme.current.borderBackgroundDark)
                        .fillMaxHeight()
                        .weight(.4f),
                ) {
                    val items by screenModel.value.collectAsState()
                    for ((index, item) in items.withIndex()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                modifier = Modifier
                                    .border(LocalTouchControllerTheme.current.listButtonDrawablesUnchecked.normal)
                                    .weight(1f)
                                    .fillMaxHeight(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4),
                            ) {
                                Item(item = item)
                                Text(
                                    modifier = Modifier.weight(1f),
                                    text = item.toStack().name,
                                )
                            }

                            IconButton(
                                modifier = Modifier.fillMaxHeight(),
                                onClick = { screenModel.removeItem(index) },
                            ) {
                                Icon(Textures.icon_delete)
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(.6f),
                ) {
                    ItemChooser(screenModel::addItem)
                }
            }
        }
    }
}
