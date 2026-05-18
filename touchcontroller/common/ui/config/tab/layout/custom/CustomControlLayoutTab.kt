/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom

import androidx.compose.runtime.*
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.collections.immutable.PersistentList
import top.fifthlight.combine.core.animation.animateFloatAsState
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.layout.Alignment
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.layout.Layout
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.ParentDataModifierNode
import top.fifthlight.combine.core.modifier.drawing.background
import top.fifthlight.combine.core.modifier.drawing.border
import top.fifthlight.combine.core.modifier.drawing.innerLine
import top.fifthlight.combine.core.modifier.placement.*
import top.fifthlight.combine.core.modifier.pointer.consumePress
import top.fifthlight.combine.core.modifier.pointer.draggable
import top.fifthlight.combine.core.paint.Colors
import top.fifthlight.combine.core.widget.layout.Box
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.core.widget.layout.Row
import top.fifthlight.combine.widget.ui.*
import top.fifthlight.data.IntOffset
import top.fifthlight.data.IntRect
import top.fifthlight.data.IntSize
import top.fifthlight.data.Offset
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.assets.texture.Textures
import top.fifthlight.touchcontroller.common.config.layout.LayoutLayer
import top.fifthlight.touchcontroller.common.control.ControllerWidget
import top.fifthlight.touchcontroller.common.layout.align.Align
import top.fifthlight.touchcontroller.common.ui.config.model.LocalConfigScreenModel
import top.fifthlight.touchcontroller.common.ui.config.tab.Tab
import top.fifthlight.touchcontroller.common.ui.config.tab.TabGroup
import top.fifthlight.touchcontroller.common.ui.config.tab.TabOptions
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.all.allCustomTabs
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.model.CustomControlLayoutTabModel
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.presets.PresetsTab
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.state.CustomControlLayoutTabState
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.tab.CustomTab
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.tab.CustomTabContext
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.tab.LocalCustomTabContext
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.provider.CustomTabProvider
import top.fifthlight.touchcontroller.common.ui.control.ControllerWidget
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme
import top.fifthlight.touchcontroller.common.ui.widget.Scaffold
import top.fifthlight.touchcontroller.common.ui.widget.navigation.AppBar
import top.fifthlight.touchcontroller.common.ui.widget.navigation.BackButton
import top.fifthlight.touchcontroller.common.ui.widget.navigation.TouchControllerNavigator

private data class ControllerWidgetParentData(
    val align: Align,
    val offset: IntOffset,
    val size: IntSize,
)

private data class WidgetDataModifierNode(
    val align: Align,
    val offset: IntOffset,
    val size: IntSize,
) : ParentDataModifierNode, Modifier.Node<WidgetDataModifierNode> {
    constructor(widget: ControllerWidget, offset: IntOffset? = null) : this(
        widget.align,
        offset ?: widget.offset,
        widget.size()
    )

    override fun modifierParentData(parentData: Any?): ControllerWidgetParentData {
        return ControllerWidgetParentData(
            align = align,
            offset = offset,
            size = size
        )
    }
}

