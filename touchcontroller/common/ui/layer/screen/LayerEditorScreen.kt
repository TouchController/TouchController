/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.layer.screen

import androidx.compose.runtime.*
import kotlinx.collections.immutable.toPersistentList
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.layout.Alignment
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.drawing.border
import top.fifthlight.combine.core.modifier.placement.*
import top.fifthlight.combine.core.modifier.scroll.verticalScroll
import top.fifthlight.combine.core.widget.layout.Box
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.core.widget.layout.Row
import top.fifthlight.combine.item.data.ItemStackFactory
import top.fifthlight.combine.item.widget.Item
import top.fifthlight.combine.theme.blackstone.widget.ListButton
import top.fifthlight.combine.theme.blackstone.widget.LocalListButtonTheme
import top.fifthlight.combine.widget.*
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.assets.texture.Textures
import top.fifthlight.touchcontroller.common.config.condition.*
import top.fifthlight.touchcontroller.common.config.preset.LayoutPreset
import top.fifthlight.touchcontroller.common.ui.layer.component.LayerEditorComponent
import top.fifthlight.touchcontroller.common.ui.layer.tab.LayerConditionTabComponent
import top.fifthlight.touchcontroller.common.ui.layer.tab.builtin.BuiltInTab
import top.fifthlight.touchcontroller.common.ui.layer.tab.custom.CustomTab
import top.fifthlight.touchcontroller.common.ui.layer.tab.holdingitem.HoldingItemTab
import top.fifthlight.touchcontroller.common.ui.layer.tab.ridingentity.RidingEntityTab
import top.fifthlight.touchcontroller.common.ui.layer.tab.selectedentity.SelectingEntityTab
import top.fifthlight.touchcontroller.common.ui.state.subscribeAsState
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme
import top.fifthlight.touchcontroller.common.ui.theme.TouchControllerTheme
import top.fifthlight.touchcontroller.common.ui.widget.Scaffold
import top.fifthlight.touchcontroller.common.ui.widget.navigation.AppBar
import top.fifthlight.touchcontroller.common.ui.widget.navigation.BackButton
import kotlin.jvm.optionals.getOrNull

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
                .border(LocalListButtonTheme.current.drawableSet.off.normal)
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
                    preset?.controlInfo?.customConditions?.conditions?.firstOrNull { it.uuid == key.key }?.let { condition ->
                        Text(condition.name?.let { name -> Text.literal(name) }
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

@Composable
fun LayerEditorScreen(component: LayerEditorComponent) {
    TouchControllerTheme {
        val uiState by component.uiState.subscribeAsState()
        val presetOptional by component.preset.subscribeAsState()
        val preset = presetOptional.getOrNull()
        val pages by component.childPages.subscribeAsState()
        val active = pages.items[pages.selectedIndex]

        Scaffold(
            topBar = {
                AppBar(
                    modifier = Modifier.fillMaxWidth(),
                    leading = {
                        BackButton(
                            screenName = Text.translatable(component.screenName),
                            onClick = component::onBack,
                        )
                    },
                    trailing = {
                        Button(
                            onClick = component::applyChanges,
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
                        value = uiState.name.asString(),
                        onValueChanged = component::editName,
                        placeholder = Text.translatable(Texts.SCREEN_LAYER_EDITOR_NAME_PLACEHOLDER),
                    )
                    Row {
                        for ((index, page) in pages.items.withIndex()) {
                            val tab = page.configuration
                            if (index == pages.selectedIndex) {
                                ListButton(
                                    modifier = Modifier
                                        .minWidth(100)
                                        .fillMaxHeight(),
                                    onClick = {},
                                ) {
                                    Text(tab.title)
                                }
                            } else {
                                IconButton(
                                    modifier = Modifier.fillMaxHeight(),
                                    onClick = {
                                        component.selectTab(tab)
                                    },
                                ) {
                                    tab.Icon()
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
                                        component.editCondition(index) {
                                            copy(value = it)
                                        }
                                    },
                                    onItemRemoved = {
                                        component.removeCondition(index)
                                    },
                                )
                            }
                        }
                    }

                    val borderModifier = if (active.configuration.needBorder) {
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
                        when (val instance = active.instance) {
                            is LayerConditionTabComponent.BuiltIn -> BuiltInTab(instance)
                            is LayerConditionTabComponent.HoldingItem -> HoldingItemTab(instance)
                            is LayerConditionTabComponent.RidingEntity -> RidingEntityTab(instance)
                            is LayerConditionTabComponent.SelectingEntity -> SelectingEntityTab(instance)
                            is LayerConditionTabComponent.Custom -> CustomTab(instance.component)
                            null -> {}
                        }
                    }
                }
            }
        }
    }
}
