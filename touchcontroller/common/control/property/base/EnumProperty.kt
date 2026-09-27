/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.control.property.base

import androidx.compose.runtime.*
import kotlinx.collections.immutable.PersistentList
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.placement.fillMaxWidth
import top.fifthlight.combine.core.modifier.scroll.verticalScroll
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.core.widget.layout.Spacer
import top.fifthlight.combine.widget.DropdownItemList
import top.fifthlight.combine.widget.Select
import top.fifthlight.combine.widget.SelectIcon
import top.fifthlight.combine.widget.Text
import top.fifthlight.touchcontroller.common.control.ControllerWidget

@Immutable
class EnumProperty<Config : ControllerWidget, T>(
    getValue: (Config) -> T,
    setValue: (Config, T) -> Config,
    private val name: Text,
    private val items: PersistentList<Pair<T, Text>>,
) : ControllerWidget.Property<Config, T>(getValue, setValue) {
    private fun getItemText(item: T): Text =
        items.firstOrNull { it.first == item }?.second ?: Text.literal(item.toString())

    @Composable
    override fun controller(
        modifier: Modifier,
        config: ControllerWidget,
        context: ConfigContext,
        onConfigChanged: (ControllerWidget) -> Unit,
    ) {
        @Suppress("UNCHECKED_CAST")
        val widgetConfig = config as Config
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(4),
        ) {
            Text(name)

            var expanded by remember { mutableStateOf(false) }
            Select(
                modifier = Modifier.fillMaxWidth(),
                expanded = expanded,
                onExpandedChanged = { expanded = it },
                dropDownContent = {
                    val value = getValue(widgetConfig)
                    val selectedIndex = items.indexOfFirst { it.first == value }
                    DropdownItemList(
                        modifier = Modifier.verticalScroll(),
                        items = items,
                        textProvider = Pair<T, Text>::second,
                        selectedIndex = selectedIndex,
                        onItemSelected = { (item, _), _ ->
                            onConfigChanged(setValue(widgetConfig, item))
                            expanded = false
                        }
                    )
                }
            ) {
                Text(getItemText(getValue(widgetConfig)))
                Spacer(modifier = Modifier.weight(1f))
                SelectIcon(expanded = expanded)
            }
        }
    }
}

fun <Config : ControllerWidget, Value, T> ControllerWidget.Property<Config, Value>.enumProperty(
    getEnum: (Value) -> T?,
    setEnum: (Value, T) -> Value,
    defaultValue: T,
    name: Text,
    items: PersistentList<Pair<T, Text>>,
) = EnumProperty<Config, T>(
    getValue = { getEnum(getValue(it)) ?: defaultValue },
    setValue = { config, value -> setValue(config, setEnum(getValue(config), value)) },
    name = name,
    items = items,
)
