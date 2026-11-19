/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.item.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.updateAndGet
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.plus
import top.fifthlight.combine.item.data.Item

class ItemListComponent(
    componentContext: ComponentContext,
    initialValue: PersistentList<Item>,
    private val onValueChanged: (PersistentList<Item>) -> Unit,
    private val onBack: () -> Unit,
) : ComponentContext by componentContext {
    private val _value = MutableValue(initialValue)
    val value: Value<PersistentList<Item>> = _value

    val itemChooser = ItemChooserComponent(
        componentContext = childContext(key = "itemChooser"),
        onItemChosen = ::addItem,
    )

    fun addItem(item: Item) {
        val newValue = _value.updateAndGet {
            if (item !in it) {
                it + item
            } else {
                it
            }
        }
        onValueChanged(newValue)
    }

    fun removeItem(index: Int) {
        val newValue = _value.updateAndGet { it.removeAt(index) }
        onValueChanged(newValue)
    }

    fun onBack() {
        onBack.invoke()
    }
}
