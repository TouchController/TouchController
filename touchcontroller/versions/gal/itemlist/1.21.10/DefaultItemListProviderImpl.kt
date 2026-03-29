/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.gal.itemlist.v1_21_10

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.Items
import top.fifthlight.combine.backend.minecraft.item.v1_21_10.ItemImpl
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.common.config.item.ItemList
import top.fifthlight.touchcontroller.common.gal.itemlist.DefaultItemListProvider
import top.fifthlight.touchcontroller.gal.item.v1_21_10.ItemDataComponentTypeImpl
import top.fifthlight.touchcontroller.gal.item.v1_21_10.ItemSubclassProviderImpl

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
            ItemImpl(Items.MILK_BUCKET),
        ),
        blacklist = persistentListOf(
            ItemImpl(Items.ARROW),
            ItemImpl(Items.FIRE_CHARGE),
            ItemImpl(Items.SPECTRAL_ARROW),
            ItemImpl(Items.TIPPED_ARROW),
            ItemImpl(Items.FIREWORK_ROCKET),
        ),
        subclasses = persistentSetOf(
            ItemSubclassProviderImpl.rangedWeaponSubclass,
            ItemSubclassProviderImpl.projectileSubclass,
        ),
        components = persistentListOf(
            ItemDataComponentTypeImpl(DataComponents.FOOD),
            ItemDataComponentTypeImpl(DataComponents.BUNDLE_CONTENTS),
            ItemDataComponentTypeImpl(DataComponents.CONSUMABLE),
            ItemDataComponentTypeImpl(DataComponents.EQUIPPABLE),
        )
    )

    override val showCrosshairItems = ItemList(
        whitelist = persistentListOf(
            ItemImpl(Items.ENDER_PEARL),
        ),
        blacklist = persistentListOf(
            ItemImpl(Items.FIREWORK_ROCKET),
            ItemImpl(Items.ARROW),
            ItemImpl(Items.FIRE_CHARGE),
            ItemImpl(Items.SPECTRAL_ARROW),
            ItemImpl(Items.TIPPED_ARROW),
        ),
        subclasses = persistentSetOf(
            ItemSubclassProviderImpl.rangedWeaponSubclass,
            ItemSubclassProviderImpl.projectileSubclass,
        )
    )

    override val crosshairAimingItems = ItemList(
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
