/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.widgets

import androidx.compose.runtime.*
import cafe.adriel.voyager.core.model.rememberScreenModel
import kotlinx.collections.immutable.persistentListOf
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.layout.Alignment
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.layout.Layout
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.ParentDataModifierNode
import top.fifthlight.combine.core.modifier.placement.*
import top.fifthlight.combine.core.modifier.scroll.verticalScroll
import top.fifthlight.combine.core.widget.layout.Box
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.core.widget.layout.Row
import top.fifthlight.combine.widget.ui.*
import top.fifthlight.data.IntOffset
import top.fifthlight.data.IntPadding
import top.fifthlight.data.IntRect
import top.fifthlight.data.IntSize
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.assets.texture.Textures
import top.fifthlight.touchcontroller.common.assets.TextureSet
import top.fifthlight.touchcontroller.common.assets.TextureSets
import top.fifthlight.touchcontroller.common.control.ControllerWidget
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.tab.CustomTab
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.tab.LocalCustomTabContext
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.widgets.model.WidgetsTabModel
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.widgets.state.WidgetsTabState
import top.fifthlight.touchcontroller.common.ui.control.AutoScaleControllerWidget
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme
import top.fifthlight.touchcontroller.common.ui.widget.CheckButton
import top.fifthlight.touchcontroller.common.ui.widget.ListButton
import kotlin.math.max

private enum class ControllerWidgetType {
    NORMAL,
    HERO,
}

private data class ControllerWidgetModifierNode(
    val type: ControllerWidgetType,
) : ParentDataModifierNode, Modifier.Node<ControllerWidgetModifierNode> {
    override fun modifierParentData(parentData: Any?): ControllerWidgetType = type
}

private object WidgetsLayoutScope {
    fun Modifier.widgetType(type: ControllerWidgetType) = then(ControllerWidgetModifierNode(type))
}

@Composable
private fun WidgetsLayout(
    modifier: Modifier = Modifier,
    content: @Composable WidgetsLayoutScope.() -> Unit,
) {
    Layout(
        modifier = modifier,
        measurePolicy = { measurables, constraints ->
            val heroesEnabled = constraints.maxWidth > 196
            val childConstraint = constraints.copy(minWidth = 0, minHeight = 0)
            val heroConstraint = if (heroesEnabled) {
                val heroWidth = constraints.maxWidth / 2
                constraints.copy(
                    minWidth = heroWidth,
                    maxWidth = heroWidth,
                    minHeight = 0,
                )
            } else {
                childConstraint
            }

            val childPositions = Array(measurables.size) { IntOffset.ZERO }
            var cursorPosition = IntOffset(0, 0)
            var maxWidth = 0
            var rowMaxHeight = 0
            var column = 0
            var row = 0

            val placeables = measurables.mapIndexed { index, measurable ->
                val isHero = measurable.parentData == ControllerWidgetType.HERO
                val placeable = measurable.measure(
                    if (isHero) {
                        heroConstraint
                    } else {
                        childConstraint
                    }
                )
                if (!isHero || placeable.width + cursorPosition.left > constraints.maxWidth || column >= 2) {
                    // Break line
                    cursorPosition = IntOffset(0, cursorPosition.y + rowMaxHeight)
                    rowMaxHeight = 0
                    column = 0
                    row++
                }
                column++
                childPositions[index] = cursorPosition
                cursorPosition = IntOffset(cursorPosition.x + placeable.width, cursorPosition.y)
                maxWidth = max(maxWidth, cursorPosition.x)
                rowMaxHeight = max(rowMaxHeight, placeable.height)
                placeable
            }

            val width = maxWidth.coerceIn(constraints.minWidth, constraints.maxWidth)
            val height = (cursorPosition.y + rowMaxHeight).coerceIn(constraints.minHeight, constraints.maxHeight)

            layout(width, height) {
                placeables.forEachIndexed { index, placeable ->
                    placeable.placeAt(childPositions[index])
                }
            }
        },
        content = { WidgetsLayoutScope.content() },
    )
}

