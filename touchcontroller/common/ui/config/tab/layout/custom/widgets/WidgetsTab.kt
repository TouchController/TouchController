/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.widgets

import androidx.compose.runtime.*
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
import top.fifthlight.combine.theme.blackstone.widget.AlertDialog
import top.fifthlight.combine.theme.blackstone.widget.CheckButton
import top.fifthlight.combine.theme.blackstone.widget.ListButton
import top.fifthlight.combine.theme.blackstone.widget.LocalListButtonTheme
import top.fifthlight.combine.widget.*
import top.fifthlight.data.IntOffset
import top.fifthlight.data.IntPadding
import top.fifthlight.data.IntRect
import top.fifthlight.data.IntSize
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.assets.texture.Textures
import top.fifthlight.touchcontroller.common.assets.TextureSet
import top.fifthlight.touchcontroller.common.assets.TextureSets
import top.fifthlight.touchcontroller.common.control.ControllerWidget
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.widget.SideBarScaffold
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.widgets.component.WidgetsTabComponent
import top.fifthlight.touchcontroller.common.ui.control.AutoScaleControllerWidget
import top.fifthlight.touchcontroller.common.ui.state.subscribeAsState
import top.fifthlight.touchcontroller.common.util.name.TranslatableName
import kotlin.jvm.optionals.getOrNull
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
    listContent: WidgetsTabComponent.ListContent.BuiltIn,
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
    listContent: WidgetsTabComponent.ListContent.Custom,
    onWidgetSelected: (ControllerWidget) -> Unit,
    onWidgetRenamed: (Int) -> Unit,
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
                            Pair(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_WIDGETS_WIDGET_PRESET_RENAME)) {
                                onWidgetRenamed(index)
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

@Composable
fun WidgetsTabActions(component: WidgetsTabComponent) {
    val selectedWidget by component.selectedWidget.subscribeAsState()
    IconButton(
        enabled = selectedWidget.getOrNull() != null,
        onClick = component::addWidgetPreset,
    ) {
        Icon(Textures.icon_save)
    }

    IconButton(
        onClick = component::openChangeNewWidgetParamsDialog,
    ) {
        Icon(Textures.icon_config)
    }
}

@Composable
fun WidgetsTab(component: WidgetsTabComponent) {
    val uiState by component.uiState.subscribeAsState()
    val dialog by component.dialog.subscribeAsState()

    AlertDialog(
        value = dialog.child?.instance,
        valueTransformer = { it as? WidgetsTabComponent.DialogComponent.ChangeNewWidgetParams },
        onDismissRequest = component::closeDialog,
        actions = { state ->
            GuideButton(
                onClick = {
                    component.updateNewWidgetParams(state.uiState.value)
                    component.closeDialog()
                },
            ) {
                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_WIDGETS_NEW_WIDGET_SAVE))
            }
            Button(
                onClick = component::closeDialog,
            ) {
                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_WIDGETS_NEW_WIDGET_CANCEL))
            }
        }
    ) { state ->
        val params by state.uiState.subscribeAsState()
        Column(
            modifier = Modifier.fillMaxWidth(.5f),
            verticalArrangement = Arrangement.spacedBy(4),
        ) {
            Text(
                Text.format(
                    Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_WIDGETS_NEW_WIDGET_OPACITY,
                    (params.opacity * 100).toInt()
                )
            )
            Slider(
                modifier = Modifier.fillMaxWidth(),
                range = 0f..1f,
                value = params.opacity,
                onValueChanged = {
                    state.update { copy(opacity = it) }
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
                            selectedIndex = textureSetEntries.indexOf(params.textureSet),
                            onItemSelected = { item, _ ->
                                state.update { copy(textureSet = item) }
                                expanded = false
                            }
                        )
                    }
                ) {
                    Text(params.textureSet.name)
                    SelectIcon(expanded = expanded)
                }
            }
        }
    }

    AlertDialog(
        value = dialog.child?.instance,
        valueTransformer = { it as? WidgetsTabComponent.DialogComponent.RenameWidgetPresetItem },
        onDismissRequest = component::closeDialog,
        title = {
            Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_WIDGETS_EDIT_WIDGET_PRESET_TITLE))
        },
        actions = { state ->
            GuideButton(
                onClick = {
                    component.renameWidgetPresetItem(state.index, state.uiState.value)
                    component.closeDialog()
                },
            ) {
                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_WIDGETS_EDIT_WIDGET_PRESET_SAVE))
            }
            Button(
                onClick = component::closeDialog,
            ) {
                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_WIDGETS_EDIT_WIDGET_PRESET_CANCEL))
            }
        }
    ) { state ->
        val name by state.uiState.subscribeAsState()
        EditText(
            modifier = Modifier.fillMaxWidth(.5f),
            value = name.asString(),
            onValueChanged = { state.update(TranslatableName.Literal(it)) }
        )
    }

    SideBarScaffold(
        modifier = Modifier.fillMaxSize(),
        title = {
            Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_WIDGETS))
        },
        actions = {
            CheckButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                checked = uiState.listState == WidgetsTabComponent.ListState.BUILTIN,
                onClick = component::selectBuiltinTab,
            ) {
                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_WIDGETS_BUILTIN))
            }
            CheckButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                checked = uiState.listState == WidgetsTabComponent.ListState.CUSTOM,
                onClick = component::selectCustomTab,
            ) {
                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_WIDGETS_PRESET))
            }
        }
    ) {
        when (val listContent = uiState.listContent) {
            is WidgetsTabComponent.ListContent.BuiltIn -> BuiltInWidgetList(
                modifier = Modifier
                    .padding(4)
                    .verticalScroll()
                    .fillMaxSize(),
                listContent = listContent,
                onWidgetSelected = component::addWidget,
            )

            is WidgetsTabComponent.ListContent.Custom ->
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
                        onWidgetSelected = component::addWidget,
                        onWidgetRenamed = component::openRenameWidgetPresetItemDialog,
                        onWidgetDeleted = component::deleteWidgetPresetItem,
                    )
                }
        }
    }
}