@Composable
private fun LayoutEditorPanel(
    modifier: Modifier = Modifier,
    selectedWidgetIndex: Int = -1,
    onSelectedWidgetChanged: (Int) -> Unit = { _ -> },
    layer: LayoutLayer,
    layerIndex: Int,
    lockMoving: Boolean = false,
    highlight: Boolean = false,
    onWidgetChanged: (Int, ControllerWidget) -> Unit = { _, _ -> },
    onEmptyAreaClicked: () -> Unit = {},
) {
    val selectedWidget = layer.widgets.getOrNull(selectedWidgetIndex)
    var panelSize by remember { mutableStateOf(IntSize.ZERO) }
    Layout(
        modifier = Modifier
            .consumePress {
                onSelectedWidgetChanged(-1)
                onEmptyAreaClicked()
            }
            .then(modifier),
        measurePolicy = { measurables, constraints ->
            val childConstraint = constraints.copy(minWidth = 0, minHeight = 0)
            val placeables = measurables.map { it.measure(childConstraint) }

            val width = constraints.maxWidth
            val height = constraints.maxHeight
            panelSize = IntSize(width, height)
            layout(width, height) {
                placeables.forEachIndexed { index, placeable ->
                    val parentData = measurables[index].parentData as ControllerWidgetParentData
                    placeable.placeAt(
                        parentData.align.alignOffset(
                            windowSize = IntSize(width, height),
                            size = parentData.size,
                            offset = parentData.offset
                        )
                    )
                }
            }
        }
    ) {
        fun Int.safeCoerceIn(min: Int, max: Int): Int = if (min > max) {
            min
        } else {
            coerceIn(min, max)
        }

        fun clampOffset(align: Align, widgetSize: IntSize, widgetOffset: IntOffset) = IntOffset(
            x = when (align) {
                Align.LEFT_TOP, Align.LEFT_CENTER, Align.LEFT_BOTTOM ->
                    widgetOffset.x.safeCoerceIn(0, panelSize.width - widgetSize.width)

                Align.CENTER_CENTER, Align.CENTER_BOTTOM, Align.CENTER_TOP ->
                    widgetOffset.x.safeCoerceIn(
                        -panelSize.width / 2 + widgetSize.width / 2,
                        panelSize.width / 2 - widgetSize.width / 2
                    )

                Align.RIGHT_TOP, Align.RIGHT_CENTER, Align.RIGHT_BOTTOM ->
                    widgetOffset.x.safeCoerceIn(0, panelSize.width - widgetSize.width)
            },
            y = when (align) {
                Align.LEFT_TOP, Align.CENTER_TOP, Align.RIGHT_TOP ->
                    widgetOffset.y.safeCoerceIn(0, panelSize.height - widgetSize.height)

                Align.LEFT_CENTER, Align.CENTER_CENTER, Align.RIGHT_CENTER ->
                    widgetOffset.y.safeCoerceIn(
                        -panelSize.height / 2 + widgetSize.height / 2,
                        panelSize.height / 2 - widgetSize.height / 2
                    )

                Align.LEFT_BOTTOM, Align.CENTER_BOTTOM, Align.RIGHT_BOTTOM ->
                    widgetOffset.y.safeCoerceIn(0, panelSize.height - widgetSize.height)
            }
        )

        var dragTotalOffset by remember(selectedWidgetIndex, layerIndex) { mutableStateOf(Offset.ZERO) }
        LaunchedEffect(selectedWidget) {
            dragTotalOffset = Offset.ZERO
        }

        for ((index, widget) in layer.widgets.withIndex()) {
            val lockWidgetMoving = lockMoving || widget.lockMoving
            var modifier = if (index == selectedWidgetIndex) {
                if (lockWidgetMoving) {
                    Modifier.innerLine(Colors.RED)
                } else {
                    Modifier.innerLine(Colors.WHITE)
                }
            } else if (highlight) {
                Modifier.innerLine(Colors.YELLOW)
            } else {
                Modifier
            }

            val widgetOffset = if (index == selectedWidgetIndex) {
                val dragIntOffset = dragTotalOffset.toIntOffset()
                val normalizedOffset = widget.align.normalizeOffset(dragIntOffset)
                normalizedOffset + widget.offset
            } else {
                null
            }

            if (!lockWidgetMoving) {
                modifier = modifier.draggable(
                    onDrag = { relative, _ ->
                        if (index != selectedWidgetIndex) {
                            onSelectedWidgetChanged(index)
                        }
                        dragTotalOffset += relative
                    },
                    onRelease = { _, _ ->
                        val widgetOffset = widgetOffset ?: return@draggable
                        val widgetSize = widget.size()
                        val clampedOffset = clampOffset(widget.align, widgetSize, widgetOffset)
                        val newWidget = widget.cloneBase(offset = clampedOffset)
                        dragTotalOffset = Offset.ZERO
                        onWidgetChanged(index, newWidget)
                    }
                )
            } else {
                modifier = modifier.draggable {
                    if (index != selectedWidgetIndex) {
                        onSelectedWidgetChanged(index)
                    }
                }
            }
            ControllerWidget(
                modifier = Modifier
                    .then(WidgetDataModifierNode(widget, widgetOffset))
                    .then(modifier),
                widget = widget
            )
        }
    }
}