@Composable
private fun WidgetButton(
    modifier: Modifier = Modifier,
    widget: ControllerWidget,
    widgetIconSize: IntSize,
    onClicked: () -> Unit,
) {
    ListButton(
        modifier = modifier,
        padding = IntPadding(left = 4, right = 4),
        onClick = onClicked,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4),
        ) {
            AutoScaleControllerWidget(
                modifier = Modifier.size(widgetIconSize),
                widget = widget,
            )
            Text(
                modifier = Modifier.weight(1f),
                text = widget.name.getText()
            )
        }
    }
}

@Composable
private fun BuiltInWidgetList(
    modifier: Modifier = Modifier,
    listContent: WidgetsTabState.ListContent.BuiltIn,
    onWidgetSelected: (ControllerWidget) -> Unit,
) {
    WidgetsLayout(modifier) {
        for (widget in listContent.heroes) {
            WidgetButton(
                modifier = Modifier.widgetType(ControllerWidgetType.HERO),
                widget = widget,
                widgetIconSize = IntSize(32),
                onClicked = { onWidgetSelected(widget) },
            )
        }
        for (widget in listContent.widgets) {
            WidgetButton(
                modifier = Modifier
                    .widgetType(ControllerWidgetType.NORMAL)
                    .fillMaxWidth(),
                widget = widget,
                widgetIconSize = IntSize(16),
                onClicked = { onWidgetSelected(widget) },
            )
        }
    }
}

@Composable
private fun CustomWidgetList(
    modifier: Modifier = Modifier,
    listContent: WidgetsTabState.ListContent.Custom,
    onWidgetSelected: (ControllerWidget) -> Unit,
    onWidgetRenamed: (Int, ControllerWidget) -> Unit,
    onWidgetDeleted: (Int) -> Unit,
) {
    WidgetsLayout(modifier) {
        for ((index, widget) in listContent.widgets.withIndex()) {
            Row(
                modifier = Modifier
                    .widgetType(ControllerWidgetType.NORMAL)
                    .height(IntrinsicSize.Min)
                    .fillMaxWidth()
            ) {
                WidgetButton(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    widget = widget,
                    widgetIconSize = IntSize(16),
                    onClicked = { onWidgetSelected(widget) },
                )

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
                            Pair(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_WIDGETS_WIDGET_PRESET_RENAME)) {
                                onWidgetRenamed(index, widget)
                            },
                            Pair(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_WIDGETS_WIDGET_PRESET_DELETE)) {
                                onWidgetDeleted(index)
                            },
                        ),
                    )
                }
            }
        }
    }
}

object WidgetsTab : CustomTab() {
    @Composable
    override fun Icon() {
        Icon(Textures.icon_widget)
    }

