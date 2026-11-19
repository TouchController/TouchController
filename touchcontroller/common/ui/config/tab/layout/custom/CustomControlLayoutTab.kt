/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom

import androidx.compose.runtime.*
import kotlinx.collections.immutable.PersistentList
import top.fifthlight.combine.core.animation.animateFloatAsState
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.layout.Alignment
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.drawing.background
import top.fifthlight.combine.core.modifier.drawing.border
import top.fifthlight.combine.core.modifier.placement.*
import top.fifthlight.combine.core.modifier.pointer.consumePress
import top.fifthlight.combine.core.widget.layout.Box
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.core.widget.layout.Row
import top.fifthlight.combine.theme.blackstone.widget.CheckBoxButton
import top.fifthlight.combine.widget.*
import top.fifthlight.data.IntRect
import top.fifthlight.data.IntSize
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.assets.texture.Textures
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.component.CustomControlLayoutScreenComponent
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.component.CustomControlLayoutTabComponent
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.layers.LayersTab
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.layers.LayersTabActions
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.presets.PresetsTab
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.presets.PresetsTabActions
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.properties.PropertiesTab
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.properties.PropertiesTabActions
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.state.CustomControlLayoutTabState
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.tab.CustomTabComponent
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.tab.CustomTabConfig
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.widget.SideBarContainer
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.widgets.WidgetsTab
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.widgets.WidgetsTabActions
import top.fifthlight.touchcontroller.common.ui.state.ImmutableValue
import top.fifthlight.touchcontroller.common.ui.state.subscribeAsState
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme
import top.fifthlight.touchcontroller.common.ui.widget.Scaffold
import top.fifthlight.touchcontroller.common.ui.widget.navigation.AppBar
import top.fifthlight.touchcontroller.common.ui.widget.navigation.BackButton

