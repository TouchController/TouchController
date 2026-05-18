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
import top.fifthlight.touchcontroller.common.config.data.RegularConfig
import top.fifthlight.touchcontroller.common.ui.config.model.LocalConfigScreenModel
import top.fifthlight.touchcontroller.common.ui.config.tab.Tab
import top.fifthlight.touchcontroller.common.ui.config.tab.TabGroup
import top.fifthlight.touchcontroller.common.ui.config.tab.TabOptions
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme
import top.fifthlight.touchcontroller.common.ui.widget.SwitchPreferenceItem

object RegularTab : Tab() {
    override val options = TabOptions(
        titleId = Texts.SCREEN_CONFIG_GENERAL_REGULAR_TITLE,
        group = TabGroup.GeneralGroup,
        index = 0,
        onReset = { copy(regular = RegularConfig()) },
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
            fun update(editor: RegularConfig.() -> RegularConfig) {
                screenModel.updateConfig { copy(regular = editor(regular)) }
            }
            SwitchPreferenceItem(
                title = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_REGULAR_DISABLE_MOUSE_MOVE_TITLE),
                description = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_REGULAR_DISABLE_MOUSE_MOVE_DESCRIPTION),
                value = globalConfig.regular.disableMouseMove,
                onValueChanged = { update { copy(disableMouseMove = it) } }
            )
            SwitchPreferenceItem(
                title = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_REGULAR_DISABLE_MOUSE_CLICK_TITLE),
                description = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_REGULAR_DISABLE_MOUSE_CLICK_DESCRIPTION),
                value = globalConfig.regular.disableMouseClick,
                onValueChanged = { update { copy(disableMouseClick = it) } }
            )
            SwitchPreferenceItem(
                title = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_REGULAR_DISABLE_CURSOR_LOCK_TITLE),
                description = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_REGULAR_DISABLE_CURSOR_LOCK_DESCRIPTION),
                value = globalConfig.regular.disableMouseLock,
                onValueChanged = { update { copy(disableMouseLock = it) } }
            )
            SwitchPreferenceItem(
                title = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_REGULAR_DISABLE_HOT_BAR_KEY_TITLE),
                description = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_REGULAR_DISABLE_HOT_BAR_KEY_DESCRIPTION),
                value = globalConfig.regular.disableHotBarKey,
                onValueChanged = { update { copy(disableHotBarKey = it) } }
            )
            SwitchPreferenceItem(
                title = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_REGULAR_VIBRATION_TITLE),
                description = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_REGULAR_VIBRATION_DESCRIPTION),
                value = globalConfig.regular.vibration,
                onValueChanged = { update { copy(vibration = it) } }
            )
            SwitchPreferenceItem(
                title = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_REGULAR_QUICK_HAND_SWAP_TITLE),
                description = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_REGULAR_QUICK_HAND_SWAP_DESCRIPTION),
                value = globalConfig.regular.quickHandSwap,
                onValueChanged = { update { copy(quickHandSwap = it) } }
            )
        }
    }
}
