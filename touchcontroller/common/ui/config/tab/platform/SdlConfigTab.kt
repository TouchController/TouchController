/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.platform

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
import top.fifthlight.touchcontroller.common.config.platform.SdlPlatformConfig
import top.fifthlight.touchcontroller.common.ui.config.tab.TabComponent
import top.fifthlight.touchcontroller.common.ui.state.subscribeAsState
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme
import top.fifthlight.touchcontroller.common.ui.widget.IntSliderPreferenceItem
import top.fifthlight.touchcontroller.common.ui.widget.SliderPreferenceItem

@Composable
fun SdlConfigTab(component: TabComponent.Sdl) {
    val config by component.config.subscribeAsState()
    fun update(editor: SdlPlatformConfig.() -> SdlPlatformConfig) {
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
            title = Text.translatable(Texts.SCREEN_CONFIG_PLATFORM_SDL_VIBRATION_STRENGTH_TITLE),
            description = Text.translatable(Texts.SCREEN_CONFIG_PLATFORM_SDL_VIBRATION_STRENGTH_DESCRIPTION),
            value = config.vibrationStrength,
            range = 0f..1f,
            onValueChanged = { update { copy(vibrationStrength = it) } }
        )
        IntSliderPreferenceItem(
            title = Text.translatable(Texts.SCREEN_CONFIG_PLATFORM_SDL_VIBRATION_LENGTH_TITLE),
            description = Text.translatable(Texts.SCREEN_CONFIG_PLATFORM_SDL_VIBRATION_LENGTH_DESCRIPTION),
            value = config.vibrationLength,
            range = 0..1000,
            onValueChanged = { update { copy(vibrationLength = it) } }
        )
    }
}
