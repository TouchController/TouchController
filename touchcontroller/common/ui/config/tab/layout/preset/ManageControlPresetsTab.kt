/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.layout.preset

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import top.fifthlight.combine.widget.GuideButton
import top.fifthlight.combine.widget.Text
import top.fifthlight.combine.widget.WarningButton
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.common.config.PresetConfig
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.preset.component.ManageControlPresetsTabComponent
import top.fifthlight.touchcontroller.common.ui.preset.BuiltInPresetKeySelector
import top.fifthlight.touchcontroller.common.ui.preset.TopBarConfigureScreen
import top.fifthlight.touchcontroller.common.ui.state.subscribeAsState
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme
import top.fifthlight.touchcontroller.common.ui.widget.Scaffold
import top.fifthlight.touchcontroller.common.ui.widget.navigation.AppBar
import top.fifthlight.touchcontroller.common.ui.widget.navigation.BackButton

@Composable
fun ManageControlPresetsTab(component: ManageControlPresetsTabComponent) {
    val stack by component.childStack.subscribeAsState()
    when (val instance = stack.active.instance) {
        is ManageControlPresetsTabComponent.ChildComponent.Preset -> PresetScreen(component)
        is ManageControlPresetsTabComponent.ChildComponent.TopBarEditor -> TopBarConfigureScreen(
            key = instance.key,
            onWidgetsChanged = { widgets ->
                component.updateKey(
                    instance.key.copy(topBar = instance.key.topBar.copy(widgets = widgets)),
                )
            },
            onBack = component::onBack,
        )
    }
}

@Composable
private fun PresetScreen(component: ManageControlPresetsTabComponent) {
    val config by component.presetConfig.subscribeAsState()
    Scaffold(
        topBar = {
            AppBar(
                modifier = Modifier.fillMaxWidth(),
                leading = {
                    BackButton(
                        screenName = Text.translatable(Texts.SCREEN_MANAGE_CONTROL_PRESET_TITLE),
                        onClick = component::onBack,
                    )
                },
            )
        },
    ) { modifier ->
        when (val preset = config) {
            is PresetConfig.BuiltIn -> BuiltInPresetKeySelector(
                modifier = modifier,
                value = preset.key,
                onValueChanged = component::updateKey,
                onConfigureTopBar = component::openTopBarEditor,
            )

            is PresetConfig.Custom -> Box(
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
                        WarningButton(onClick = component::openControlPresets) {
                            Text(Text.translatable(Texts.SCREEN_MANAGE_CONTROL_PRESET_SWITCH_SWITCH))
                        }
                        GuideButton(onClick = component.onOpenCustomLayout) {
                            Text(Text.translatable(Texts.SCREEN_MANAGE_CONTROL_PRESET_SWITCH_GOTO_CUSTOM))
                        }
                    }
                }
            }
        }
    }
}
