package top.fifthlight.touchcontroller.common.gal.itemlist

import top.fifthlight.mergetools.api.ExpectFactory
import top.fifthlight.touchcontroller.common.config.item.ItemList

interface DefaultItemListProvider {
    val usableItems: ItemList
    val showCrosshairItems: ItemList
    val crosshairAimingItems: ItemList

    @ExpectFactory
    interface Factory {
        fun of(): DefaultItemListProvider
    }

    companion object : DefaultItemListProvider by DefaultItemListProviderFactory.of()
}
