/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.gal.item.v26_1

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import net.minecraft.world.item.*
import top.fifthlight.combine.backend.minecraft.item.v26_1.toVanilla
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.item.data.Item
import top.fifthlight.touchcontroller.assets.lang.Texts
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
        name = Text.translatable(Texts.ITEM_SUBCLASS_RANGED_WEAPON),
        configId = "RangedWeaponItem",
        clazz = ProjectileWeaponItem::class.java,
    )

    val projectileSubclass = ItemSubclassImpl(
        name = Text.translatable(Texts.ITEM_SUBCLASS_PROJECTILE),
        configId = "ProjectileItem",
        clazz = ProjectileItem::class.java,
    )

    val bucketSubclass = ItemSubclassImpl(
        name = Text.translatable(Texts.ITEM_SUBCLASS_BUCKET),
        configId = "BucketItem",
        clazz = BucketItem::class.java,
    )

    val boatSubclass = ItemSubclassImpl(
        name = Text.translatable(Texts.ITEM_SUBCLASS_BOAT),
        configId = "BoatItem",
        clazz = BoatItem::class.java,
    )

    val placeableOnWaterSubclass = ItemSubclassImpl(
        name = Text.translatable(Texts.ITEM_SUBCLASS_PLACEABLE_ON_WATER),
        configId = "PlaceableOnWaterItem",
        clazz = PlaceOnWaterBlockItem::class.java,
    )

    val spawnEggSubclass = ItemSubclassImpl(
        name = Text.translatable(Texts.ITEM_SUBCLASS_SPAWN_EGG),
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
