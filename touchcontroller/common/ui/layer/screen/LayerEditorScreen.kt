package top.fifthlight.touchcontroller.common.ui.layer.screen

import androidx.compose.runtime.*
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.StateFlow
import top.fifthlight.combine.data.Identifier
import top.fifthlight.combine.data.Text
import top.fifthlight.combine.item.data.ItemStackFactory
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
import top.fifthlight.combine.widget.layout.Spacer
import top.fifthlight.combine.widget.ui.*
import top.fifthlight.touchcontroller.assets.Texts
import top.fifthlight.touchcontroller.assets.Textures
import top.fifthlight.touchcontroller.common.config.condition.BuiltinLayerConditionKey
import top.fifthlight.touchcontroller.common.config.condition.CustomLayerConditionKey
import top.fifthlight.touchcontroller.common.config.condition.HoldingItemLayerConditionKey
import top.fifthlight.touchcontroller.common.config.condition.LayerConditions
import top.fifthlight.touchcontroller.common.config.condition.RidingEntityLayerConditionKey
import top.fifthlight.touchcontroller.common.config.condition.SelectEntityLayerConditionKey
import top.fifthlight.touchcontroller.common.config.layout.LayoutLayer
import top.fifthlight.touchcontroller.common.config.preset.LayoutPreset
import top.fifthlight.touchcontroller.common.config.preset.info.LayerCustomConditions
import top.fifthlight.touchcontroller.common.ui.layer.model.LayerEditorScreenModel
import top.fifthlight.touchcontroller.common.ui.layer.tab.LayerConditionTab
import top.fifthlight.touchcontroller.common.ui.layer.tab.LayerConditionTabContext
import top.fifthlight.touchcontroller.common.ui.layer.tab.LocalLayerConditionTabContext
import top.fifthlight.touchcontroller.common.ui.layer.tab.all.allLayerConditionTabs
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme
import top.fifthlight.touchcontroller.common.ui.theme.TouchControllerTheme
import top.fifthlight.touchcontroller.common.ui.widget.*
import top.fifthlight.touchcontroller.common.ui.widget.navigation.AppBar
import top.fifthlight.touchcontroller.common.ui.widget.navigation.BackButton
import top.fifthlight.touchcontroller.common.ui.widget.navigation.TouchControllerNavigator

@Composable
private fun LayerConditionItem(
    preset: LayoutPreset?,
    item: LayerConditions.Item,
    onValueChanged: (LayerConditions.Value) -> Unit,
    onItemRemoved: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
    ) {
        Row(
            modifier = Modifier
                .border(LocalTouchControllerTheme.current.listButtonDrawablesUnchecked.normal)
                .fillMaxHeight()
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4),
        ) {
            when (val key = item.key) {
                is BuiltinLayerConditionKey -> {
                    Text(Text.translatable(key.condition.text))
                }

                is CustomLayerConditionKey -> {
                    preset?.controlInfo?.customConditions?.conditions?.firstOrNull { it.uuid == key.key }?.let {
                        Text(it.name?.let { Text.literal(it) }
                            ?: Text.translatable(Texts.SCREEN_LAYER_EDITOR_CUSTOM_CONDITION_UNNAMED))
                    } ?: Text(Text.translatable(Texts.SCREEN_LAYER_EDITOR_CUSTOM_CONDITION_UNKNOWN))
                }

                is HoldingItemLayerConditionKey -> {
                    val stack = remember(key.item) { ItemStackFactory.create(key.item, 1) }
                    Item(item = key.item)
                    Text(stack.name)
                }

                is RidingEntityLayerConditionKey -> {
                    Text(
                        Text.format(
                            Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_RIDING_ENTITY_ITEM,
                            key.entityType.name
                        )
                    )
                }

                is SelectEntityLayerConditionKey -> {
                    Text(
                        Text.format(
                            Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_SELECTING_ENTITY_ITEM,
                            key.entityType.name
                        )
                    )
                }
            }
        }

        var expanded by remember { mutableStateOf(false) }
        Select(
            modifier = Modifier.fillMaxHeight(),
            expanded = expanded,
            onExpandedChanged = { expanded = it },
            dropDownContent = {
                DropdownItemList(
                    modifier = Modifier.verticalScroll(),
                    onItemSelected = { expanded = false },
                    items = LayerConditions.Value.entries.map {
                        Pair(Text.translatable(it.text)) {
                            onValueChanged(it)
                        }
                    }.toPersistentList(),
                )
            }
        ) {
            Text(Text.translatable(item.value.text))
            SelectIcon(expanded = expanded)
        }

        IconButton(
            modifier = Modifier.fillMaxHeight(),
            onClick = onItemRemoved,
        ) {
            Icon(Textures.icon_delete)
        }
    }
}

