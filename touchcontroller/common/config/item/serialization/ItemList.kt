/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.config.item.serialization

import com.ubertob.kondor.json.*
import com.ubertob.kondor.json.jsonnode.FieldsValues
import com.ubertob.kondor.json.jsonnode.NodePath
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentSet
import top.fifthlight.combine.core.data.Identifier
import top.fifthlight.touchcontroller.common.config.item.ItemList
import top.fifthlight.touchcontroller.common.gal.item.ItemDataComponentType
import top.fifthlight.touchcontroller.common.gal.item.ItemDataComponentTypeFactory
import top.fifthlight.touchcontroller.common.gal.item.ItemSubclass
import top.fifthlight.touchcontroller.common.gal.item.ItemSubclassProvider
import top.fifthlight.touchcontroller.common.serialization.JItem

object JItemDataComponentType : JStringRepresentable<ItemDataComponentType>() {
    override val cons: (String) -> ItemDataComponentType = { id ->
        ItemDataComponentTypeFactory.of(Identifier(id)) ?: error("Unknown item data component type: $id")
    }

    override val render: (ItemDataComponentType) -> String = { type ->
        type.id?.toString() ?: error("Item data component type has no id: $type")
    }
}

object JItemSubclass : JStringRepresentable<ItemSubclass>() {
    private val allSubclasses = ItemSubclassProvider.itemSubclasses

    override val cons: (String) -> ItemSubclass = { configId ->
        allSubclasses.firstOrNull { it.configId == configId } ?: error("Unknown item subclass: $configId")
    }

    override val render: (ItemSubclass) -> String = ItemSubclass::configId
}

object JItemList : JObj<ItemList>() {
    private val whitelist by JFieldMaybe(ItemList::whitelist, JList(JItem))
    private val blacklist by JFieldMaybe(ItemList::blacklist, JList(JItem))
    private val components by JFieldMaybe(ItemList::components, JList(JItemDataComponentType))
    private val subclasses by JFieldMaybe(ItemList::subclasses, JSet(JItemSubclass))

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = ItemList(
        whitelist = (+whitelist)?.toPersistentList() ?: persistentListOf(),
        blacklist = (+blacklist)?.toPersistentList() ?: persistentListOf(),
        components = (+components)?.toPersistentList() ?: persistentListOf(),
        subclasses = (+subclasses)?.toPersistentSet() ?: persistentSetOf(),
    )
}
