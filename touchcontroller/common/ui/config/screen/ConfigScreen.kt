/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus
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
import top.fifthlight.touchcontroller.buildinfo.BuildInfo
import top.fifthlight.touchcontroller.common.config.holder.GlobalConfigHolder
import top.fifthlight.touchcontroller.common.config.preset.manager.PresetManager
import top.fifthlight.touchcontroller.common.config.widget.WidgetPresetManager
import top.fifthlight.touchcontroller.common.ui.config.component.ConfigComponent
import top.fifthlight.touchcontroller.common.ui.config.tab.TabComponent
import top.fifthlight.touchcontroller.common.ui.config.tab.TabConfig
import top.fifthlight.touchcontroller.common.ui.config.tab.TabGroup
import top.fifthlight.touchcontroller.common.ui.config.tab.about.AboutTab
import top.fifthlight.touchcontroller.common.ui.config.tab.general.ControlTab
import top.fifthlight.touchcontroller.common.ui.config.tab.general.DebugTab
import top.fifthlight.touchcontroller.common.ui.config.tab.general.RegularTab
import top.fifthlight.touchcontroller.common.ui.config.tab.general.TouchRingTab
import top.fifthlight.touchcontroller.common.ui.config.tab.item.ItemTab
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.CustomControlLayoutTab
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.preset.ManageControlPresetsTab
import top.fifthlight.touchcontroller.common.ui.config.tab.platform.SdlConfigTab
import top.fifthlight.touchcontroller.common.ui.config.tab.status.StatusTab
import top.fifthlight.touchcontroller.common.ui.config.tab.widget.SideTabBar
import top.fifthlight.touchcontroller.common.ui.navigation.BackController
import top.fifthlight.touchcontroller.common.ui.navigation.LifecycleController
import top.fifthlight.touchcontroller.common.ui.state.asValue
import top.fifthlight.touchcontroller.common.ui.state.subscribeAsState
import top.fifthlight.touchcontroller.common.ui.theme.TouchControllerTheme
import top.fifthlight.touchcontroller.common.ui.widget.Scaffold
import top.fifthlight.touchcontroller.common.ui.widget.navigation.AppBar
import top.fifthlight.touchcontroller.common.ui.widget.navigation.BackButton

@Composable
private fun DevelopmentWarningDialog(
    visible: Boolean,
    onClose: () -> Unit,
) = AlertDialog(
    visible = visible,
    onDismissRequest = onClose,
    title = {
        Text(Text.translatable(Texts.WARNING_DEVELOPMENT_VERSION_TITLE))
    },
    actions = {
        GuideButton(onClick = onClose) {
            Text(Text.translatable(Texts.WARNING_DEVELOPMENT_VERSION_OK))
        }
    },
) {
    Text(Text.translatable(Texts.WARNING_DEVELOPMENT_VERSION_MESSAGE))
}

@Composable
private fun ResetDialog(
    visible: Boolean,
    component: ConfigComponent,
    onResetCurrentTab: () -> Unit,
    onClose: () -> Unit,
) = AlertDialog(
    visible = visible,
    modifier = Modifier
        .fillMaxWidth(.4f)
        .minWidth(230),
    onDismissRequest = onClose,
    title = {
        Text(Text.translatable(Texts.SCREEN_CONFIG_RESET_TITLE))
    },
) {
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
                    onResetCurrentTab()
                    onClose()
                }
            ) {
                Text(Text.translatable(Texts.SCREEN_CONFIG_RESET_CURRENT_TAB))
            }
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    component.resetLayoutSettings()
                    onClose()
                }
            ) {
                Text(Text.translatable(Texts.SCREEN_CONFIG_RESET_LAYOUT_SETTINGS))
            }
        }
        WarningButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                component.resetConfig()
                onClose()
            }
        ) {
            Text(Text.translatable(Texts.SCREEN_CONFIG_RESET_ALL_SETTINGS))
        }
        GuideButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onClose,
        ) {
            Text(Text.translatable(Texts.SCREEN_CONFIG_RESET_CANCEL))
        }
    }
}

