/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.item.screen

import androidx.compose.runtime.Composable
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.layout.Alignment
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.drawing.border
import top.fifthlight.combine.core.modifier.placement.fillMaxSize
import top.fifthlight.combine.core.widget.layout.Box
import top.fifthlight.combine.item.data.Item
import top.fifthlight.combine.widget.ui.Text
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.common.gal.gamestate.GameState
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme
import top.fifthlight.touchcontroller.common.ui.widget.navigation.TouchControllerNavigator

@Composable
fun ItemChooser(
    onItemChosen: (Item) -> Unit,
) {
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

    TouchControllerNavigator(ItemListChooseScreen(onItemChosen))
}
