package top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.layers

import androidx.compose.runtime.*
import cafe.adriel.voyager.core.model.rememberScreenModel
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import top.fifthlight.combine.data.Text
import top.fifthlight.combine.layout.Alignment
import top.fifthlight.combine.layout.Arrangement
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.placement.*
import top.fifthlight.combine.modifier.scroll.verticalScroll
import top.fifthlight.combine.widget.layout.Box
import top.fifthlight.combine.widget.layout.Column
import top.fifthlight.combine.widget.layout.Row
import top.fifthlight.combine.widget.ui.*
import top.fifthlight.data.IntRect
import top.fifthlight.touchcontroller.assets.Texts
import top.fifthlight.touchcontroller.assets.Textures
import top.fifthlight.touchcontroller.common.config.layout.ControllerLayout
import top.fifthlight.touchcontroller.common.config.layout.LayoutLayer
import top.fifthlight.touchcontroller.common.ext.mapState
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.layers.model.LayersTabModel
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.layers.state.LayersTabState
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.state.CustomControlLayoutTabState
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.tab.CustomTab
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.tab.LocalCustomTabContext
import top.fifthlight.touchcontroller.common.ui.layer.screen.LayerEditorScreen
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme
import top.fifthlight.touchcontroller.common.ui.widget.ListButton

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
        for ((index, preset) in listContent.withIndex()) {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                ListButton(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    checked = currentSelectedLayoutIndex == index,
                    onClick = {
                        onLayerSelected(index, preset)
                    },
                ) {
                    Text(
                        modifier = Modifier.alignment(Alignment.CenterLeft),
                        text = preset.name,
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
                    drawableSet = LocalTouchControllerTheme.current.listButtonDrawablesUnchecked,
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
                                onLayerEdited(index, preset)
                            },
                            Pair(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_COPY)) {
                                onLayerCopied(index, preset)
                            },
                            Pair(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_DELETE)) {
                                onLayerDeleted(index, preset)
                            },
                        ),
                    )
                }
            }
        }
    }
}

object LayersTab : CustomTab() {
    @Composable
    override fun Icon() {
        Icon(Textures.icon_layer)
    }

    @Composable
    override fun Content() {
        val (screenModel, uiState, tabsButton, sideBarAtRight, parentNavigator) = LocalCustomTabContext.current
        val tabModel = rememberScreenModel { LayersTabModel(screenModel) }
        val tabState by tabModel.uiState.collectAsState()

        AlertDialog(
            value = tabState,
            valueTransformer = { tabState as? LayersTabState.Delete },
            onDismissRequest = { tabModel.clearState() },
            action = { state ->
                WarningButton(
                    onClick = {
                        screenModel.deleteLayer(state.index)
                        tabModel.clearState()
                    },
                ) {
                    Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_DELETE_LAYER_DELETE))
                }
                Button(
                    onClick = {
                        tabModel.clearState()
                    },
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
                Text(Text.format(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_DELETE_LAYER_2, state.layer.name))
            }
        }
        SideBarContainer(
            sideBarAtRight = sideBarAtRight,
            tabsButton = tabsButton,
            actions = {
                IconButton(
                    onClick = {
                        uiState.selectedPreset?.let { preset ->
                            parentNavigator?.push(
                                LayerEditorScreen(
                                    screenName = Texts.SCREEN_LAYER_EDITOR_CREATE,
                                    preset = screenModel.uiState.mapState { (it as? CustomControlLayoutTabState.Enabled)?.selectedPreset },
                                    onCustomConditionsChanged = {
                                        screenModel.editPreset {
                                            copy(
                                                controlInfo = preset.controlInfo.copy(
                                                    customConditions = it
                                                )
                                            )
                                        }
                                    },
                                    initialValue = LayoutLayer(),
                                    onValueChanged = tabModel::createLayer,
                                )
                            )
                        }
                    },
                    enabled = uiState.selectedPreset != null,
                ) {
                    Icon(Textures.icon_add)
                }
            }
        ) { modifier ->
            val selectedPreset = uiState.selectedPreset
            SideBarScaffold(
                modifier = modifier,
                title = {
                    Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS))
                },
                actions = {
                    if (selectedPreset != null) {
                        val layerIndices = selectedPreset.layout.indices
                        val selectedLayerIndex = uiState.pageState.selectedLayerIndex.takeIf { it in layerIndices }
                        Button(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            enabled = selectedLayerIndex != null && selectedLayerIndex > 0,
                            onClick = {
                                selectedLayerIndex?.let { index ->
                                    tabModel.moveLayer(index, -1)
                                    screenModel.selectLayer(index - 1)
                                }
                            }
                        ) {
                            Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_MOVE_UP))
                        }
                        Button(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            enabled = selectedLayerIndex != null && selectedLayerIndex < layerIndices.last,
                            onClick = {
                                selectedLayerIndex?.let { index ->
                                    tabModel.moveLayer(index, 1)
                                    screenModel.selectLayer(index + 1)
                                }
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
                            currentSelectedLayoutIndex = uiState.pageState.selectedLayerIndex,
                            onLayerSelected = { index, layer ->
                                screenModel.selectLayer(index)
                            },
                            onLayerEdited = { index, layer ->
                                parentNavigator?.push(
                                    LayerEditorScreen(
                                        screenName = Texts.SCREEN_LAYER_EDITOR_EDIT,
                                        preset = screenModel.uiState.mapState { (it as? CustomControlLayoutTabState.Enabled)?.selectedPreset },
                                        onCustomConditionsChanged = {
                                            screenModel.editPreset {
                                                copy(
                                                    controlInfo = selectedPreset.controlInfo.copy(
                                                        customConditions = it
                                                    )
                                                )
                                            }
                                        },
                                        initialValue = layer,
                                        onValueChanged = { newLayer ->
                                            screenModel.editPreset {
                                                copy(
                                                    layout = ControllerLayout(
                                                        layout.set(index, newLayer)
                                                    )
                                                )
                                            }
                                        },
                                    )
                                )
                            },
                            onLayerCopied = { index, layer ->
                                tabModel.copyLayer(layer)
                            },
                            onLayerDeleted = { index, layer ->
                                tabModel.openDeleteLayerDialog(index, layer)
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
    }
}
