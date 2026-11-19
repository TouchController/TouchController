/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.item

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.placement.fillMaxSize
import top.fifthlight.combine.core.modifier.placement.padding
import top.fifthlight.combine.core.modifier.scroll.verticalScroll
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.widget.Button
import top.fifthlight.combine.widget.Switch
import top.fifthlight.combine.widget.Text
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.common.gal.item.ItemSubclassProvider
import top.fifthlight.touchcontroller.common.ui.component.screen.ComponentScreen
import top.fifthlight.touchcontroller.common.ui.config.tab.item.component.ItemScreenComponent
import top.fifthlight.touchcontroller.common.ui.config.tab.item.component.ItemTabComponent
import top.fifthlight.touchcontroller.common.ui.item.screen.ItemListScreen
import top.fifthlight.touchcontroller.common.ui.state.subscribeAsState
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme
import top.fifthlight.touchcontroller.common.ui.widget.HorizontalPreferenceItem

@Composable
fun ItemScreen(component: ItemScreenComponent) {
    val value by component.itemList.subscribeAsState()
    Column(
        modifier = Modifier
            .padding(8)
            .verticalScroll(background = LocalTouchControllerTheme.current.background)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8),
    ) {
        HorizontalPreferenceItem(
            title = Text.translatable(Texts.SCREEN_CONFIG_ITEM_WHITELIST_TITLE),
            description = Text.translatable(Texts.SCREEN_CONFIG_ITEM_WHITELIST_DESCRIPTION),
        ) {
            Button(
                onClick = {
                    component.openWhitelistEditor()
                },
            ) {
                Text(Text.translatable(Texts.SCREEN_CONFIG_ITEM_EDIT_TITLE))
            }
        }
        HorizontalPreferenceItem(
            title = Text.translatable(Texts.SCREEN_CONFIG_ITEM_BLACKLIST_TITLE),
            description = Text.translatable(Texts.SCREEN_CONFIG_ITEM_BLACKLIST_DESCRIPTION),
        ) {
            Button(
                onClick = {
                    component.openBlacklistEditor()
                },
            ) {
                Text(Text.translatable(Texts.SCREEN_CONFIG_ITEM_EDIT_TITLE))
            }
        }
        HorizontalPreferenceItem(
            title = Text.translatable(Texts.SCREEN_CONFIG_ITEM_COMPONENT_TITLE),
            description = Text.translatable(Texts.SCREEN_CONFIG_ITEM_COMPONENT_DESCRIPTION),
        ) {
            Button(
                onClick = {
                    component.openComponentListEditor()
                },
            ) {
                Text(Text.translatable(Texts.SCREEN_CONFIG_ITEM_EDIT_TITLE))
            }
        }
        for (subclass in ItemSubclassProvider.itemSubclasses) {
            HorizontalPreferenceItem(
                title = subclass.name,
            ) {
                Switch(
                    value = value.subclasses.contains(subclass),
                    onValueChanged = {
                        if (it) {
                            component.onItemListChanged(value.copy(subclasses = value.subclasses.add(subclass)))
                        } else {
                            component.onItemListChanged(value.copy(subclasses = value.subclasses.remove(subclass)))
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ItemTab(component: ItemTabComponent) {
    val stack by component.childStack.subscribeAsState()
    when (val instance = stack.active.instance) {
        is ItemTabComponent.ChildComponent.Tab -> ItemScreen(instance.component)
        is ItemTabComponent.ChildComponent.Whitelist -> ItemListScreen(instance.component)
        is ItemTabComponent.ChildComponent.Blacklist -> ItemListScreen(instance.component)
        is ItemTabComponent.ChildComponent.Components -> ComponentScreen(instance.component)
    }
}
