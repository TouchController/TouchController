/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.widget

import androidx.compose.runtime.Composable
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.placement.fillMaxHeight
import top.fifthlight.combine.core.modifier.placement.fillMaxSize
import top.fifthlight.combine.core.widget.layout.Box
import top.fifthlight.combine.core.widget.layout.ColumnScope
import top.fifthlight.combine.core.widget.layout.Row

@Composable
fun SideBarContainer(
    sideBarAtRight: Boolean,
    tabsButton: @Composable () -> Unit,
    actions: @Composable ColumnScope.() -> Unit,
    content: @Composable () -> Unit,
) {
    Row(modifier = Modifier.fillMaxSize()) {
        if (sideBarAtRight) {
            SideBar(tabsButton, actions)
        }
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            content()
        }
        if (!sideBarAtRight) {
            SideBar(tabsButton, actions)
        }
    }
}