    @Composable
    override fun Content() {
        val (screenModel, uiState, tabsButton, sideBarAtRight) = LocalCustomTabContext.current
        val tabModel = rememberScreenModel { WidgetsTabModel(screenModel) }
        val tabState by tabModel.uiState.collectAsState()

        AlertDialog(
            value = tabState,
            valueTransformer = { tabState.tabState.dialogState as? WidgetsTabState.DialogState.ChangeNewWidgetParams },
            onDismissRequest = { tabModel.closeDialog() },
            action = { dialogState ->
                GuideButton(
                    onClick = {
                        tabModel.closeDialog()
                        tabModel.updateNewWidgetParams(dialogState.toParams())
                    },
                ) {
                    Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_WIDGETS_NEW_WIDGET_SAVE))
                }
                Button(
                    onClick = {
                        tabModel.closeDialog()
                    },
                ) {
                    Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_WIDGETS_NEW_WIDGET_CANCEL))
                }
            }
        ) { dialogState ->
            Column(
                modifier = Modifier.fillMaxWidth(.5f),
                verticalArrangement = Arrangement.spacedBy(4),
            ) {
                Text(
                    Text.format(
                        Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_WIDGETS_NEW_WIDGET_OPACITY,
                        (dialogState.opacity * 100).toInt()
                    )
                )
                Slider(
                    modifier = Modifier.fillMaxWidth(),
                    range = 0f..1f,
                    value = dialogState.opacity,
                    onValueChanged = {
                        tabModel.updateNewWidgetParamsDialog { copy(opacity = it) }
                    }
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_WIDGETS_NEW_WIDGET_TEXTURE_SET)
                    )

                    var expanded by remember { mutableStateOf(false) }
                    val textureSetEntries = TextureSets.registry.values().toList()
                    Select(
                        expanded = expanded,
                        onExpandedChanged = { expanded = it },
                        dropDownContent = {
                            DropdownItemList(
                                modifier = Modifier.verticalScroll(),
                                items = textureSetEntries,
                                textProvider = TextureSet::name,
                                selectedIndex = textureSetEntries.indexOf(dialogState.textureSet),
                                onItemSelected = { item, _ ->
                                    tabModel.updateNewWidgetParamsDialog { copy(textureSet = item) }
                                    expanded = false
                                }
                            )
                        }
                    ) {
                        Text(dialogState.textureSet.name)
                        SelectIcon(expanded = expanded)
                    }
                }
            }
        }
        AlertDialog(
            value = tabState,
            valueTransformer = { tabState.tabState.dialogState as? WidgetsTabState.DialogState.RenameWidgetPresetItem },
            onDismissRequest = { tabModel.closeDialog() },
            title = {
                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_WIDGETS_EDIT_WIDGET_PRESET_TITLE))
            },
            action = { dialogState ->
                GuideButton(
                    onClick = {
                        tabModel.renameWidgetPresetItem(dialogState.index, dialogState.name)
                        tabModel.closeDialog()
                    },
                ) {
                    Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_WIDGETS_EDIT_WIDGET_PRESET_SAVE))
                }
                Button(
                    onClick = {
                        tabModel.closeDialog()
                    },
                ) {
                    Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_WIDGETS_EDIT_WIDGET_PRESET_CANCEL))
                }
            }
        ) { dialogState ->
            EditText(
                modifier = Modifier.fillMaxWidth(.5f),
                value = dialogState.name.asString(),
                onValueChanged = tabModel::updateRenameWidgetPresetItemDialog
            )
        }

        SideBarContainer(
            sideBarAtRight = sideBarAtRight,
            tabsButton = tabsButton,
            actions = {
                IconButton(
                    enabled = uiState.selectedWidget != null,
                    onClick = {
                        uiState.selectedWidget?.let(screenModel::addWidgetPreset)
                    },
                ) {
                    Icon(Textures.icon_save)
                }

                IconButton(
                    onClick = {
                        tabModel.openNewWidgetParamsDialog()
                    }
                ) {
                    Icon(Textures.icon_config)
                }
            }
        ) { modifier ->
            SideBarScaffold(
                modifier = modifier,
                title = {
                    Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_WIDGETS))
                },
                actions = {
                    CheckButton(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        checked = tabState.tabState.listState == WidgetsTabState.ListState.BUILTIN,
                        onClick = {
                            tabModel.selectBuiltinTab()
                        }
                    ) {
                        Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_WIDGETS_BUILTIN))
                    }
                    CheckButton(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        checked = tabState.tabState.listState == WidgetsTabState.ListState.CUSTOM,
                        onClick = {
                            tabModel.selectCustomTab()
                        }
                    ) {
                        Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_WIDGETS_PRESET))
                    }
                }
            ) {
                fun addWidget(widget: ControllerWidget) {
                    val newWidget = widget.cloneBase(
                        opacity = tabState.tabState.newWidgetParams.opacity,
                    )
                    val index = screenModel.newWidget(newWidget)
                    screenModel.selectWidget(index)
                }

                when (val listContent = tabState.listContent) {
                    is WidgetsTabState.ListContent.BuiltIn -> BuiltInWidgetList(
                        modifier = Modifier
                            .padding(4)
                            .verticalScroll()
                            .fillMaxSize(),
                        listContent = listContent,
                        onWidgetSelected = ::addWidget,
                    )

                    is WidgetsTabState.ListContent.Custom ->
                        if (listContent.widgets.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                alignment = Alignment.Center,
                            ) {
                                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_WIDGETS_WIDGET_PRESET_EMPTY_TIP))
                            }
                        } else {
                            CustomWidgetList(
                                modifier = Modifier
                                    .padding(4)
                                    .verticalScroll()
                                    .fillMaxSize(),
                                listContent = listContent,
                                onWidgetSelected = ::addWidget,
                                onWidgetRenamed = tabModel::openRenameWidgetPresetItemDialog,
                                onWidgetDeleted = tabModel::deleteWidgetPresetItem,
                            )
                        }
                }
            }
        }
    }
}
