/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.presets

import androidx.compose.runtime.*
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
import top.fifthlight.combine.theme.blackstone.widget.AlertDialog
import top.fifthlight.combine.theme.blackstone.widget.ListButton
import top.fifthlight.combine.theme.blackstone.widget.LocalNavigationButtonTheme
import top.fifthlight.combine.widget.*
import top.fifthlight.data.IntRect
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.assets.texture.Textures
import top.fifthlight.touchcontroller.common.config.preset.LayoutPreset
import top.fifthlight.touchcontroller.common.config.preset.builtin.key.BuiltinPresetKey
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.presets.component.PresetsTabComponent
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.widget.SideBarScaffold
import top.fifthlight.touchcontroller.common.ui.importpreset.screen.ImportPresetScreen
import top.fifthlight.touchcontroller.common.ui.preset.TopBarConfigureScreen
import top.fifthlight.touchcontroller.common.ui.state.subscribeAsState
import top.fifthlight.touchcontroller.common.util.name.TranslatableName
import kotlin.jvm.optionals.getOrNull
import kotlin.uuid.Uuid

@Composable
private fun PresetsList(
    modifier: Modifier = Modifier,
    presets: PersistentList<Pair<Uuid, LayoutPreset>> = persistentListOf(),
    currentSelectedPresetUuid: Uuid? = null,
    currentSelectedPreset: LayoutPreset? = null,
    onPresetSelected: (Uuid, LayoutPreset) -> Unit = { _, _ -> },
    onPresetEdited: (Uuid, LayoutPreset) -> Unit = { _, _ -> },
    onPresetShowPath: (Uuid) -> Unit = {},
    onPresetCopied: (Uuid, LayoutPreset) -> Unit = { _, _ -> },
    onPresetDeleted: (Uuid, LayoutPreset) -> Unit = { _, _ -> },
) {
    Column(modifier = modifier) {
        for ((uuid, preset) in presets) {
            val preset = if (currentSelectedPresetUuid == uuid) {
                currentSelectedPreset ?: preset
            } else {
                preset
            }
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
                        text = preset.name.getText(),
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
                    drawableSet = LocalNavigationButtonTheme.current.drawableSet.off,
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

@Composable
fun PresetsTabActions(component: PresetsTabComponent) {
    IconButton(
        onClick = {
            component.openCreatePresetChooseDialog()
        }
    ) {
        Icon(Textures.icon_add)
    }
}

@Composable
fun PresetsTab(component: PresetsTabComponent) {
    val stack by component.childStack.subscribeAsState()
    when (val instance = stack.active.instance) {
        is PresetsTabComponent.StackComponent.Presets -> PresetsScreen(component)
        is PresetsTabComponent.StackComponent.Import -> ImportPresetScreen(instance.component)
        is PresetsTabComponent.StackComponent.ImportTopBar -> {
            val key by instance.key.subscribeAsState()
            TopBarConfigureScreen(
                key = key,
                onWidgetsChanged = { instance.onKeyChanged(BuiltinPresetKey.TopBarConfig(it)) },
                onBack = instance.onBack,
            )
        }
    }
}

@Composable
private fun PresetsScreen(component: PresetsTabComponent) {
    val presets by component.presets.subscribeAsState()
    val selectedPresetValue by component.selectedPreset.subscribeAsState()
    val (selectedPresetUuid, selectedPreset) = selectedPresetValue
    val dialog by component.dialog.subscribeAsState()
    AlertDialog(
        value = dialog.child?.instance,
        valueTransformer = { it as? PresetsTabComponent.ChildComponent.CreateChoose },
        onDismissRequest = { component.closeDialog() },
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
                    component.openImportPreset()
                },
            ) {
                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_CREATE_PRESET_CHOOSE_PRESET))
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    component.openCreateEmptyPresetDialog()
                },
            ) {
                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_CREATE_PRESET_CHOOSE_EMPTY))
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    component.closeDialog()
                },
            ) {
                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_CREATE_PRESET_CHOOSE_CANCEL))
            }
        }
    }
    AlertDialog(
        value = dialog.child?.instance,
        valueTransformer = { it as? PresetsTabComponent.ChildComponent.CreateEmpty },
        onDismissRequest = { component.closeDialog() },
        title = {
            Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_CREATE_EMPTY_PRESET))
        },
        actions = { state ->
            GuideButton(
                onClick = {
                    state.onCreated(state.uiState.value.name, state.uiState.value.controlInfo)
                    component.closeDialog()
                },
            ) {
                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_CREATE_EMPTY_PRESET_CREATE))
            }
            Button(
                onClick = {
                    component.closeDialog()
                },
            ) {
                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_CREATE_EMPTY_PRESET_CANCEL))
            }
        }
    ) { state ->
        val data by state.uiState.subscribeAsState()
        PresetDataEditor(
            data = data,
            onNameChanged = { name -> state.update { copy(name = TranslatableName.Literal(name)) } },
            onSplitControlsChanged = { value -> state.update { copy(controlInfo = controlInfo.copy(splitControls = value)) } },
            onTouchGestureChanged = { value -> state.update { copy(controlInfo = controlInfo.copy(disableTouchGesture = value)) } },
            onCrosshairChanged = { value -> state.update { copy(controlInfo = controlInfo.copy(disableCrosshair = value)) } },
        )
    }
    AlertDialog(
        value = dialog.child?.instance,
        valueTransformer = { it as? PresetsTabComponent.ChildComponent.Edit },
        onDismissRequest = { component.closeDialog() },
        title = {
            Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_EDIT_PRESET))
        },
        actions = { state ->
            GuideButton(
                onClick = {
                    state.onEdited(state.uiState.value.name, state.uiState.value.controlInfo)
                    component.closeDialog()
                },
            ) {
                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_EDIT_PRESET_OK))
            }
            Button(
                onClick = {
                    component.closeDialog()
                },
            ) {
                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_EDIT_PRESET_CANCEL))
            }
        }
    ) { state ->
        val data by state.uiState.subscribeAsState()
        PresetDataEditor(
            data = data,
            onNameChanged = { name -> state.update { copy(name = TranslatableName.Literal(name)) } },
            onSplitControlsChanged = { value -> state.update { copy(controlInfo = controlInfo.copy(splitControls = value)) } },
            onTouchGestureChanged = { value -> state.update { copy(controlInfo = controlInfo.copy(disableTouchGesture = value)) } },
            onCrosshairChanged = { value -> state.update { copy(controlInfo = controlInfo.copy(disableCrosshair = value)) } },
        )
    }
    AlertDialog(
        value = dialog.child?.instance,
        valueTransformer = { it as? PresetsTabComponent.ChildComponent.Delete },
        onDismissRequest = { component.closeDialog() },
        actions = { state ->
            WarningButton(
                onClick = {
                    state.onDeleted()
                    component.closeDialog()
                },
            ) {
                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_DELETE_PRESET_DELETE))
            }
            Button(
                onClick = {
                    component.closeDialog()
                },
            ) {
                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_DELETE_PRESET_CANCEL))
            }
        }
    ) { state ->
        Column(
            verticalArrangement = Arrangement.spacedBy(4),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_DELETE_PRESET_1))
            Text(Text.format(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_DELETE_PRESET_2, state.name))
        }
    }
    AlertDialog(
        value = dialog.child?.instance,
        valueTransformer = { it as? PresetsTabComponent.ChildComponent.Path },
        onDismissRequest = { component.closeDialog() },
        title = {
            Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_PATH))
        },
        actions = { _ ->
            GuideButton(
                onClick = {
                    component.closeDialog()
                },
            ) {
                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_PATH_OK))
            }
        }
    ) { state ->
        Text(state.path?.let { Text.literal(it) }
            ?: Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_PATH_GET_FAILED))
    }

    SideBarScaffold(
        modifier = Modifier.fillMaxSize(),
        title = {
            Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS))
        },
        actions = {
            val indices = presets.orderedEntries.indices
            val index = selectedPresetUuid?.let { uuid ->
                presets.orderedEntries.indexOfFirst { (entryUuid, _) -> entryUuid == uuid }
            } ?: -1
            Button(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                enabled = selectedPresetUuid != null && index > 0,
                onClick = {
                    selectedPresetUuid?.let { uuid ->
                        component.movePreset(uuid, -1)
                    }
                }
            ) {
                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_MOVE_UP))
            }
            Button(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                enabled = selectedPresetUuid != null && index < indices.last,
                onClick = {
                    selectedPresetUuid?.let { uuid ->
                        component.movePreset(uuid, 1)
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
                presets = presets.orderedEntries,
                currentSelectedPresetUuid = selectedPresetUuid,
                currentSelectedPreset = selectedPreset,
                onPresetSelected = { uuid, _ ->
                    component.selectPreset(uuid)
                },
                onPresetEdited = { uuid, _ ->
                    component.openEditPresetDialog(uuid)
                },
                onPresetShowPath = { uuid ->
                    component.openPresetPathDialog(uuid)
                },
                onPresetCopied = { _, preset ->
                    component.copyPreset(preset)
                },
                onPresetDeleted = { uuid, _ ->
                    component.openDeletePresetBox(uuid)
                }
            )
        }
    }
}

