/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.item.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import top.fifthlight.touchcontroller.common.config.item.ItemList

class ItemScreenComponent(
    componentContext: ComponentContext,
    val itemList: Value<ItemList>,
    val onItemListChanged: (ItemList) -> Unit,
    val onWhitelistOpened: () -> Unit,
    val onBlacklistOpened: () -> Unit,
    val onComponentListOpened: () -> Unit,
) : ComponentContext by componentContext {
    fun openWhitelistEditor() = onWhitelistOpened()
    fun openBlacklistEditor() = onBlacklistOpened()
    fun openComponentListEditor() = onComponentListOpened()
}
