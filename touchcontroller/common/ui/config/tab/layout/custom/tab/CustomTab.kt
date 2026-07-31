/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.tab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.drawing.border
import top.fifthlight.combine.core.modifier.placement.*
import top.fifthlight.combine.core.widget.layout.*
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.model.CustomControlLayoutTabModel
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.state.CustomControlLayoutTabState
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme
import top.fifthlight.data.IntSize
import top.fifthlight.touchcontroller.common.ui.widget.TitleBox

data class CustomTabContext(
    val screenModel: CustomControlLayoutTabModel,
    val uiState: CustomControlLayoutTabState.Enabled,
    val tabsButton: @Composable () -> Unit,
    val sideBarAtRight: Boolean,
    val editAreaSize: IntSize,
    val parentNavigator: Navigator?,
)

val LocalCustomTabContext = compositionLocalOf<CustomTabContext> { error("No CustomTabContext") }

abstract class CustomTab : Screen {
    @Composable
    abstract fun Icon()

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

    @Composable
    fun SideBarContainer(
        sideBarAtRight: Boolean,
        tabsButton: @Composable () -> Unit,
        actions: @Composable ColumnScope.() -> Unit,
        content: @Composable RowScope.(Modifier) -> Unit,
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            if (sideBarAtRight) {
                SideBar(tabsButton, actions)
            }
            content(Modifier.weight(1f).fillMaxHeight())
            if (!sideBarAtRight) {
                SideBar(tabsButton, actions)
            }
        }
    }

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
}
