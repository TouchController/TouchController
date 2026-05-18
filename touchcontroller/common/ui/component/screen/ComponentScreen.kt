/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.component.screen

import androidx.compose.runtime.*
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.collections.immutable.PersistentList
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
import top.fifthlight.combine.widget.ui.EditText
import top.fifthlight.combine.widget.ui.Icon
import top.fifthlight.combine.widget.ui.IconButton
import top.fifthlight.combine.widget.ui.Text
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.assets.texture.Textures
import top.fifthlight.touchcontroller.common.gal.gamestate.GameState
import top.fifthlight.touchcontroller.common.gal.item.ItemDataComponentType
import top.fifthlight.touchcontroller.common.gal.item.ItemDataComponentTypeProviderFactory
import top.fifthlight.touchcontroller.common.ui.component.model.ComponentScreenModel
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme
import top.fifthlight.touchcontroller.common.ui.widget.ItemShower
import top.fifthlight.touchcontroller.common.ui.widget.ListButton
import top.fifthlight.touchcontroller.common.ui.widget.Scaffold
import top.fifthlight.touchcontroller.common.ui.widget.navigation.AppBar
import top.fifthlight.touchcontroller.common.ui.widget.navigation.BackButton

class ComponentScreen(
    private val initialValue: PersistentList<ItemDataComponentType>,
    private val onValueChanged: (PersistentList<ItemDataComponentType>) -> Unit,
) : Screen {
    @Composable
    override fun Content() {
        val screenModel: ComponentScreenModel =
            rememberScreenModel { ComponentScreenModel(initialValue, onValueChanged) }
        DisposableEffect(screenModel) {
            onDispose {
                screenModel.onDispose()
            }
        }

        Scaffold(
            topBar = {
                AppBar(
                    modifier = Modifier.fillMaxWidth(),
                    leading = {
                        BackButton(
                            screenName = Text.translatable(Texts.SCREEN_COMPONENT_LIST_TITLE),
                        )
                    },
                )
            },
        ) { modifier ->
            if (GameState.inGame) {
                Row(modifier) {
                    val items by screenModel.value.collectAsState()
                    Column(
                        modifier = Modifier
                            .padding(2)
                            .verticalScroll()
                            .border(LocalTouchControllerTheme.current.borderBackgroundDark)
                            .fillMaxHeight()
                            .weight(1f),
                    ) {
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
                                    val items = remember(item) { item.allItems }
                                    ItemShower(items = items)
                                    Text(
                                        modifier = Modifier.weight(1f),
                                        text = item.id.toString()
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
                    Column(
                        modifier = Modifier
                            .padding(4)
                            .border(LocalTouchControllerTheme.current.borderBackgroundDark)
                            .fillMaxHeight()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4),
                    ) {
                        var searchText by remember { mutableStateOf("") }

                        EditText(
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = Text.translatable(Texts.SCREEN_COMPONENT_LIST_SEARCH_PLACEHOLDER),
                            value = searchText,
                            onValueChanged = { searchText = it }
                        )

                        Column(
                            modifier = Modifier
                                .verticalScroll()
                                .weight(1f)
                                .fillMaxWidth(),
                        ) {
                            val dataComponentTypes =
                                remember(Unit) { ItemDataComponentTypeProviderFactory.of().allComponents }
                            val showingTypes = remember(dataComponentTypes, searchText, items) {
                                dataComponentTypes.filter {
                                    if (it in items) {
                                        return@filter false
                                    }
                                    if (searchText.isNotEmpty() && !it.id.toString()
                                            .contains(searchText, ignoreCase = true)
                                    ) {
                                        return@filter false
                                    }
                                    true
                                }.toPersistentList()
                            }
                            for (item in showingTypes) {
                                ListButton(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = { screenModel.addItem(item) },
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        val items = remember(item) { item.allItems }
                                        ItemShower(items = items)
                                        Text(
                                            modifier = Modifier.weight(1f),
                                            text = item.id.toString()
                                        )
                                        Text(Text.translatable(Texts.SCREEN_COMPONENT_LIST_ADD))
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .border(LocalTouchControllerTheme.current.borderBackgroundDark)
                        .fillMaxSize(),
                    alignment = Alignment.Center,
                ) {
                    Text(Text.translatable(Texts.SCREEN_COMPONENT_LIST_WARNING_NOT_IN_GAME))
                }
            }
        }
    }
}