@Composable
private fun SideBar(
    onTabSelected: (CustomTab) -> Unit,
    allTabs: PersistentList<CustomTab>,
    selectedTab: CustomTab? = null,
) {
    for (tab in allTabs) {
        IconButton(
            selected = selectedTab == tab,
            onClick = {
                onTabSelected(tab)
            },
        ) {
            tab.Icon()
        }
    }
}

object CustomControlLayoutTab : Tab() {
    override val options = TabOptions(
        titleId = Texts.SCREEN_CONFIG_LAYOUT_CUSTOM_CONTROL_LAYOUT,
        group = TabGroup.LayoutGroup,
        index = 1,
        openAsScreen = true,
    )

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val configScreenModel = LocalConfigScreenModel.current
        val screenModel = rememberScreenModel { CustomControlLayoutTabModel(configScreenModel) }
        val currentUiState by screenModel.uiState.collectAsState()
        val uiState = currentUiState
        if (uiState is CustomControlLayoutTabState.Enabled) {
            Scaffold(
                topBar = {
                    AppBar(
                        modifier = Modifier.fillMaxWidth(),
                        leading = {
                            BackButton(
                                screenName = Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_TITLE),
                            )

                            val copiedWidget = uiState.pageState.copiedWidget
                            Button(
                                onClick = {
                                    copiedWidget?.let(screenModel::newWidget)
                                },
                                enabled = copiedWidget != null
                            ) {
                                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PASTE_WIDGET))
                            }

                            val widgetIndex = uiState.selectedLayer?.widgets?.indices?.let { indices ->
                                uiState.pageState.selectedWidgetIndex.takeIf { it in indices }
                            }
                            WarningButton(
                                onClick = {
                                    widgetIndex?.let(screenModel::deleteWidget)
                                },
                                enabled = widgetIndex != null
                            ) {
                                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_DELETE_WIDGET))
                            }
                        },
                        trailing = {
                            CheckBoxButton(
                                checked = uiState.pageState.showSideBar,
                                onClick = {
                                    screenModel.setShowSideBar(!uiState.pageState.showSideBar)
                                },
                            ) {
                                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_SHOW_SIDE_BAR))
                            }
                            IconButton(
                                enabled = uiState.pageState.editState?.undoStack?.haveUndoItem == true,
                                onClick = { screenModel.undo() },
                            ) {
                                Icon(Textures.icon_undo)
                            }
                            IconButton(
                                enabled = uiState.pageState.editState?.undoStack?.haveRedoItem == true,
                                onClick = { screenModel.redo() },
                            ) {
                                Icon(Textures.icon_redo)
                            }
                        }
                    )
                },
            ) { modifier ->
                var anchor by remember { mutableStateOf(IntRect.ZERO) }
                Box(
                    modifier = Modifier
                        .background(LocalTouchControllerTheme.current.background)
                        .anchor { anchor = it }
                        .then(modifier),
                    alignment = Alignment.Center,
                ) {
                    var sideBarNavigator by mutableStateOf<Navigator?>(null)

                    val selectedLayer = uiState.selectedLayer
                    val selectedPreset = uiState.selectedPreset
                    if (selectedLayer != null) {
                        LayoutEditorPanel(
                            modifier = Modifier.fillMaxSize(),
                            selectedWidgetIndex = uiState.pageState.selectedWidgetIndex,
                            onSelectedWidgetChanged = {
                                screenModel.selectWidget(it)
                                if (!uiState.pageState.showSideBar && uiState.pageState.sideBarAutoToggle) {
                                    screenModel.setShowSideBar(
                                        showSideBar = true,
                                        autoToggle = true
                                    )
                                }
                            },
                            layer = selectedLayer,
                            layerIndex = uiState.pageState.selectedLayerIndex,
                            lockMoving = uiState.pageState.moveLocked,
                            highlight = uiState.pageState.highlight,
                            onWidgetChanged = screenModel::editWidget,
                            onEmptyAreaClicked = {
                                if (uiState.pageState.showSideBar) {
                                    screenModel.setShowSideBar(
                                        showSideBar = false,
                                        autoToggle = true,
                                    )
                                }
                            },
                        )
                    } else if (selectedPreset != null) {
                        Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_NO_LAYER_SELECTED))
                        Button(
                            modifier = Modifier
                                .padding(bottom = 16)
                                .alignment(Alignment.BottomCenter),
                            onClick = {
                                screenModel.setShowSideBar(true)
                                sideBarNavigator?.replace(PresetsTab)
                            },
                        ) {
                            Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_EDIT_LAYERS))
                        }
                    } else {
                        Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_NO_PRESET_SELECTED))
                        Button(
                            modifier = Modifier
                                .padding(bottom = 16)
                                .alignment(Alignment.BottomCenter),
                            onClick = {
                                screenModel.setShowSideBar(true)
                                sideBarNavigator?.replace(PresetsTab)
                            },
                        ) {
                            Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_EDIT_PRESETS))
                        }
                    }

                    TouchControllerNavigator(allCustomTabs.first()) { innerNavigator ->
                        SideEffect {
                            sideBarNavigator = innerNavigator
                        }
                        val currentScreen = innerNavigator.lastItem

                        @Composable
                        fun SideBar() = SideBar(
                            allTabs = allCustomTabs,
                            onTabSelected = innerNavigator::replace,
                            selectedTab = currentScreen as? CustomTab,
                        )

                        val sideBarAtRight by remember(uiState.selectedWidget, anchor) {
                            derivedStateOf {
                                uiState.selectedWidget?.let { widget ->
                                    val editAreaSize = anchor.size
                                    val widgetSize = widget.size()
                                    val offset =
                                        widget.align.alignOffset(editAreaSize, widget.size(), widget.offset)
                                    val centerOffset = offset + widgetSize / 2
                                    centerOffset.left < editAreaSize.width / 2
                                } != false
                            }
                        }

                        val sideBarProgress by animateFloatAsState(
                            targetValue = if (!uiState.pageState.showSideBar) {
                                .5f
                            } else if (sideBarAtRight) {
                                1f
                            } else {
                                0f
                            },
                        )
                        val currentCustomTabContext = CustomTabContext(
                            screenModel = screenModel,
                            uiState = uiState,
                            tabsButton = @Composable { SideBar() },
                            sideBarAtRight = sideBarProgress > .5f,
                            parentNavigator = navigator,
                        )
                        if (uiState.pageState.showSideBar || sideBarProgress != .5f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(.4f)
                                    .fillMaxHeight()
                                    .alignment(
                                        if (sideBarProgress > .5f) {
                                            Alignment.CenterRight
                                        } else {
                                            Alignment.CenterLeft
                                        }
                                    )
                                    .offset(
                                        x = if (sideBarProgress > .5f) {
                                            1f + (.5f - sideBarProgress) * 2f
                                        } else {
                                            -sideBarProgress * 2
                                        }
                                    )
                                    .consumePress(),
                            ) {
                                CompositionLocalProvider(
                                    LocalCustomTabContext provides currentCustomTabContext,
                                ) {
                                    CurrentScreen()
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Scaffold(
                topBar = {
                    AppBar(
                        modifier = Modifier.fillMaxWidth(),
                        leading = {
                            BackButton(
                                screenName = Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_TITLE),
                            )
                        },
                    )
                },
            ) { modifier ->
                Box(
                    modifier = Modifier
                        .background(LocalTouchControllerTheme.current.background)
                        .then(modifier),
                    alignment = Alignment.Center,
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12)
                            .border(LocalTouchControllerTheme.current.borderBackgroundDark),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12),
                    ) {
                        Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_SWITCH_MESSAGE))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12),
                        ) {
                            WarningButton(
                                onClick = {
                                    screenModel.enableCustomLayout()
                                }
                            ) {
                                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_SWITCH_SWITCH))
                            }
                            GuideButton(
                                onClick = {
                                    navigator?.replace(CustomTabProvider.presetTab)
                                }
                            ) {
                                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_SWITCH_GOTO_PRESET))
                            }
                        }
                    }
                }
            }
        }
    }
}
