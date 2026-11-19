/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.status

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.data.TextColor
import top.fifthlight.combine.core.layout.Alignment
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.drawing.border
import top.fifthlight.combine.core.modifier.placement.fillMaxSize
import top.fifthlight.combine.core.modifier.placement.fillMaxWidth
import top.fifthlight.combine.core.modifier.placement.padding
import top.fifthlight.combine.core.modifier.scroll.verticalScroll
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.theme.blackstone.widget.ListButton
import top.fifthlight.combine.theme.blackstone.widget.LocalListButtonTheme
import top.fifthlight.combine.widget.Text
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.common.config.data.StatusConfig
import top.fifthlight.touchcontroller.common.ui.config.tab.status.component.StatusTabComponent
import top.fifthlight.touchcontroller.common.ui.state.subscribeAsState
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme

@Composable
private fun StatusItem(
    modifier: Modifier = Modifier,
    title: Text,
    description: Text,
    selected: Boolean,
    onClick: () -> Unit,
) {
    ListButton(
        modifier = modifier,
        checked = selected,
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.alignment(Alignment.CenterLeft),
            verticalArrangement = Arrangement.spacedBy(4),
        ) {
            Text(title)
            Text(description)
        }
    }
}

@Composable
fun StatusTab(component: StatusTabComponent) {
    val uiState by component.uiState.subscribeAsState()
    Column(
        modifier = Modifier
            .padding(8)
            .verticalScroll(background = LocalTouchControllerTheme.current.background)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8),
    ) {
        uiState.currentPlatform?.let {
            Text(Text.format(Texts.SCREEN_CONFIG_STATUS_PLATFORM, it))
        } ?: run {
            Text(
                Text.format(
                    Texts.SCREEN_CONFIG_STATUS_PLATFORM,
                    Text.translatable(Texts.SCREEN_CONFIG_STATUS_PLATFORM_UNAVAILABLE)
                ).color(TextColor.RED)
            )
        }

        uiState.warningMessage?.let { warning ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(LocalListButtonTheme.current.drawableSet.off.normal),
                verticalArrangement = Arrangement.spacedBy(8),
            ) {
                Text(warning)
            }
        }

        uiState.systemInfo?.let { systemInfo ->
            Text(Text.translatable(Texts.SCREEN_CONFIG_STATUS_DEBUG_INFO_TITLE))

            Text(Text.format(Texts.SCREEN_CONFIG_STATUS_DEBUG_INFO_SYSTEM, systemInfo.system))
            Text(Text.format(Texts.SCREEN_CONFIG_STATUS_DEBUG_INFO_ARCHITECTURE, systemInfo.arch))
        }

        Text(Text.translatable(Texts.SCREEN_CONFIG_STATUS_STATUS_TITLE))

        Column(Modifier.fillMaxWidth()) {
            StatusItem(
                modifier = Modifier.fillMaxWidth(),
                title = Text.translatable(Texts.SCREEN_CONFIG_STATUS_STATUS_ENABLE_TITLE),
                description = Text.translatable(Texts.SCREEN_CONFIG_STATUS_STATUS_ENABLE_DESCRIPTION),
                selected = uiState.config.status == StatusConfig.Status.ENABLED,
                onClick = {
                    component.updateConfig(uiState.config.copy(status = StatusConfig.Status.ENABLED))
                }
            )
            StatusItem(
                modifier = Modifier.fillMaxWidth(),
                title = Text.translatable(Texts.SCREEN_CONFIG_STATUS_STATUS_ONLY_VIEW_CLICK_TO_INTERACT_TITLE),
                description = Text.translatable(Texts.SCREEN_CONFIG_STATUS_STATUS_ONLY_VIEW_CLICK_TO_INTERACT_DESCRIPTION),
                selected = uiState.config.status == StatusConfig.Status.ONLY_VIEW_CLICK_TO_INTERACT,
                onClick = {
                    component.updateConfig(uiState.config.copy(status = StatusConfig.Status.ONLY_VIEW_CLICK_TO_INTERACT))
                }
            )
            StatusItem(
                modifier = Modifier.fillMaxWidth(),
                title = Text.translatable(Texts.SCREEN_CONFIG_STATUS_STATUS_ONLY_VIEW_AIMING_BY_CROSSHAIR_TITLE),
                description = Text.translatable(Texts.SCREEN_CONFIG_STATUS_STATUS_ONLY_VIEW_AIMING_BY_CROSSHAIR_DESCRIPTION),
                selected = uiState.config.status == StatusConfig.Status.ONLY_VIEW_AIMING_BY_CROSSHAIR,
                onClick = {
                    component.updateConfig(uiState.config.copy(status = StatusConfig.Status.ONLY_VIEW_AIMING_BY_CROSSHAIR))
                }
            )
            StatusItem(
                modifier = Modifier.fillMaxWidth(),
                title = Text.translatable(Texts.SCREEN_CONFIG_STATUS_STATUS_DISABLE_TITLE),
                description = Text.translatable(Texts.SCREEN_CONFIG_STATUS_STATUS_DISABLE_DESCRIPTION),
                selected = uiState.config.status == StatusConfig.Status.DISABLED,
                onClick = {
                    component.updateConfig(uiState.config.copy(status = StatusConfig.Status.DISABLED))
                }
            )
        }
    }
}
