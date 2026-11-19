/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.widget

import androidx.compose.runtime.Composable
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.drawing.border
import top.fifthlight.combine.core.modifier.placement.width
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.core.widget.layout.ColumnScope
import top.fifthlight.combine.core.widget.layout.Spacer
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme

@Composable
fun SideBar(
    tabsButton: @Composable () -> Unit,
    actions: @Composable ColumnScope.() -> Unit,
) {
    Column {
        tabsButton()
        Spacer(
            modifier = Modifier
                .border(LocalTouchControllerTheme.current.titleBoxBackground)
                .weight(1f)
                .width(22),
        )
        actions()
    }
}
