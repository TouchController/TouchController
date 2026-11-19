/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.widget

import androidx.compose.runtime.Composable
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.drawing.border
import top.fifthlight.combine.core.modifier.placement.fillMaxWidth
import top.fifthlight.combine.core.modifier.placement.padding
import top.fifthlight.combine.core.modifier.placement.width
import top.fifthlight.combine.core.modifier.scroll.verticalScroll
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.theme.blackstone.widget.NavigationButton
import top.fifthlight.combine.widget.Text
import top.fifthlight.touchcontroller.common.ui.config.tab.TabConfig
import top.fifthlight.touchcontroller.common.ui.config.tab.TabGroup
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme

@Composable
fun SideTabBar(
    modifier: Modifier = Modifier,
    selectedTab: TabConfig,
    onTabSelected: (TabConfig) -> Unit,
    tabGroups: List<Pair<TabGroup?, List<TabConfig>>>,
) {
    Column(
        modifier = Modifier
            .width(130)
            .padding(top = 8, left = 2, right = 2, bottom = 2)
            .verticalScroll()
            .border(LocalTouchControllerTheme.current.borderBackgroundDark)
            .then(modifier),
        verticalArrangement = Arrangement.spacedBy(12),
    ) {
        for ((group, tabs) in tabGroups) {
            if (tabs.isEmpty()) {
                continue
            }
            Column(verticalArrangement = Arrangement.spacedBy(4)) {
                group?.let { group ->
                    Text(group.title)
                }
                Column(verticalArrangement = Arrangement.spacedBy(4)) {
                    for (tab in tabs) {
                        NavigationButton(
                            modifier = Modifier.fillMaxWidth(),
                            checked = selectedTab == tab,
                            onClick = {
                                onTabSelected(tab)
                            },
                        ) {
                            Text(tab.title)
                        }
                    }
                }
            }
        }
    }
}
