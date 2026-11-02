/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.screen

import androidx.compose.runtime.*
import cafe.adriel.voyager.navigator.CurrentScreen
import kotlinx.collections.immutable.persistentListOf
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.layout.Alignment
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.placement.fillMaxHeight
import top.fifthlight.combine.core.modifier.placement.fillMaxWidth
import top.fifthlight.combine.core.modifier.placement.minWidth
import top.fifthlight.combine.core.screen.LocalCloseHandler
import top.fifthlight.combine.core.screen.ScreenFactory
import top.fifthlight.combine.core.widget.layout.Box
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.core.widget.layout.Row
import top.fifthlight.combine.theme.blackstone.widget.AlertDialog
import top.fifthlight.combine.widget.Button
import top.fifthlight.combine.widget.GuideButton
import top.fifthlight.combine.widget.Text
import top.fifthlight.combine.widget.WarningButton
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.common.config.PresetConfig
import top.fifthlight.touchcontroller.common.ui.config.model.ConfigScreenModel
import top.fifthlight.touchcontroller.common.ui.config.model.LocalConfigScreenModel
import top.fifthlight.touchcontroller.common.ui.config.tab.OnResetHandler
import top.fifthlight.touchcontroller.common.ui.config.tab.Tab
import top.fifthlight.touchcontroller.common.ui.config.tab.all.allTabGroups
import top.fifthlight.touchcontroller.common.ui.config.tab.all.getAllTabs
import top.fifthlight.touchcontroller.common.ui.config.tab.component.SideTabBar
import top.fifthlight.touchcontroller.common.ui.config.tab.general.RegularTab
import top.fifthlight.touchcontroller.common.ui.theme.TouchControllerTheme
import top.fifthlight.touchcontroller.common.ui.widget.Scaffold
import top.fifthlight.touchcontroller.common.ui.widget.navigation.AppBar
import top.fifthlight.touchcontroller.common.ui.widget.navigation.BackButton
import top.fifthlight.touchcontroller.common.ui.widget.navigation.TouchControllerNavigator

@Composable
private fun ConfigScreen() {
    val closeHandler = LocalCloseHandler.current
    val screenModel = remember { ConfigScreenModel() }
    DisposableEffect(screenModel) {
        onDispose {
            screenModel.onDispose()
        }
    }

    TouchControllerTheme {
        val uiState by screenModel.uiState.collectAsState()
        AlertDialog(
            visible = uiState.developmentWarningDialog,
            onDismissRequest = {
                screenModel.closeDevelopmentDialog()
            },
            title = {
                Text(Text.translatable(Texts.WARNING_DEVELOPMENT_VERSION_TITLE))
            },
            actions = {
                GuideButton(onClick = { screenModel.closeDevelopmentDialog() }) {
                    Text(Text.translatable(Texts.WARNING_DEVELOPMENT_VERSION_OK))
                }
            },
        ) {
            Text(Text.translatable(Texts.WARNING_DEVELOPMENT_VERSION_MESSAGE))
        }

        val tabGroups = remember {
            val allTabs = getAllTabs(screenModel)
            (persistentListOf(null) + allTabGroups).map { group ->
                Pair(group, allTabs.filter { it.options.group == group }.sortedBy { it.options.index })
            }
        }

        CompositionLocalProvider(LocalConfigScreenModel provides screenModel) {
            TouchControllerNavigator(RegularTab) { navigator ->
                val currentTab = (navigator.lastItem as? Tab)?.takeIf { !it.options.openAsScreen }
                currentTab?.let {
                    var onResetTab by remember { mutableStateOf<OnResetHandler?>(null) }
                    AlertDialog(
                        value = onResetTab,
                        valueTransformer = { it },
                        modifier = Modifier
                            .fillMaxWidth(.4f)
                            .minWidth(230),
                        onDismissRequest = {
                            onResetTab = null
                        },
                        title = {
                            Text(Text.translatable(Texts.SCREEN_CONFIG_RESET_TITLE))
                        }
                    ) { currentOnResetTab ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4),
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Button(
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        screenModel.updateConfig(currentOnResetTab)
                                        onResetTab = null
                                    }
                                ) {
                                    Text(Text.translatable(Texts.SCREEN_CONFIG_RESET_CURRENT_TAB))
                                }
                                Button(
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        screenModel.updateConfig {
                                            copy(preset = PresetConfig.BuiltIn())
                                        }
                                        onResetTab = null
                                    }
                                ) {
                                    Text(Text.translatable(Texts.SCREEN_CONFIG_RESET_LAYOUT_SETTINGS))
                                }
                            }
                            WarningButton(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    screenModel.resetConfig()
                                    onResetTab = null
                                }
                            ) {
                                Text(Text.translatable(Texts.SCREEN_CONFIG_RESET_ALL_SETTINGS))
                            }
                            GuideButton(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    onResetTab = null
                                }
                            ) {
                                Text(Text.translatable(Texts.SCREEN_CONFIG_RESET_CANCEL))
                            }
                        }
                    }

                    Scaffold(
                        topBar = {
                            AppBar(
                                modifier = Modifier.fillMaxWidth(),
                                leading = {
                                    BackButton(
                                        screenName = Text.translatable(Texts.SCREEN_CONFIG_TITLE),
                                    )
                                },
                                title = {
                                    Text(currentTab.options.title)
                                },
                                trailing = {
                                    val onReset = currentTab.options.onReset
                                    if (onReset != null) {
                                        WarningButton(
                                            onClick = {
                                                onResetTab = onReset
                                            }
                                        ) {
                                            Text(Text.translatable(Texts.SCREEN_CONFIG_RESET))
                                        }
                                        Button(
                                            onClick = {
                                                screenModel.undoConfig()
                                            }
                                        ) {
                                            Text(Text.translatable(Texts.SCREEN_CONFIG_UNDO))
                                        }
                                        Button(
                                            onClick = {
                                                screenModel.undoConfig()
                                                closeHandler.close()
                                            }
                                        ) {
                                            Text(Text.translatable(Texts.SCREEN_CONFIG_CANCEL))
                                        }
                                    }
                                }
                            )
                        },
                        leftSideBar = {
                            SideTabBar(
                                modifier = Modifier.fillMaxHeight(),
                                onTabSelected = { tab, options ->
                                    if (options.openAsScreen) {
                                        navigator.push(tab)
                                    } else {
                                        navigator.replace(tab)
                                    }
                                },
                                tabGroups = tabGroups,
                            )
                        },
                    ) { modifier ->
                        Box(modifier) {
                            CurrentScreen()
                        }
                    }
                } ?: run {
                    CurrentScreen()
                }
            }
        }
    }
}

fun getConfigScreenButtonText(): Text = Text.translatable(Texts.SCREEN_CONFIG)

fun getConfigScreen(parent: Any?): Any {
    return ScreenFactory.getScreen(
        parent = parent,
        renderBackground = false,
        title = Text.translatable(Texts.SCREEN_CONFIG_TITLE),
        content = { ConfigScreen() },
    )
}
