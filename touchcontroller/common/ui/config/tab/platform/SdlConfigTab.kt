/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.platform

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
import top.fifthlight.touchcontroller.common.config.platform.SdlPlatformConfig
import top.fifthlight.touchcontroller.common.ui.config.model.LocalConfigScreenModel
import top.fifthlight.touchcontroller.common.ui.config.tab.Tab
import top.fifthlight.touchcontroller.common.ui.config.tab.TabGroup
import top.fifthlight.touchcontroller.common.ui.config.tab.TabOptions
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme
import top.fifthlight.touchcontroller.common.ui.widget.IntSliderPreferenceItem
import top.fifthlight.touchcontroller.common.ui.widget.SliderPreferenceItem

object SdlConfigTab : Tab() {
    override val options = TabOptions(
        titleId = Texts.SCREEN_CONFIG_PLATFORM_TITLE,
        group = TabGroup.SystemGroup,
        index = 2,
        onReset = { copy(platform = platform.copy(sdl = SdlPlatformConfig())) },
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
            fun update(editor: SdlPlatformConfig.() -> SdlPlatformConfig) {
                screenModel.updateConfig {
                    copy(
                        platform = platform.copy(
                            sdl = editor(platform.sdl),
                        ),
                    )
                }
            }
            SliderPreferenceItem(
                title = Text.translatable(Texts.SCREEN_CONFIG_PLATFORM_SDL_VIBRATION_STRENGTH_TITLE),
                description = Text.translatable(Texts.SCREEN_CONFIG_PLATFORM_SDL_VIBRATION_STRENGTH_DESCRIPTION),
                value = globalConfig.platform.sdl.vibrationStrength,
                range = 0f..1f,
                onValueChanged = { update { copy(vibrationStrength = it) } }
            )
            IntSliderPreferenceItem(
                title = Text.translatable(Texts.SCREEN_CONFIG_PLATFORM_SDL_VIBRATION_LENGTH_TITLE),
                description = Text.translatable(Texts.SCREEN_CONFIG_PLATFORM_SDL_VIBRATION_LENGTH_DESCRIPTION),
                value = globalConfig.platform.sdl.vibrationLength,
                range = 0..1000,
                onValueChanged = { update { copy(vibrationLength = it) } }
            )
        }
    }
}
