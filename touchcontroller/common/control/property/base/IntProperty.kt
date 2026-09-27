/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.control.property.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.data.TextFactory
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.placement.fillMaxWidth
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.widget.IntSlider
import top.fifthlight.combine.widget.Text
import top.fifthlight.data.IntPadding
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.common.control.ControllerWidget

@Immutable
class IntProperty<Config : ControllerWidget>(
    getValue: (Config) -> Int,
    setValue: (Config, Int) -> Config,
    private val range: IntRange,
    private val messageFormatter: (Int) -> Text,
) : ControllerWidget.Property<Config, Int>(getValue, setValue) {
    @Composable
    override fun controller(
        modifier: Modifier,
        config: ControllerWidget,
        context: ConfigContext,
        onConfigChanged: (ControllerWidget) -> Unit,
    ) {
        @Suppress("UNCHECKED_CAST")
        val widgetConfig = config as Config
        Column(modifier) {
            val value = getValue(widgetConfig)
            Text(messageFormatter(value))
            IntSlider(
                modifier = Modifier.fillMaxWidth(),
                value = value,
                range = range,
                onValueChanged = {
                    onConfigChanged(setValue(widgetConfig, it))
                }
            )
        }
    }
}

fun <Config : ControllerWidget, Value> ControllerWidget.Property<Config, Value>.intProperty(
    getInt: (Value) -> Int?,
    setInt: (Value, Int) -> Value,
    range: IntRange,
    name: Text,
) = IntProperty<Config>(
    getValue = { getInt(getValue(it)) ?: 0 },
    setValue = { config, value -> setValue(config, setInt(getValue(config), value)) },
    range = range,
    messageFormatter = {
        Text.format(Texts.SCREEN_CONFIG_VALUE, TextFactory.toNative(name), it.toString())
    },
)

fun <Config : ControllerWidget, Value> ControllerWidget.Property<Config, Value>.paddingProperty(
    getPadding: (Value) -> IntPadding?,
    setPadding: (Value, IntPadding) -> Value,
    name: Text,
) = IntPaddingProperty<Config>(
    getValue = { getPadding(getValue(it)) ?: IntPadding.ZERO },
    setValue = { config, value -> setValue(config, setPadding(getValue(config), value)) },
    name = name,
)
