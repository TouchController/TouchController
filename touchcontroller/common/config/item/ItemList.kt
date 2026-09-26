/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.config.item

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import top.fifthlight.combine.item.data.Item
import top.fifthlight.touchcontroller.common.gal.item.ItemDataComponentType
import top.fifthlight.touchcontroller.common.gal.item.ItemSubclass

@Immutable
@ConsistentCopyVisibility
data class ItemList private constructor(
    private val _whitelist: ItemsList = ItemsList(),
    private val _blacklist: ItemsList = ItemsList(),
    private val _subclasses: ItemSubclassSet = ItemSubclassSet(),
    private val _components: ComponentTypesList = ComponentTypesList(),
) {
    constructor(
        whitelist: PersistentList<Item> = persistentListOf(),
        blacklist: PersistentList<Item> = persistentListOf(),
        components: PersistentList<ItemDataComponentType> = persistentListOf(),
        subclasses: PersistentSet<ItemSubclass> = persistentSetOf(),
    ) : this(
        _whitelist = ItemsList(whitelist),
        _blacklist = ItemsList(blacklist),
        _components = ComponentTypesList(components),
        _subclasses = ItemSubclassSet(subclasses),
    )

    val whitelist: PersistentList<Item>
        get() = _whitelist.items
    val blacklist: PersistentList<Item>
        get() = _blacklist.items
    val components: PersistentList<ItemDataComponentType>
        get() = _components.items
    val subclasses: PersistentSet<ItemSubclass>
        get() = _subclasses.items

    fun copy(
        whitelist: PersistentList<Item> = this.whitelist,
        blacklist: PersistentList<Item> = this.blacklist,
        components: PersistentList<ItemDataComponentType> = this.components,
        subclasses: PersistentSet<ItemSubclass> = this.subclasses,
    ) = ItemList(
        _whitelist = ItemsList(whitelist),
        _blacklist = ItemsList(blacklist),
        _components = ComponentTypesList(components),
        _subclasses = ItemSubclassSet(subclasses),
    )

    operator fun contains(item: Item) = when {
        blacklist.any { it.matches(item) } -> false
        whitelist.any { it.matches(item) } -> true
        components.any { item in it } -> true
        subclasses.any { item in it } -> true
        else -> false
    }
}

@JvmInline
value class ItemsList(val items: PersistentList<Item> = persistentListOf())

@JvmInline
value class ComponentTypesList(val items: PersistentList<ItemDataComponentType> = persistentListOf())

@JvmInline
value class ItemSubclassSet(val items: PersistentSet<ItemSubclass> = persistentSetOf())
