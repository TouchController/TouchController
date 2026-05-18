/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.status

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import top.fifthlight.combine.widget.ui.Text
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.common.config.data.StatusConfig
import top.fifthlight.touchcontroller.common.ui.config.model.LocalConfigScreenModel
import top.fifthlight.touchcontroller.common.ui.config.tab.Tab
import top.fifthlight.touchcontroller.common.ui.config.tab.TabGroup
import top.fifthlight.touchcontroller.common.ui.config.tab.TabOptions
import top.fifthlight.touchcontroller.common.ui.config.tab.status.model.StatusTabModel
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme
import top.fifthlight.touchcontroller.common.ui.widget.ListButton

object StatusTab : Tab() {
    override val options = TabOptions(
        titleId = Texts.SCREEN_CONFIG_STATUS_TITLE,
        group = TabGroup.SystemGroup,
        index = 1,
        onReset = { copy(status = StatusConfig()) },
    )

    @Composable
    private fun StatusItem(
        modifier: Modifier = Modifier,
        title: Text,
        description: Text,
        selected: Boolean,
        onClick: () -> Unit,
    ) {
        ListButton(
            modifier = Modifier.then(modifier),
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
    override fun Content() {
        val screenModel = LocalConfigScreenModel.current
        val screenState by screenModel.uiState.collectAsState()
        val tabModel = remember { StatusTabModel() }
        val uiState by tabModel.uiState.collectAsState()
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

            uiState.warningMessage?.let { systemInfo ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(LocalTouchControllerTheme.current.listButtonDrawablesUnchecked.normal),
                    verticalArrangement = Arrangement.spacedBy(8),
                ) {
                    uiState.warningMessage?.let { Text(it) }
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
                    selected = screenState.config.status.status == StatusConfig.Status.ENABLED,
                    onClick = {
                        screenModel.updateConfig { copy(status = status.copy(status = StatusConfig.Status.ENABLED)) }
                    }
                )
                StatusItem(
                    modifier = Modifier.fillMaxWidth(),
                    title = Text.translatable(Texts.SCREEN_CONFIG_STATUS_STATUS_ONLY_VIEW_CLICK_TO_INTERACT_TITLE),
                    description = Text.translatable(Texts.SCREEN_CONFIG_STATUS_STATUS_ONLY_VIEW_CLICK_TO_INTERACT_DESCRIPTION),
                    selected = screenState.config.status.status == StatusConfig.Status.ONLY_VIEW_CLICK_TO_INTERACT,
                    onClick = {
                        screenModel.updateConfig { copy(status = status.copy(status = StatusConfig.Status.ONLY_VIEW_CLICK_TO_INTERACT)) }
                    }
                )
                StatusItem(
                    modifier = Modifier.fillMaxWidth(),
                    title = Text.translatable(Texts.SCREEN_CONFIG_STATUS_STATUS_ONLY_VIEW_AIMING_BY_CROSSHAIR_TITLE),
                    description = Text.translatable(Texts.SCREEN_CONFIG_STATUS_STATUS_ONLY_VIEW_AIMING_BY_CROSSHAIR_DESCRIPTION),
                    selected = screenState.config.status.status == StatusConfig.Status.ONLY_VIEW_AIMING_BY_CROSSHAIR,
                    onClick = {
                        screenModel.updateConfig { copy(status = status.copy(status = StatusConfig.Status.ONLY_VIEW_AIMING_BY_CROSSHAIR)) }
                    }
                )
                StatusItem(
                    modifier = Modifier.fillMaxWidth(),
                    title = Text.translatable(Texts.SCREEN_CONFIG_STATUS_STATUS_DISABLE_TITLE),
                    description = Text.translatable(Texts.SCREEN_CONFIG_STATUS_STATUS_DISABLE_DESCRIPTION),
                    selected = screenState.config.status.status == StatusConfig.Status.DISABLED,
                    onClick = {
                        screenModel.updateConfig { copy(status = status.copy(status = StatusConfig.Status.DISABLED)) }
                    }
                )
            }
        }
    }
}
