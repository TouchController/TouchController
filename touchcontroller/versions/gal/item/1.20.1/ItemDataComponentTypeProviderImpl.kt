/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.gal.item.v1_20_1

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import net.minecraft.core.registries.BuiltInRegistries
import top.fifthlight.combine.backend.minecraft.item.v1_20_1.toCombine
import top.fifthlight.combine.backend.minecraft.item.v1_20_1.toVanilla
import top.fifthlight.combine.core.data.Identifier
import top.fifthlight.combine.item.data.Item
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.buildinfo.BuildInfo
import top.fifthlight.touchcontroller.common.gal.item.ItemDataComponentType
import top.fifthlight.touchcontroller.common.gal.item.ItemDataComponentTypeProvider

@ActualImpl(ItemDataComponentType::class)
sealed class ItemDataComponentTypeImpl(override val id: Identifier) : ItemDataComponentType {
    object FoodItemDataComponentType: ItemDataComponentTypeImpl(Identifier.of(BuildInfo.MOD_ID, "food")) {
        override val id: Identifier = Identifier.of(BuildInfo.MOD_ID, "food")

        override val allItems: PersistentList<Item> by lazy {
            BuiltInRegistries.ITEM.filter { it.isEdible }.map { it.toCombine() }.toPersistentList()
        }

        override fun contains(item: Item) = item.toVanilla().isEdible
    }

    companion object {
        @JvmStatic
        @ActualConstructor
        fun of(id: Identifier): ItemDataComponentType? = when (id) {
            FoodItemDataComponentType.id -> FoodItemDataComponentType
            else -> null
        }
    }
}

@ActualImpl(ItemDataComponentTypeProvider::class)
object ItemDataComponentTypeProviderImpl : ItemDataComponentTypeProvider {
    @JvmStatic
    @ActualConstructor
    fun of(): ItemDataComponentTypeProvider = ItemDataComponentTypeProviderImpl

    override val supportDataComponents: Boolean
        get() = false

    override val allComponents: PersistentList<ItemDataComponentType> = persistentListOf(
        ItemDataComponentTypeImpl.FoodItemDataComponentType,
    )
}
