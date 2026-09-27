/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.control.property.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.layout.Alignment
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.paint.Color
import top.fifthlight.combine.core.paint.Colors
import top.fifthlight.combine.core.widget.layout.Row
import top.fifthlight.combine.widget.ColorPicker
import top.fifthlight.combine.widget.Text
import top.fifthlight.touchcontroller.common.control.ControllerWidget

@Immutable
class ColorProperty<Config : ControllerWidget>(
    getValue: (Config) -> Color,
    setValue: (Config, Color) -> Config,
    private val name: Text,
) : ControllerWidget.Property<Config, Color>(getValue, setValue) {
    @Composable
    override fun controller(
        modifier: Modifier,
        config: ControllerWidget,
        context: ConfigContext,
        onConfigChanged: (ControllerWidget) -> Unit,
    ) {
        @Suppress("UNCHECKED_CAST")
        val widgetConfig = config as Config
        val value = getValue(widgetConfig)
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4),
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = name,
            )
            ColorPicker(
                value = value,
                onValueChanged = { onConfigChanged(setValue(config, it)) }
            )
        }
    }
}

fun <Config : ControllerWidget, Value> ControllerWidget.Property<Config, Value>.colorProperty(
    getColor: (Value) -> Color?,
    setColor: (Value, Color) -> Value,
    name: Text,
) = ColorProperty<Config>(
    getValue = { getColor(getValue(it)) ?: Colors.BLACK },
    setValue = { config, value -> setValue(config, setColor(getValue(config), value)) },
    name = name,
)
