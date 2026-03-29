/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.gal.item.v1_21_10

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.BuiltInRegistries
import top.fifthlight.combine.backend.minecraft.identifier.v1_21_10.toCombine
import top.fifthlight.combine.backend.minecraft.identifier.v1_21_10.toMinecraft
import top.fifthlight.combine.backend.minecraft.item.v1_21_10.toCombine
import top.fifthlight.combine.backend.minecraft.item.v1_21_10.toVanilla
import top.fifthlight.combine.data.Identifier
import top.fifthlight.combine.item.data.Item
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.common.gal.item.ItemDataComponentType
import top.fifthlight.touchcontroller.common.gal.item.ItemDataComponentTypeProvider
import kotlin.jvm.optionals.getOrNull

@ActualImpl(ItemDataComponentType::class)
data class ItemDataComponentTypeImpl(val component: DataComponentType<*>) : ItemDataComponentType {
    override val id: Identifier?
        get() = BuiltInRegistries.DATA_COMPONENT_TYPE.getResourceKey(component).getOrNull()?.location()?.toCombine()

    override val allItems: PersistentList<Item> by lazy {
        BuiltInRegistries.ITEM
            .filter { it.components().has(component) }
            .map { it.toCombine() }
            .toPersistentList()
    }

    override fun contains(item: Item): Boolean = item.toVanilla().components().has(component)

    companion object {
        @JvmStatic
        @ActualConstructor
        fun of(id: Identifier): ItemDataComponentType? = BuiltInRegistries.DATA_COMPONENT_TYPE
            .getOptional(id.toMinecraft())
            .getOrNull()
            ?.let(::ItemDataComponentTypeImpl)
    }
}

@ActualImpl(ItemDataComponentTypeProvider::class)
object ItemDataComponentTypeProviderImpl : ItemDataComponentTypeProvider {
    @JvmStatic
    @ActualConstructor
    fun of(): ItemDataComponentTypeProvider = ItemDataComponentTypeProviderImpl

    override val supportDataComponents: Boolean
        get() = true

    override val allComponents: PersistentList<ItemDataComponentType> by lazy {
        BuiltInRegistries.DATA_COMPONENT_TYPE.map { ItemDataComponentTypeImpl(it) }.toPersistentList()
    }
}