@Composable
private fun SideBar(
    onTabSelected: (CustomTabConfig) -> Unit,
    allTabs: PersistentList<CustomTabConfig>,
    selectedTab: CustomTabConfig? = null,
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

@Composable
private fun NotInCustomModeScreen(
    onBack: () -> Unit,
    openCustomLayout: () -> Unit,
    openControlPresets: () -> Unit,
) {
    Scaffold(
        topBar = {
            AppBar(
                modifier = Modifier.fillMaxWidth(),
                leading = {
                    BackButton(
                        screenName = Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_TITLE),
                        onClick = onBack,
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
                    WarningButton(onClick = openCustomLayout) {
                        Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_SWITCH_SWITCH))
                    }
                    GuideButton(onClick = openControlPresets) {
                        Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_SWITCH_GOTO_PRESET))
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomTabContent(
    component: CustomControlLayoutScreenComponent,
    instance: CustomTabComponent?,
    uiState: CustomControlLayoutTabState,
    editAreaSize: State<IntSize>,
) {
    when (instance) {
        is CustomTabComponent.Properties -> PropertiesTab(
            selectedWidget = uiState.selectedWidget,
            selectedWidgetIndex = uiState.selectedWidgetIndex,
            editAreaSize = editAreaSize,
            presetControlInfo = uiState.selectedPreset?.controlInfo,
            onWidgetEdited = component::editWidget,
            onWidgetCopied = component::copyWidget,
            onWidgetDeleted = component::deleteWidget,
        )

        is CustomTabComponent.Layers -> LayersTab(instance.component)

        is CustomTabComponent.Widgets -> WidgetsTab(instance.component)

        is CustomTabComponent.Presets -> PresetsTab(instance.component)

        null -> {}
    }
}

@Composable
private fun CustomControlScreen(component: CustomControlLayoutScreenComponent) {
    val uiState by component.uiState.subscribeAsState()
    val childPages by component.childPages.subscribeAsState()
    val selectedPage = childPages.items[childPages.selectedIndex]
    val tabShowUi by (selectedPage.instance?.showTabUi ?: ImmutableValue(true)).subscribeAsState()
    val anchor = remember { mutableStateOf(IntRect.ZERO) }
    var anchorValue by anchor
    val editAreaSize = remember { derivedStateOf { anchorValue.size } }
    Scaffold(
        topBar = {
            if (tabShowUi) {
                AppBar(
                    modifier = Modifier.fillMaxWidth(),
                    leading = {
                        BackButton(
                            screenName = Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_TITLE),
                            onClick = component::onBack,
                        )

                        val copiedWidget = uiState.copiedWidget
                        Button(
                            onClick = {
                                copiedWidget?.let(component::newWidget)
                            },
                            enabled = copiedWidget != null
                        ) {
                            Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PASTE_WIDGET))
                        }

                        val widgetIndex = uiState.selectedLayer?.widgets?.indices?.let { indices ->
                            uiState.selectedWidgetIndex.takeIf { it in indices }
                        }
                        WarningButton(
                            onClick = {
                                widgetIndex?.let(component::deleteWidget)
                            },
                            enabled = widgetIndex != null
                        ) {
                            Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_DELETE_WIDGET))
                        }
                    },
                    trailing = {
                        CheckBoxButton(
                            checked = uiState.showSideBar,
                            onClick = {
                                component.setShowSideBar(!uiState.showSideBar)
                            },
                        ) {
                            Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_SHOW_SIDE_BAR))
                        }
                        IconButton(
                            enabled = uiState.undoStack?.haveUndoItem == true,
                            onClick = component::undo,
                        ) {
                            Icon(Textures.icon_undo)
                        }
                        IconButton(
                            enabled = uiState.undoStack?.haveRedoItem == true,
                            onClick = component::redo,
                        ) {
                            Icon(Textures.icon_redo)
                        }
                    }
                )
            }
        },
    ) { modifier ->
        if (!tabShowUi) {
            Box(
                modifier = Modifier
                    .background(LocalTouchControllerTheme.current.background)
                    .then(modifier),
                alignment = Alignment.Center,
            ) {
                CustomTabContent(component, selectedPage.instance, uiState, editAreaSize)
            }
        } else {
            Box(
                modifier = Modifier
                    .background(LocalTouchControllerTheme.current.background)
                    .anchor { anchorValue = it }
                    .then(modifier),
                alignment = Alignment.Center,
            ) {
                val selectedLayer = uiState.selectedLayer
                val selectedPreset = uiState.selectedPreset
                if (selectedLayer != null) {
                    LayoutEditorPanel(
                        modifier = Modifier.fillMaxSize(),
                        selectedWidgetIndex = uiState.selectedWidgetIndex,
                        onSelectedWidgetChanged = {
                            component.selectWidget(it)
                            if (!uiState.showSideBar && uiState.sideBarAutoToggle) {
                                component.setShowSideBar(
                                    showSideBar = true,
                                    autoToggle = true
                                )
                            }
                        },
                        layer = selectedLayer,
                        layerIndex = uiState.selectedLayerIndex,
                        lockMoving = uiState.moveLocked,
                        highlight = uiState.highlight,
                        onWidgetChanged = component::editWidget,
                        onEmptyAreaClicked = {
                            if (uiState.showSideBar) {
                                component.setShowSideBar(
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
                        onClick = { component.selectTab(CustomTabConfig.Layers) },
                    ) {
                        Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_EDIT_LAYERS))
                    }
                } else {
                    Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_NO_PRESET_SELECTED))
                    Button(
                        modifier = Modifier
                            .padding(bottom = 16)
                            .alignment(Alignment.BottomCenter),
                        onClick = { component.selectTab(CustomTabConfig.Presets) },
                    ) {
                        Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_EDIT_PRESETS))
                    }
                }

                val sideBarAtRight by remember(uiState.selectedWidget, anchor) {
                    derivedStateOf {
                        uiState.selectedWidget?.let { widget ->
                            val editAreaSize = anchorValue.size
                            val widgetSize = widget.size()
                            val offset =
                                widget.align.alignOffset(editAreaSize, widget.size(), widget.offset)
                            val centerOffset = offset + widgetSize / 2
                            centerOffset.left <= editAreaSize.width / 2
                        } != false
                    }
                }

                val sideBarProgress by animateFloatAsState(
                    targetValue = if (!uiState.showSideBar) {
                        .5f
                    } else if (sideBarAtRight) {
                        1f
                    } else {
                        0f
                    },
                )
                if (uiState.showSideBar || sideBarProgress != .5f) {
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
                        SideBarContainer(
                            sideBarAtRight = sideBarAtRight,
                            tabsButton = {
                                SideBar(
                                    onTabSelected = component::selectTab,
                                    allTabs = CustomTabConfig.all,
                                    selectedTab = selectedPage.configuration,
                                )
                            },
                            actions = {
                                when (val instance = selectedPage.instance) {
                                    is CustomTabComponent.Properties -> PropertiesTabActions(
                                        moveLocked = uiState.moveLocked,
                                        highlight = uiState.highlight,
                                        presetSelected = uiState.selectedPreset != null,
                                        selectedWidget = uiState.selectedWidget,
                                        onMoveLockedChanged = component::setMoveLocked,
                                        onHighlightChanged = component::setHighlight,
                                        onAddWidgetPreset = component::addWidgetPreset,
                                    )

                                    is CustomTabComponent.Layers -> LayersTabActions(instance.component)

                                    is CustomTabComponent.Widgets -> WidgetsTabActions(instance.component)

                                    is CustomTabComponent.Presets -> PresetsTabActions(instance.component)

                                    null -> {}
                                }
                            },
                            content = {
                                CustomTabContent(component, selectedPage.instance, uiState, editAreaSize)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomControlLayoutTab(component: CustomControlLayoutTabComponent) {
    val pages by component.childPages.subscribeAsState()
    val instance = pages.items[pages.selectedIndex].instance
    if (instance is CustomControlLayoutTabComponent.ChildComponent.Custom) {
        CustomControlScreen(instance.component)
    } else {
        NotInCustomModeScreen(
            onBack = component::onBack,
            openControlPresets = component::openControlPresets,
            openCustomLayout = component::openCustomLayout,
        )
    }
}
