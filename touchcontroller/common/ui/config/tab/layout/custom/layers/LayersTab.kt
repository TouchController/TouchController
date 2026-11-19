/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.layers

import androidx.compose.runtime.*
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.layout.Alignment
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.placement.*
import top.fifthlight.combine.core.modifier.scroll.verticalScroll
import top.fifthlight.combine.core.widget.layout.Box
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.core.widget.layout.Row
import top.fifthlight.combine.theme.blackstone.widget.AlertDialog
import top.fifthlight.combine.theme.blackstone.widget.ListButton
import top.fifthlight.combine.theme.blackstone.widget.LocalListButtonTheme
import top.fifthlight.combine.widget.*
import top.fifthlight.data.IntRect
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.assets.texture.Textures
import top.fifthlight.touchcontroller.common.config.layout.LayoutLayer
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.layers.component.LayersTabComponent
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.widget.SideBarScaffold
import top.fifthlight.touchcontroller.common.ui.layer.screen.LayerEditorScreen
import top.fifthlight.touchcontroller.common.ui.state.subscribeAsState
import kotlin.jvm.optionals.getOrNull

@Composable
private fun LayersList(
    modifier: Modifier = Modifier,
    listContent: PersistentList<LayoutLayer> = persistentListOf(),
    currentSelectedLayoutIndex: Int? = null,
    onLayerSelected: (Int, LayoutLayer) -> Unit = { _, _ -> },
    onLayerEdited: (Int, LayoutLayer) -> Unit = { _, _ -> },
    onLayerCopied: (Int, LayoutLayer) -> Unit = { _, _ -> },
    onLayerDeleted: (Int, LayoutLayer) -> Unit = { _, _ -> },
) {
    Column(modifier = modifier) {
        for ((index, layer) in listContent.withIndex()) {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                ListButton(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    checked = currentSelectedLayoutIndex == index,
                    onClick = {
                        onLayerSelected(index, layer)
                    },
                ) {
                    Text(
                        modifier = Modifier.alignment(Alignment.CenterLeft),
                        text = layer.name.getText(),
                    )
                }

                var expanded by remember { mutableStateOf(false) }
                var anchor by remember { mutableStateOf(IntRect.ZERO) }
                IconButton(
                    modifier = Modifier
                        .width(24)
                        .minHeight(24)
                        .fillMaxHeight()
                        .anchor { anchor = it },
                    drawableSet = LocalListButtonTheme.current.drawableSet.off,
                    onClick = {
                        expanded = true
                    },
                ) {
                    Icon(Textures.icon_menu)
                }

                DropDownMenu(
                    expanded = expanded,
                    anchor = anchor,
                    onDismissRequest = {
                        expanded = false
                    }
                ) {
                    DropdownItemList(
                        modifier = Modifier.verticalScroll(),
                        onItemSelected = { expanded = false },
                        items = persistentListOf(
                            Pair(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_EDIT)) {
                                onLayerEdited(index, layer)
                            },
                            Pair(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_COPY)) {
                                onLayerCopied(index, layer)
                            },
                            Pair(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_DELETE)) {
                                onLayerDeleted(index, layer)
                            },
                        ),
                    )
                }
            }
        }
    }
}

@Composable
fun LayersTabActions(component: LayersTabComponent) {
    val selectedPreset by component.selectedPreset.subscribeAsState()
    IconButton(
        enabled = selectedPreset.getOrNull() != null,
        onClick = component::openCreateLayerEditor,
    ) {
        Icon(Textures.icon_add)
    }
}

@Composable
fun LayersTab(component: LayersTabComponent) {
    val stack by component.childStack.subscribeAsState()
    when (val instance = stack.active.instance) {
        is LayersTabComponent.StackComponent.List -> LayersScreen(component)
        is LayersTabComponent.StackComponent.Editor -> LayerEditorScreen(instance.component)
    }
}

@Composable
private fun LayersScreen(component: LayersTabComponent) {
    val selectedPresetOptional by component.selectedPreset.subscribeAsState()
    val selectedPreset = selectedPresetOptional.getOrNull()
    val selectedLayerIndex by component.selectedLayerIndex.subscribeAsState()
    val dialog by component.dialog.subscribeAsState()

    AlertDialog(
        value = dialog.child?.instance,
        valueTransformer = { it as? LayersTabComponent.DialogComponent.Delete },
        onDismissRequest = component::closeDialog,
        actions = { state ->
            WarningButton(
                onClick = {
                    state.onDeleted()
                    component.closeDialog()
                },
            ) {
                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_DELETE_LAYER_DELETE))
            }
            Button(
                onClick = component::closeDialog,
            ) {
                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_DELETE_LAYER_CANCEL))
            }
        }
    ) { state ->
        Column(
            verticalArrangement = Arrangement.spacedBy(4),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_DELETE_LAYER_1))
            Text(Text.format(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_DELETE_LAYER_2, state.layerName))
        }
    }

    SideBarScaffold(
        modifier = Modifier.fillMaxSize(),
        title = {
            Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS))
        },
        actions = {
            if (selectedPreset != null) {
                val layerIndices = selectedPreset.layout.indices
                val index = selectedLayerIndex.takeIf { it in layerIndices }
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    enabled = index != null && index > 0,
                    onClick = {
                        index?.let { component.moveLayer(it, -1) }
                    }
                ) {
                    Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_MOVE_UP))
                }
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    enabled = index != null && index < layerIndices.last,
                    onClick = {
                        index?.let { component.moveLayer(it, 1) }
                    }
                ) {
                    Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_MOVE_DOWN))
                }
            }
        }
    ) {
        if (selectedPreset != null) {
            Column(
                modifier = Modifier
                    .verticalScroll()
                    .fillMaxSize()
            ) {
                LayersList(
                    modifier = Modifier
                        .padding(4)
                        .fillMaxWidth(),
                    listContent = selectedPreset.layout,
                    currentSelectedLayoutIndex = selectedLayerIndex,
                    onLayerSelected = { index, _ ->
                        component.selectLayer(index)
                    },
                    onLayerEdited = { index, _ ->
                        component.openEditLayerEditor(index)
                    },
                    onLayerCopied = { _, layer ->
                        component.copyLayer(layer)
                    },
                    onLayerDeleted = { index, _ ->
                        component.openDeleteLayerDialog(index)
                    },
                )
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                alignment = Alignment.Center,
            ) {
                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_NO_PRESET_SELECTED))
            }
        }
    }
}
