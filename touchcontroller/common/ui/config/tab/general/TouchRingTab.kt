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
import top.fifthlight.touchcontroller.common.config.data.TouchRingConfig
import top.fifthlight.touchcontroller.common.ui.config.tab.TabComponent
import top.fifthlight.touchcontroller.common.ui.state.subscribeAsState
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme
import top.fifthlight.touchcontroller.common.ui.widget.IntSliderPreferenceItem
import top.fifthlight.touchcontroller.common.ui.widget.SliderPreferenceItem

@Composable
fun TouchRingTab(component: TabComponent.TouchRing) {
    val config by component.config.subscribeAsState()
    fun update(editor: TouchRingConfig.() -> TouchRingConfig) {
        component.onConfigChanged(config.let(editor))
    }
    Column(
        modifier = Modifier
            .padding(8)
            .verticalScroll(background = LocalTouchControllerTheme.current.background)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8),
    ) {
        IntSliderPreferenceItem(
            title = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_TOUCH_RING_RADIUS_TITLE),
            description = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_TOUCH_RING_RADIUS_DESCRIPTION),
            range = 16..96,
            value = config.radius,
            onValueChanged = { update { copy(radius = it) } }
        )
        IntSliderPreferenceItem(
            title = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_TOUCH_RING_BORDER_WIDTH_TITLE),
            description = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_TOUCH_RING_BORDER_WIDTH_DESCRIPTION),
            range = 0..8,
            value = config.outerRadius,
            onValueChanged = { update { copy(outerRadius = it) } },
        )
        SliderPreferenceItem(
            title = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_TOUCH_RING_INITIAL_PROGRESS_TITLE),
            description = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_TOUCH_RING_INITIAL_PROGRESS_DESCRIPTION),
            range = 0f..1f,
            value = config.initialProgress,
            onValueChanged = { update { copy(initialProgress = it) } },
        )
    }
}
