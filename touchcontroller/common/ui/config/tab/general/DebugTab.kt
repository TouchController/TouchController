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
import top.fifthlight.touchcontroller.common.config.data.DebugConfig
import top.fifthlight.touchcontroller.common.ui.config.model.LocalConfigScreenModel
import top.fifthlight.touchcontroller.common.ui.config.tab.Tab
import top.fifthlight.touchcontroller.common.ui.config.tab.TabGroup
import top.fifthlight.touchcontroller.common.ui.config.tab.TabOptions
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme
import top.fifthlight.touchcontroller.common.ui.widget.SwitchPreferenceItem

object DebugTab : Tab() {
    override val options = TabOptions(
        titleId = Texts.SCREEN_CONFIG_GENERAL_DEBUG_TITLE,
        group = TabGroup.GeneralGroup,
        index = 3,
        onReset = { copy(debug = DebugConfig()) },
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
            fun update(editor: DebugConfig.() -> DebugConfig) {
                screenModel.updateConfig { copy(debug = editor(debug)) }
            }
            SwitchPreferenceItem(
                title = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_DEBUG_SHOW_POINTERS_TITLE),
                description = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_DEBUG_SHOW_POINTERS_DESCRIPTION),
                value = globalConfig.debug.showPointers,
                onValueChanged = { update { copy(showPointers = it) } }
            )
            SwitchPreferenceItem(
                title = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_DEBUG_ENABLE_TOUCH_EMULATION_TITLE),
                description = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_DEBUG_ENABLE_TOUCH_EMULATION_DESCRIPTION),
                value = globalConfig.debug.enableTouchEmulation,
                onValueChanged = { update { copy(enableTouchEmulation = it) } }
            )
        }
    }
}
