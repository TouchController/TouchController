/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.item.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.layout.Alignment
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.drawing.border
import top.fifthlight.combine.core.modifier.placement.fillMaxSize
import top.fifthlight.combine.core.widget.layout.Box
import top.fifthlight.combine.widget.Text
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.common.gal.gamestate.GameState
import top.fifthlight.touchcontroller.common.ui.item.component.ItemChooserComponent
import top.fifthlight.touchcontroller.common.ui.state.subscribeAsState
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme

@Composable
fun ItemChooser(component: ItemChooserComponent) {
    if (!GameState.inGame) {
        Box(
            modifier = Modifier
                .border(LocalTouchControllerTheme.current.borderBackgroundDark)
                .fillMaxSize(),
            alignment = Alignment.Center,
        ) {
            Text(Text.translatable(Texts.SCREEN_ITEM_LIST_WARNING_NOT_IN_GAME))
        }
        return
    }

    val slot by component.slot.subscribeAsState()
    when (val child = slot.child?.instance) {
        null -> ItemListChooseScreen(component)
        ItemChooserComponent.ChildComponent.DefaultList -> DefaultListItemChooseScreen(component)
        is ItemChooserComponent.ChildComponent.CreativeTab -> CreativeTabItemChooseScreen(
            component = component,
            tabs = child.tabs,
            playerInventory = child.playerInventory,
        )
    }
}
