/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.presets

import androidx.compose.runtime.*
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.layout.Alignment
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.placement.*
import top.fifthlight.combine.core.modifier.scroll.verticalScroll
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.core.widget.layout.Row
import top.fifthlight.combine.widget.ui.*
import top.fifthlight.data.IntRect
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.assets.texture.Textures
import top.fifthlight.touchcontroller.common.config.preset.LayoutPreset
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.presets.model.PresetsTabModel
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.presets.state.PresetsTabState
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.tab.CustomTab
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.tab.LocalCustomTabContext
import top.fifthlight.touchcontroller.common.ui.importpreset.screen.ImportPresetScreen
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme
import top.fifthlight.touchcontroller.common.ui.widget.ListButton
import kotlin.uuid.Uuid

@Composable
private fun PresetsList(
    modifier: Modifier = Modifier,
    listContent: PersistentList<Pair<Uuid, LayoutPreset>> = persistentListOf(),
    currentSelectedPresetUuid: Uuid? = null,
    onPresetSelected: (Uuid, LayoutPreset) -> Unit = { _, _ -> },
    onPresetEdited: (Uuid, LayoutPreset) -> Unit = { _, _ -> },
    onPresetShowPath: (Uuid) -> Unit = {},
    onPresetCopied: (Uuid, LayoutPreset) -> Unit = { _, _ -> },
    onPresetDeleted: (Uuid, LayoutPreset) -> Unit = { _, _ -> },
) {
    Column(modifier = modifier) {
        for ((uuid, preset) in listContent) {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                ListButton(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    checked = currentSelectedPresetUuid == uuid,
                    onClick = {
                        onPresetSelected(uuid, preset)
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
                    drawableSet = LocalTouchControllerTheme.current.tabButtonDrawablesUnchecked,
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
                            Pair(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_EDIT)) {
                                onPresetEdited(uuid, preset)
                            },
                            Pair(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_SHOW_PATH)) {
                                onPresetShowPath(uuid)
                            },
                            Pair(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_COPY)) {
                                onPresetCopied(uuid, preset)
                            },
                            Pair(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_DELETE)) {
                                onPresetDeleted(uuid, preset)
                            },
                        ),
                    )
                }
            }
        }
    }
}

object PresetsTab : CustomTab() {
    @Composable
    override fun Icon() {
        Icon(Textures.icon_preset)
    }

