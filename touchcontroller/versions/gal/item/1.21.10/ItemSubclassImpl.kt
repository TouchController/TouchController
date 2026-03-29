/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.gal.item.v1_21_10

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import net.minecraft.network.chat.Component
import net.minecraft.world.item.BoatItem
import net.minecraft.world.item.BucketItem
import net.minecraft.world.item.PlaceOnWaterBlockItem
import net.minecraft.world.item.ProjectileItem
import net.minecraft.world.item.ProjectileWeaponItem
import net.minecraft.world.item.SpawnEggItem
import top.fifthlight.combine.backend.minecraft.text.v1_21_10.TextImpl
import top.fifthlight.combine.backend.minecraft.item.v1_21_10.toVanilla
import top.fifthlight.combine.data.Text
import top.fifthlight.combine.item.data.Item
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.common.gal.item.ItemSubclass
import top.fifthlight.touchcontroller.common.gal.item.ItemSubclassProvider

class ItemSubclassImpl<Clazz>(
    override val name: Text,
    override val configId: String,
    val clazz: Class<Clazz>,
) : ItemSubclass {
    override val id: String = clazz.simpleName

    override fun contains(item: Item) = clazz.isInstance(item.toVanilla())

    override val items: PersistentList<Item> by lazy {
        ItemProviderImpl.allItems.filter { it in this }.toPersistentList()
    }
}

@ActualImpl(ItemSubclassProvider::class)
object ItemSubclassProviderImpl : ItemSubclassProvider {
    @JvmStatic
    @ActualConstructor
    fun of(): ItemSubclassProvider = ItemSubclassProviderImpl

    val rangedWeaponSubclass = ItemSubclassImpl(
        name = TextImpl(Component.literal("Ranged weapon")),
        configId = "RangedWeaponItem",
        clazz = ProjectileWeaponItem::class.java,
    )

    val projectileSubclass = ItemSubclassImpl(
        name = TextImpl(Component.literal("Projectile")),
        configId = "ProjectileItem",
        clazz = ProjectileItem::class.java,
    )

    val bucketSubclass = ItemSubclassImpl(
        name = TextImpl(Component.literal("Bucket")),
        configId = "BucketItem",
        clazz = BucketItem::class.java,
    )

    val boatSubclass = ItemSubclassImpl(
        name = TextImpl(Component.literal("Boat")),
        configId = "BoatItem",
        clazz = BoatItem::class.java,
    )

    val placeableOnWaterSubclass = ItemSubclassImpl(
        name = TextImpl(Component.literal("PlaceableOnWater")),
        configId = "PlaceableOnWaterItem",
        clazz = PlaceOnWaterBlockItem::class.java,
    )

    val spawnEggSubclass = ItemSubclassImpl(
        name = TextImpl(Component.literal("SpawnEgg")),
        configId = "SpawnEggItem",
        clazz = SpawnEggItem::class.java,
    )

    override val itemSubclasses: PersistentList<ItemSubclass> = persistentListOf(
        rangedWeaponSubclass,
        projectileSubclass,
        bucketSubclass,
        boatSubclass,
        placeableOnWaterSubclass,
        spawnEggSubclass,
    )
}