class LayerEditorScreen(
    private val screenName: Identifier,
    private val preset: StateFlow<LayoutPreset?>,
    private val onCustomConditionsChanged: (LayerCustomConditions) -> Unit,
    private val initialValue: LayoutLayer,
    private val onValueChanged: (LayoutLayer) -> Unit,
) : Screen {
    @Composable
    override fun Content() {
        val screenModel = rememberScreenModel { LayerEditorScreenModel(initialValue, onValueChanged) }
        val uiState by screenModel.uiState.collectAsState()
        val navigator = LocalNavigator.current
        val preset by preset.collectAsState()
        var innerNavigator by remember { mutableStateOf<Navigator?>(null) }
        TouchControllerTheme {
            Scaffold(
                topBar = {
                    AppBar(
                        modifier = Modifier.fillMaxWidth(),
                        leading = {
                            BackButton(screenName = Text.translatable(screenName))
                        },
                        trailing = {
                            Button(
                                onClick = {
                                    screenModel.applyChanges()
                                    navigator?.pop()
                                }
                            ) {
                                Text(Text.translatable(Texts.SCREEN_LAYER_EDITOR_SAVE))
                            }
                        },
                    )
                },
            ) { modifier ->
                Column(modifier = modifier) {
                    Row(
                        modifier = Modifier.height(IntrinsicSize.Min),
                    ) {
                        EditText(
                            modifier = Modifier.weight(1f),
                            value = uiState.name,
                            onValueChanged = screenModel::editName,
                            placeholder = Text.translatable(Texts.SCREEN_LAYER_EDITOR_NAME_PLACEHOLDER),
                        )
                        Row {
                            innerNavigator?.let { innerNavigator ->
                                for (tab in allLayerConditionTabs) {
                                    if (tab == innerNavigator.lastItem) {
                                        ListButton(
                                            modifier = Modifier
                                                .minWidth(100)
                                                .fillMaxHeight(),
                                            onClick = {},
                                        ) {
                                            Text(Text.translatable(tab.name))
                                        }
                                    } else {
                                        IconButton(
                                            modifier = Modifier.fillMaxHeight(),
                                            onClick = {
                                                innerNavigator.replace(tab)
                                            },
                                        ) {
                                            tab.Icon()
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.weight(1f),
                    ) {
                        if (uiState.conditions.conditions.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .border(LocalTouchControllerTheme.current.borderBackgroundDark)
                                    .weight(.4f)
                                    .fillMaxHeight(),
                            ) {
                                Text(
                                    modifier = Modifier.alignment(Alignment.Center),
                                    text = Text.translatable(Texts.SCREEN_LAYER_EDITOR_CONDITION_EMPTY),
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .padding(4)
                                    .verticalScroll()
                                    .border(LocalTouchControllerTheme.current.borderBackgroundDark)
                                    .weight(.4f)
                                    .fillMaxHeight(),
                            ) {
                                for ((index, condition) in uiState.conditions.conditions.withIndex()) {
                                    LayerConditionItem(
                                        preset = preset,
                                        item = condition,
                                        onValueChanged = {
                                            screenModel.editCondition(index) {
                                                copy(value = it)
                                            }
                                        },
                                        onItemRemoved = {
                                            screenModel.removeCondition(index)
                                        },
                                    )
                                }
                            }
                        }
                        TouchControllerNavigator(allLayerConditionTabs.first()) { navigator ->
                            innerNavigator = navigator
                            val currentLayerConditionTabContext = LayerConditionTabContext(
                                preset = preset,
                                onCustomConditionsChanged = onCustomConditionsChanged,
                                onConditionAdded = {
                                    screenModel.addCondition(
                                        LayerConditions.Item(
                                            key = it,
                                            value = LayerConditions.Value.WANT,
                                        )
                                    )
                                },
                            )
                            CompositionLocalProvider(
                                LocalLayerConditionTabContext provides currentLayerConditionTabContext,
                            ) {
                                val needBorder by derivedStateOf {
                                    (navigator.lastItem as? LayerConditionTab)?.needBorder ?: true
                                }
                                val borderModifier = if (needBorder) {
                                    Modifier.border(LocalTouchControllerTheme.current.borderBackgroundDark)
                                } else {
                                    Modifier
                                }
                                Box(
                                    modifier = Modifier
                                        .then(borderModifier)
                                        .weight(.6f)
                                        .fillMaxHeight(),
                                    alignment = Alignment.Center,
                                ) {
                                    CurrentScreen()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