@Composable
private fun PresetDataEditor(
    data: PresetsTabComponent.PresetData,
    onNameChanged: (String) -> Unit,
    onSplitControlsChanged: (Boolean) -> Unit,
    onTouchGestureChanged: (Boolean) -> Unit,
    onCrosshairChanged: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(.5f),
        verticalArrangement = Arrangement.spacedBy(4),
    ) {
        EditText(
            modifier = Modifier.fillMaxWidth(),
            value = data.name.asString(),
            onValueChanged = onNameChanged,
            placeholder = Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_NAME_PLACEHOLDER),
        )

        SwitchRow(
            title = Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_SPLIT_CONTROLS),
            value = data.controlInfo.splitControls,
            onValueChanged = onSplitControlsChanged,
        )

        SwitchRow(
            title = Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_DISABLE_TOUCH_GESTURES),
            value = data.controlInfo.disableTouchGesture,
            onValueChanged = onTouchGestureChanged,
        )

        SwitchRow(
            title = Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PRESETS_DISABLE_CROSSHAIR),
            value = data.controlInfo.disableCrosshair,
            onValueChanged = onCrosshairChanged,
        )
    }
}

@Composable
private fun SwitchRow(
    title: Text,
    value: Boolean,
    onValueChanged: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title)

        Switch(
            value = value,
            onValueChanged = onValueChanged,
        )
    }
}
