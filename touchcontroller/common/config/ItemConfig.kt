package top.fifthlight.touchcontroller.common.config

import kotlinx.serialization.Serializable
import top.fifthlight.touchcontroller.common.config.item.ItemList
import top.fifthlight.touchcontroller.common.gal.itemlist.DefaultItemListProvider

@Serializable
data class ItemConfig(
    val usableItems: ItemList,
    val showCrosshairItems: ItemList,
    val crosshairAimingItems: ItemList,
) {
    companion object {
        fun default(itemListProvider: DefaultItemListProvider = DefaultItemListProvider) = ItemConfig(
            usableItems = itemListProvider.usableItems,
            showCrosshairItems = itemListProvider.showCrosshairItems,
            crosshairAimingItems = itemListProvider.crosshairAimingItems,
        )
    }
}
