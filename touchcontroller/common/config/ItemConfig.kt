/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.config

import top.fifthlight.touchcontroller.common.config.item.ItemList
import top.fifthlight.touchcontroller.common.gal.itemlist.DefaultItemListProvider

data class ItemConfig(
    val usableItems: ItemList,
    val showCrosshairItems: ItemList,
    val usingAimingItems: ItemList,
) {
    companion object {
        fun default(itemListProvider: DefaultItemListProvider = DefaultItemListProvider) = ItemConfig(
            usableItems = itemListProvider.usableItems,
            showCrosshairItems = itemListProvider.showCrosshairItems,
            usingAimingItems = itemListProvider.usingAimingItems,
        )
    }
}