    @Composable
    override fun Content() {
        val (screenModel, uiState, tabsButton, sideBarAtRight) = LocalCustomTabContext.current
        val tabModel = rememberScreenModel { PresetsTabModel(screenModel) }
        val tabState by tabModel.uiState.collectAsState()
        val navigator = LocalNavigator.current
        AlertDialog(
            visible = tabState == PresetsTabState.CreateChoose,
            onDismissRequest = { tabModel.clearState() },
            title = {
                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_CREATE_PRESET_CHOOSE))
            },
        ) {
            Column(
                modifier = Modifier.width(IntrinsicSize.Min),
                verticalArrangement = Arrangement.spacedBy(4),
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        navigator?.parent?.push(ImportPresetScreen { key ->
                            screenModel.newPreset(key.preset)
                        })
                        tabModel.clearState()
                    },
                ) {
                    Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_CREATE_PRESET_CHOOSE_PRESET))
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        tabModel.openCreateEmptyPresetDialog()
                    },
                ) {
                    Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_CREATE_PRESET_CHOOSE_EMPTY))
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        tabModel.clearState()
                    },
                ) {
                    Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_CREATE_PRESET_CHOOSE_CANCEL))
                }
            }
        }
        AlertDialog(
            value = tabState,
            valueTransformer = { tabState as? PresetsTabState.CreateEmpty },
            title = {
                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_CREATE_EMPTY_PRESET))
            },
            action = { state ->
                GuideButton(
                    onClick = {
                        tabModel.createPreset(state)
                    },
                ) {
                    Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_CREATE_EMPTY_PRESET_CREATE))
                }
                Button(
                    onClick = {
                        tabModel.clearState()
                    },
                ) {
                    Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_CREATE_EMPTY_PRESET_CANCEL))
                }
            }
        ) { state ->
            Column(
                modifier = Modifier.fillMaxWidth(.5f),
                verticalArrangement = Arrangement.spacedBy(4),
            ) {
                EditText(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.name,
                    onValueChanged = {
                        tabModel.updateCreatePresetState { copy(name = it) }
                    },
                    placeholder = Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_NAME_PLACEHOLDER),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_SPLIT_CONTROLS))

                    Switch(
                        value = state.controlInfo.splitControls,
                        onValueChanged = {
                            tabModel.updateCreatePresetState {
                                copy(controlInfo = controlInfo.copy(splitControls = it))
                            }
                        },
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_DISABLE_TOUCH_GESTURES))

                    Switch(
                        value = state.controlInfo.disableTouchGesture,
                        onValueChanged = {
                            tabModel.updateCreatePresetState {
                                copy(controlInfo = controlInfo.copy(disableTouchGesture = it))
                            }
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_DISABLE_CROSSHAIR))

                    Switch(
                        value = state.controlInfo.disableCrosshair,
                        onValueChanged = {
                            tabModel.updateCreatePresetState {
                                copy(controlInfo = controlInfo.copy(disableCrosshair = it))
                            }
                        }
                    )
                }
            }
        }
        AlertDialog(
            value = tabState,
            valueTransformer = { tabState as? PresetsTabState.Edit },
            title = {
                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_EDIT_PRESET))
            },
            action = { state ->
                GuideButton(
                    onClick = {
                        if (uiState.selectedPresetUuid == state.uuid) {
                            screenModel.editPreset(false, state::edit)
                            screenModel.savePreset()
                            tabModel.clearState()
                        } else {
                            tabModel.editPreset(state)
                        }
                    },
                ) {
                    Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_EDIT_PRESET_OK))
                }
                Button(
                    onClick = {
                        tabModel.clearState()
                    },
                ) {
                    Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_EDIT_PRESET_CANCEL))
                }
            }
        ) { state ->
            Column(
                modifier = Modifier.fillMaxWidth(.5f),
                verticalArrangement = Arrangement.spacedBy(4),
            ) {
                EditText(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.name,
                    onValueChanged = {
                        tabModel.updateEditPresetState { copy(name = it) }
                    },
                    placeholder = Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_NAME_PLACEHOLDER),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_SPLIT_CONTROLS))

                    Switch(
                        value = state.controlInfo.splitControls,
                        onValueChanged = {
                            tabModel.updateEditPresetState {
                                copy(controlInfo = controlInfo.copy(splitControls = it))
                            }
                        },
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_DISABLE_TOUCH_GESTURES))

                    Switch(
                        value = state.controlInfo.disableTouchGesture,
                        onValueChanged = {
                            tabModel.updateEditPresetState {
                                copy(controlInfo = controlInfo.copy(disableTouchGesture = it))
                            }
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_DISABLE_CROSSHAIR))

                    Switch(
                        value = state.controlInfo.disableCrosshair,
                        onValueChanged = {
                            tabModel.updateEditPresetState {
                                copy(controlInfo = controlInfo.copy(disableCrosshair = it))
                            }
                        }
                    )
                }
            }
        }
        AlertDialog(
            value = tabState,
            valueTransformer = { tabState as? PresetsTabState.Delete },
            onDismissRequest = { tabModel.clearState() },
            action = { state ->
                WarningButton(
                    onClick = {
                        screenModel.deletePreset(state.uuid)
                        tabModel.clearState()
                    },
                ) {
                    Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_DELETE_PRESET_DELETE))
                }
                Button(
                    onClick = {
                        tabModel.clearState()
                    },
                ) {
                    Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_DELETE_PRESET_CANCEL))
                }
            }
        ) { state ->
            val presetName = uiState.allPresets[state.uuid]?.name ?: "ERROR"
            Column(
                verticalArrangement = Arrangement.spacedBy(4),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_DELETE_PRESET_1))
                Text(Text.format(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_DELETE_PRESET_2, presetName))
            }
        }
        AlertDialog(
            value = tabState,
            valueTransformer = { tabState as? PresetsTabState.Path },
            onDismissRequest = { tabModel.clearState() },
            title = {
                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_PATH))
            },
            action = { state ->
                GuideButton(
                    onClick = {
                        tabModel.clearState()
                    },
                ) {
                    Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_PATH_OK))
                }
            }
        ) { state ->
            Text(state.path?.let { Text.literal(it) }
                ?: Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_PATH_GET_FAILED))
        }

        SideBarContainer(
            sideBarAtRight = sideBarAtRight,
            tabsButton = tabsButton,
            actions = {
                IconButton(
                    onClick = {
                        tabModel.openCreatePresetChooseDialog()
                    }
                ) {
                    Icon(Textures.icon_add)
                }
            }
        ) { modifier ->
            SideBarScaffold(
                modifier = modifier,
                title = {
                    Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS))
                },
                actions = {
                    val selectedUuid = uiState.selectedPresetUuid
                    val indices = uiState.allPresets.orderedEntries.indices
                    val index = selectedUuid?.let { selectedUuid ->
                        uiState.allPresets.orderedEntries.indexOfFirst { (uuid, _) -> uuid == selectedUuid }
                    } ?: run {
                        -1
                    }
                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        enabled = selectedUuid != null && index > 0,
                        onClick = {
                            selectedUuid?.let { uuid ->
                                tabModel.movePreset(uuid, -1)
                            }
                        }
                    ) {
                        Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_MOVE_UP))
                    }
                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        enabled = selectedUuid != null && index < indices.last,
                        onClick = {
                            selectedUuid?.let { uuid ->
                                tabModel.movePreset(uuid, 1)
                            }
                        }
                    ) {
                        Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_MOVE_DOWN))
                    }
                }
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll()
                        .fillMaxSize()
                ) {
                    PresetsList(
                        modifier = Modifier
                            .padding(4)
                            .fillMaxWidth(),
                        listContent = uiState.allPresets.orderedEntries,
                        currentSelectedPresetUuid = uiState.selectedPresetUuid,
                        onPresetSelected = { uuid, preset ->
                            screenModel.selectPreset(uuid)
                        },
                        onPresetEdited = { uuid, preset ->
                            tabModel.openEditPresetDialog(uuid, preset)
                        },
                        onPresetShowPath = { uuid ->
                            tabModel.openPresetPathDialog(uuid)
                        },
                        onPresetCopied = { uuid, preset ->
                            screenModel.newPreset(preset)
                        },
                        onPresetDeleted = { uuid, preset ->
                            tabModel.openDeletePresetBox(uuid)
                        }
                    )
                }
            }
        }
    }
}