@Composable
private fun ConfigScreen(component: ConfigComponent) {
    TouchControllerTheme {
        val tabGroups = remember {
            (persistentListOf(null) + TabGroup.all).map { group ->
                Pair(group, TabConfig.all.filter { it.group == group }.sortedBy { it.index })
            }
        }

        val tabs by component.childTabs.subscribeAsState()
        val active = tabs.active ?: return@TouchControllerTheme
        val instance = active.instance
        val tabShowUi by instance.showTabUi.subscribeAsState()
        val showTabUi = tabs.screen == null && tabShowUi

        val dialog by component.dialog.subscribeAsState()
        val dialogInstance = dialog.child?.instance
        DevelopmentWarningDialog(
            visible = dialogInstance is ConfigComponent.ChildComponent.DevelopmentWarning,
            onClose = component::closeDevelopmentDialog,
        )
        ResetDialog(
            visible = dialogInstance is ConfigComponent.ChildComponent.Reset,
            component = component,
            onResetCurrentTab = { instance.onReset?.invoke() },
            onClose = component::closeResetDialog,
        )

        Scaffold(
            topBar = {
                if (showTabUi) {
                    AppBar(
                        modifier = Modifier.fillMaxWidth(),
                        leading = {
                            BackButton(
                                screenName = Text.translatable(Texts.SCREEN_CONFIG_TITLE),
                                onClick = {
                                    component.onBack()
                                },
                            )
                        },
                        title = {
                            Text(active.configuration.title)
                        },
                        trailing = {
                            val onReset = instance.onReset
                            if (onReset != null) {
                                WarningButton(
                                    onClick = {
                                        component.openResetDialog()
                                    }
                                ) {
                                    Text(Text.translatable(Texts.SCREEN_CONFIG_RESET))
                                }
                                Button(
                                    onClick = {
                                        component.undoConfig()
                                    }
                                ) {
                                    Text(Text.translatable(Texts.SCREEN_CONFIG_UNDO))
                                }
                                Button(
                                    onClick = {
                                        component.undoConfig()
                                        component.onBack()
                                    }
                                ) {
                                    Text(Text.translatable(Texts.SCREEN_CONFIG_CANCEL))
                                }
                            }
                        }
                    )
                }
            },
            leftSideBar = {
                if (showTabUi) {
                    SideTabBar(
                        modifier = Modifier.fillMaxHeight(),
                        selectedTab = active.configuration,
                        onTabSelected = component::selectTab,
                        tabGroups = tabGroups,
                    )
                }
            },
        ) { modifier ->
            Box(modifier) {
                when (instance) {
                    is TabComponent.Sdl -> SdlConfigTab(instance)
                    is TabComponent.About -> AboutTab(component = instance.component)
                    is TabComponent.Status -> StatusTab(component = instance.component)
                    is TabComponent.ManageControlPresets -> ManageControlPresetsTab(component = instance.component)
                    is TabComponent.CustomControlLayout -> CustomControlLayoutTab(component = instance.component)
                    is TabComponent.Regular -> RegularTab(instance)
                    is TabComponent.Control -> ControlTab(instance)
                    is TabComponent.TouchRing -> TouchRingTab(instance)
                    is TabComponent.Debug -> DebugTab(instance)
                    is TabComponent.ItemList -> ItemTab(component = instance.component)
                }
            }
        }
    }
}

@Suppress("SimplifyBooleanWithConstants", "KotlinConstantConditions")
private var developmentWarningDialog: Boolean = BuildInfo.MOD_STATE != "release"

fun getConfigScreenButtonText(): Text = Text.translatable(Texts.SCREEN_CONFIG)

fun getConfigScreen(parent: Any?): Any {
    val lifecycleRegistry = LifecycleRegistry()
    val backDispatcher = BackDispatcher()

    return ScreenFactory.getScreen(
        parent = parent,
        renderBackground = false,
        title = Text.translatable(Texts.SCREEN_CONFIG_TITLE),
        content = {
            val scope = rememberCoroutineScope()
            val closeHandler = LocalCloseHandler.current
            val viewModel = remember(closeHandler) {
                ConfigComponent(
                    componentContext = DefaultComponentContext(
                        lifecycle = lifecycleRegistry,
                        backHandler = backDispatcher,
                    ),
                    initialConfig = GlobalConfigHolder.config.value,
                    onConfigUpdated = { newConfig ->
                        GlobalConfigHolder.updateConfig { newConfig }
                    },
                    presets = PresetManager.presets.asValue(scope),
                    onPresetSaved = PresetManager::savePreset,
                    onPresetDeleted = PresetManager::removePreset,
                    onPresetOrderChanged = PresetManager::saveOrder,
                    onAddWidgetPreset = { widget ->
                        WidgetPresetManager.save(WidgetPresetManager.presets.value + widget)
                    },
                    widgetPresets = WidgetPresetManager.presets.asValue(scope),
                    onWidgetPresetsSaved = WidgetPresetManager::save,
                    developmentWarningDialog = developmentWarningDialog,
                    onDevelopmentWarningDialogClosed = {
                        developmentWarningDialog = false
                    },
                    onBack = {
                        closeHandler.close()
                    },
                )
            }

            BackController(backDispatcher)
            LifecycleController(lifecycleRegistry)

            ConfigScreen(viewModel)
        },
    )
}
