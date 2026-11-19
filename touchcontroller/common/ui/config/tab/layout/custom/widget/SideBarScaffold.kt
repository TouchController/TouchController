/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.widget

import androidx.compose.runtime.Composable
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.drawing.border
import top.fifthlight.combine.core.modifier.placement.IntrinsicSize
import top.fifthlight.combine.core.modifier.placement.fillMaxWidth
import top.fifthlight.combine.core.modifier.placement.height
import top.fifthlight.combine.core.modifier.placement.padding
import top.fifthlight.combine.core.widget.layout.*
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme
import top.fifthlight.touchcontroller.common.ui.widget.TitleBox

@Composable
fun SideBarScaffold(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    actions: @Composable (RowScope.() -> Unit)?,
    content: @Composable BoxScope.() -> Unit,
) {
    Column(modifier) {
        TitleBox(
            modifier = Modifier.fillMaxWidth()
        ) {
            title()
        }
        Box(
            modifier = Modifier
                .border(LocalTouchControllerTheme.current.borderBackgroundDark)
                .weight(1f)
                .fillMaxWidth(),
        ) {
            content()
        }
        actions?.let { actions ->
            Row(
                modifier = Modifier
                    .padding(2)
                    .border(LocalTouchControllerTheme.current.borderBackgroundDark)
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(2),
            ) {
                actions()
            }
        }
    }
}
