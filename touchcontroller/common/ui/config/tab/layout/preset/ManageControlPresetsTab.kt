/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.layout.preset

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.layout.Alignment
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.drawing.background
import top.fifthlight.combine.core.modifier.drawing.border
import top.fifthlight.combine.core.modifier.placement.fillMaxWidth
import top.fifthlight.combine.core.modifier.placement.padding
import top.fifthlight.combine.core.widget.layout.Box
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.core.widget.layout.Row
import top.fifthlight.combine.widget.ui.GuideButton
import top.fifthlight.combine.widget.ui.Text
import top.fifthlight.combine.widget.ui.WarningButton
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.common.config.PresetConfig
import top.fifthlight.touchcontroller.common.ui.component.BuiltInPresetKeySelector
import top.fifthlight.touchcontroller.common.ui.config.model.LocalConfigScreenModel
import top.fifthlight.touchcontroller.common.ui.config.tab.Tab
import top.fifthlight.touchcontroller.common.ui.config.tab.TabGroup
import top.fifthlight.touchcontroller.common.ui.config.tab.TabOptions
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.preset.model.ManageControlPresetsTabModel
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.provider.CustomTabProvider
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme
import top.fifthlight.touchcontroller.common.ui.widget.Scaffold
import top.fifthlight.touchcontroller.common.ui.widget.navigation.AppBar
import top.fifthlight.touchcontroller.common.ui.widget.navigation.BackButton

object ManageControlPresetsTab : Tab() {
    override val options = TabOptions(
        titleId = Texts.SCREEN_CONFIG_LAYOUT_MANAGE_CONTROL_PRESET,
        group = TabGroup.LayoutGroup,
        index = 0,
        openAsScreen = true,
    )

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        Scaffold(
            topBar = {
                AppBar(
                    modifier = Modifier.fillMaxWidth(),
                    leading = {
                        BackButton(
                            screenName = Text.translatable(Texts.SCREEN_MANAGE_CONTROL_PRESET_TITLE),
                        )
                    },
                )
            },
        ) { modifier ->
            val configScreenModel = LocalConfigScreenModel.current
            val screenModel = rememberScreenModel { ManageControlPresetsTabModel(configScreenModel) }
            val presetConfig by screenModel.presetConfig.collectAsState()
            val currentPresetConfig = presetConfig
            if (currentPresetConfig != null) {
                BuiltInPresetKeySelector(
                    modifier = modifier,
                    value = currentPresetConfig.key,
                    onValueChanged = screenModel::updateKey,
                )
            } else {
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
                        Text(Text.translatable(Texts.SCREEN_MANAGE_CONTROL_PRESET_SWITCH_MESSAGE))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12),
                        ) {
                            WarningButton(
                                onClick = {
                                    screenModel.update(PresetConfig.BuiltIn())
                                }
                            ) {
                                Text(Text.translatable(Texts.SCREEN_MANAGE_CONTROL_PRESET_SWITCH_SWITCH))
                            }
                            GuideButton(
                                onClick = {
                                    navigator?.replace(CustomTabProvider.customTab)
                                }
                            ) {
                                Text(Text.translatable(Texts.SCREEN_MANAGE_CONTROL_PRESET_SWITCH_GOTO_CUSTOM))
                            }
                        }
                    }
                }
            }
        }
    }
}
