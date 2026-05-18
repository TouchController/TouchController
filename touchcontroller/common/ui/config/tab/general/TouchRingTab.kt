/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.general

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import top.fifthlight.touchcontroller.common.ui.config.model.LocalConfigScreenModel
import top.fifthlight.touchcontroller.common.ui.config.tab.Tab
import top.fifthlight.touchcontroller.common.ui.config.tab.TabGroup
import top.fifthlight.touchcontroller.common.ui.config.tab.TabOptions
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme
import top.fifthlight.touchcontroller.common.ui.widget.IntSliderPreferenceItem
import top.fifthlight.touchcontroller.common.ui.widget.SliderPreferenceItem

object TouchRingTab : Tab() {
    override val options = TabOptions(
        titleId = Texts.SCREEN_CONFIG_GENERAL_TOUCH_RING_TITLE,
        group = TabGroup.GeneralGroup,
        index = 2,
        onReset = { copy(touchRing = TouchRingConfig()) },
    )

    @Composable
    override fun Content() {
        val screenModel = LocalConfigScreenModel.current
        Column(
            modifier = Modifier
                .padding(8)
                .verticalScroll(background = LocalTouchControllerTheme.current.background)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8),
        ) {
            val uiState by screenModel.uiState.collectAsState()
            val globalConfig = uiState.config
            fun update(editor: TouchRingConfig.() -> TouchRingConfig) {
                screenModel.updateConfig { copy(touchRing = editor(touchRing)) }
            }
            IntSliderPreferenceItem(
                title = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_TOUCH_RING_RADIUS_TITLE),
                description = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_TOUCH_RING_RADIUS_DESCRIPTION),
                range = 16..96,
                value = globalConfig.touchRing.radius,
                onValueChanged = { update { copy(radius = it) } }
            )
            IntSliderPreferenceItem(
                title = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_TOUCH_RING_BORDER_WIDTH_TITLE),
                description = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_TOUCH_RING_BORDER_WIDTH_DESCRIPTION),
                range = 0..8,
                value = globalConfig.touchRing.outerRadius,
                onValueChanged = { update { copy(outerRadius = it) } },
            )
            SliderPreferenceItem(
                title = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_TOUCH_RING_INITIAL_PROGRESS_TITLE),
                description = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_TOUCH_RING_INITIAL_PROGRESS_DESCRIPTION),
                range = 0f..1f,
                value = globalConfig.touchRing.initialProgress,
                onValueChanged = { update { copy(initialProgress = it) } },
            )
        }
    }
}
