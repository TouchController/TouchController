/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.gal.itemlist.v1_20_1

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import net.minecraft.world.item.Items
import top.fifthlight.combine.backend.minecraft.item.v1_20_1.ItemImpl
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.common.config.item.ItemList
import top.fifthlight.touchcontroller.common.gal.itemlist.DefaultItemListProvider
import top.fifthlight.touchcontroller.gal.item.v1_20_1.ItemDataComponentTypeImpl
import top.fifthlight.touchcontroller.gal.item.v1_20_1.ItemSubclassProviderImpl

@ActualImpl(DefaultItemListProvider::class)
object DefaultItemListProviderImpl : DefaultItemListProvider {
    @JvmStatic
    @ActualConstructor
    fun of(): DefaultItemListProvider = this

    override val usableItems = ItemList(
        whitelist = persistentListOf(
            ItemImpl(Items.FISHING_ROD),
            ItemImpl(Items.SPYGLASS),
            ItemImpl(Items.MAP),
            ItemImpl(Items.SHIELD),
            ItemImpl(Items.KNOWLEDGE_BOOK),
            ItemImpl(Items.WRITABLE_BOOK),
            ItemImpl(Items.WRITTEN_BOOK),
            ItemImpl(Items.ENDER_EYE),
            ItemImpl(Items.ENDER_PEARL),
            ItemImpl(Items.POTION),
            ItemImpl(Items.SNOWBALL),
            ItemImpl(Items.EGG),
            ItemImpl(Items.SPLASH_POTION),
            ItemImpl(Items.LINGERING_POTION),
            ItemImpl(Items.EXPERIENCE_BOTTLE),
            ItemImpl(Items.TRIDENT),
            ItemImpl(Items.MILK_BUCKET),
        ),
        subclasses = persistentSetOf(
            ItemSubclassProviderImpl.rangedWeaponSubclass,
            ItemSubclassProviderImpl.armorSubclass,
            ItemSubclassProviderImpl.bundleSubclass,
        ),
        components = persistentListOf(
            ItemDataComponentTypeImpl.FoodItemDataComponentType,
        )
    )

    override val showCrosshairItems = ItemList(
        whitelist = persistentListOf(
            ItemImpl(Items.EGG),
            ItemImpl(Items.SNOWBALL),
            ItemImpl(Items.SPLASH_POTION),
            ItemImpl(Items.LINGERING_POTION),
            ItemImpl(Items.EXPERIENCE_BOTTLE),
            ItemImpl(Items.TRIDENT),
            ItemImpl(Items.ENDER_PEARL),
        ),
        subclasses = persistentSetOf(
            ItemSubclassProviderImpl.rangedWeaponSubclass,
        )
    )

    override val usingAimingItems = ItemList(
        whitelist = persistentListOf(
            ItemImpl(Items.ENDER_EYE),
            ItemImpl(Items.GLASS_BOTTLE),
        ),
        subclasses = persistentSetOf(
            ItemSubclassProviderImpl.bucketSubclass,
            ItemSubclassProviderImpl.boatSubclass,
            ItemSubclassProviderImpl.placeableOnWaterSubclass,
            ItemSubclassProviderImpl.spawnEggSubclass,
        )
    )
}
