/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.general

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.placement.fillMaxSize
import top.fifthlight.combine.core.modifier.placement.padding
import top.fifthlight.combine.core.modifier.scroll.verticalScroll
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.common.config.data.ControlConfig
import top.fifthlight.touchcontroller.common.ui.config.tab.TabComponent
import top.fifthlight.touchcontroller.common.ui.state.subscribeAsState
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme
import top.fifthlight.touchcontroller.common.ui.widget.IntSliderPreferenceItem
import top.fifthlight.touchcontroller.common.ui.widget.SliderPreferenceItem

@Composable
fun ControlTab(component: TabComponent.Control) {
    val config by component.config.subscribeAsState()
    fun update(editor: ControlConfig.() -> ControlConfig) {
        component.onConfigChanged(config.let(editor))
    }
    Column(
        modifier = Modifier
            .padding(8)
            .verticalScroll(background = LocalTouchControllerTheme.current.background)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8),
    ) {
        SliderPreferenceItem(
            title = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_CONTROL_VIEW_MOVEMENT_SENSITIVITY_TITLE),
            description = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_CONTROL_VIEW_MOVEMENT_SENSITIVITY_DESCRIPTION),
            percent = false,
            range = 0f..1800f,
            value = config.viewMovementSensitivity,
            onValueChanged = { update { copy(viewMovementSensitivity = it) } }
        )
        IntSliderPreferenceItem(
            title = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_CONTROL_VIEW_HOLD_DETECT_THRESHOLD_TITLE),
            description = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_CONTROL_VIEW_HOLD_DETECT_THRESHOLD_DESCRIPTION),
            range = 0..10,
            value = config.viewHoldDetectThreshold,
            onValueChanged = { update { copy(viewHoldDetectThreshold = it) } }
        )
        IntSliderPreferenceItem(
            title = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_CONTROL_VIEW_HOLD_DETECT_TICKS_TITLE),
            description = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_CONTROL_VIEW_HOLD_DETECT_TICKS_DESCRIPTION),
            range = 1..60,
            value = config.viewHoldDetectTicks,
            onValueChanged = { update { copy(viewHoldDetectTicks = it) } }
        )
        IntSliderPreferenceItem(
            title = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_CONTROL_CREATIVE_BREAK_HOLD_TICKS_TITLE),
            description = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_CONTROL_CREATIVE_BREAK_HOLD_TICKS_DESCRIPTION),
            range = 1..60,
            value = config.creativeBreakDetectTicks,
            onValueChanged = { update { copy(creativeBreakDetectTicks = it) } }
        )
    }
}
