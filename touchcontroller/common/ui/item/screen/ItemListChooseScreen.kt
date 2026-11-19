/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.item.screen

import androidx.compose.runtime.Composable
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.layout.Alignment
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.drawing.border
import top.fifthlight.combine.core.modifier.placement.fillMaxSize
import top.fifthlight.combine.core.modifier.placement.fillMaxWidth
import top.fifthlight.combine.core.widget.layout.Box
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.widget.Button
import top.fifthlight.combine.widget.Text
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.common.gal.player.PlayerHandleFactory
import top.fifthlight.touchcontroller.common.ui.item.component.ItemChooserComponent
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme

@Composable
fun ItemListChooseScreen(
    component: ItemChooserComponent,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .border(LocalTouchControllerTheme.current.borderBackgroundDark),
        alignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(.6f),
            verticalArrangement = Arrangement.spacedBy(8),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(Text.translatable(Texts.SCREEN_ITEM_LIST_CONTENT))
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = component::openDefaultList,
            ) {
                Text(Text.translatable(Texts.SCREEN_ITEM_LIST_DEFAULT))
            }

            val player = PlayerHandleFactory.current()
            if (player != null) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = component::openCreativeTab,
                ) {
                    Text(Text.translatable(Texts.SCREEN_ITEM_LIST_VANILLA_INVENTORY))
                }
            }
        }
    }
}
