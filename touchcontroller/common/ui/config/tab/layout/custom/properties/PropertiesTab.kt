/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.properties

import androidx.compose.runtime.Composable
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.layout.Alignment
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.placement.fillMaxHeight
import top.fifthlight.combine.core.modifier.placement.fillMaxSize
import top.fifthlight.combine.core.modifier.placement.fillMaxWidth
import top.fifthlight.combine.core.modifier.placement.padding
import top.fifthlight.combine.core.modifier.scroll.verticalScroll
import top.fifthlight.combine.core.widget.layout.Box
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.widget.ui.Button
import top.fifthlight.combine.widget.ui.Icon
import top.fifthlight.combine.widget.ui.IconButton
import top.fifthlight.combine.widget.ui.Text
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.assets.texture.Textures
import top.fifthlight.touchcontroller.common.control.ControllerWidget
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.tab.CustomTab
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.tab.LocalCustomTabContext

object PropertiesTab : CustomTab() {
    @Composable
    override fun Icon() {
        Icon(Textures.icon_properties)
    }

    @Composable
    override fun Content() {
        val (screenModel, uiState, tabsButton, sideBarAtRight) = LocalCustomTabContext.current
        SideBarContainer(
            sideBarAtRight = sideBarAtRight,
            tabsButton = tabsButton,
            actions = {
                val moveLocked = uiState.pageState.moveLocked
                IconButton(
                    onClick = {
                        screenModel.setMoveLocked(!moveLocked)
                    },
                    selected = moveLocked,
                    enabled = uiState.selectedPreset != null,
                ) {
                    Icon(Textures.icon_lock)
                }

                val highlight = uiState.pageState.highlight
                IconButton(
                    onClick = {
                        screenModel.setHighlight(!highlight)
                    },
                    selected = highlight,
                    enabled = uiState.selectedPreset != null,
                ) {
                    Icon(Textures.icon_light)
                }

                IconButton(
                    enabled = uiState.selectedWidget != null,
                    onClick = {
                        uiState.selectedWidget?.let(screenModel::addWidgetPreset)
                    },
                ) {
                    Icon(Textures.icon_save)
                }
            }
        ) { modifier ->
            val selectedWidget = uiState.selectedWidget
            SideBarScaffold(
                modifier = modifier,
                title = {
                    Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PROPERTIES))
                },
                actions = if (selectedWidget != null) {
                    {
                        Button(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            onClick = {
                                screenModel.copyWidget(selectedWidget)
                            }
                        ) {
                            Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_COPY))
                        }
                        Button(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            onClick = {
                                screenModel.copyWidget(selectedWidget)
                                screenModel.deleteWidget(uiState.pageState.selectedWidgetIndex)
                            }
                        ) {
                            Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_CUT))
                        }
                    }
                } else {
                    null
                }
            ) {
                if (selectedWidget != null) {
                    Column(
                        modifier = Modifier
                            .padding(4)
                            .verticalScroll()
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4),
                    ) {
                        for (property in selectedWidget.properties) {
                            property.controller(
                                modifier = Modifier.fillMaxWidth(),
                                config = selectedWidget,
                                context = ControllerWidget.Property.ConfigContext(
                                    presetControlInfo = uiState.selectedPreset?.controlInfo,
                                ),
                                onConfigChanged = { screenModel.editWidget(uiState.pageState.selectedWidgetIndex, it) }
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        alignment = Alignment.Center,
                    ) {
                        Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_NO_WIDGET_SELECTED))
                    }
                }
            }
        }
    }
}
