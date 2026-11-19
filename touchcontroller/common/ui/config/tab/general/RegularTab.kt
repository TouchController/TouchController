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
import top.fifthlight.touchcontroller.common.config.data.RegularConfig
import top.fifthlight.touchcontroller.common.ui.config.tab.TabComponent
import top.fifthlight.touchcontroller.common.ui.state.subscribeAsState
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme
import top.fifthlight.touchcontroller.common.ui.widget.SwitchPreferenceItem

@Composable
fun RegularTab(component: TabComponent.Regular) {
    val config by component.config.subscribeAsState()
    fun update(editor: RegularConfig.() -> RegularConfig) {
        component.onConfigChanged(config.let(editor))
    }
    Column(
        modifier = Modifier
            .padding(8)
            .verticalScroll(background = LocalTouchControllerTheme.current.background)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8),
    ) {
        SwitchPreferenceItem(
            title = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_REGULAR_DISABLE_MOUSE_MOVE_TITLE),
            description = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_REGULAR_DISABLE_MOUSE_MOVE_DESCRIPTION),
            value = config.disableMouseMove,
            onValueChanged = { update { copy(disableMouseMove = it) } }
        )
        SwitchPreferenceItem(
            title = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_REGULAR_DISABLE_MOUSE_CLICK_TITLE),
            description = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_REGULAR_DISABLE_MOUSE_CLICK_DESCRIPTION),
            value = config.disableMouseClick,
            onValueChanged = { update { copy(disableMouseClick = it) } }
        )
        SwitchPreferenceItem(
            title = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_REGULAR_DISABLE_CURSOR_LOCK_TITLE),
            description = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_REGULAR_DISABLE_CURSOR_LOCK_DESCRIPTION),
            value = config.disableMouseLock,
            onValueChanged = { update { copy(disableMouseLock = it) } }
        )
        SwitchPreferenceItem(
            title = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_REGULAR_DISABLE_HOT_BAR_KEY_TITLE),
            description = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_REGULAR_DISABLE_HOT_BAR_KEY_DESCRIPTION),
            value = config.disableHotBarKey,
            onValueChanged = { update { copy(disableHotBarKey = it) } }
        )
        SwitchPreferenceItem(
            title = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_REGULAR_VIBRATION_TITLE),
            description = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_REGULAR_VIBRATION_DESCRIPTION),
            value = config.vibration,
            onValueChanged = { update { copy(vibration = it) } }
        )
        SwitchPreferenceItem(
            title = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_REGULAR_QUICK_HAND_SWAP_TITLE),
            description = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_REGULAR_QUICK_HAND_SWAP_DESCRIPTION),
            value = config.quickHandSwap,
            onValueChanged = { update { copy(quickHandSwap = it) } }
        )
    }
}
