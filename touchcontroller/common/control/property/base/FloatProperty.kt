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
import top.fifthlight.combine.widget.Slider
import top.fifthlight.combine.widget.Text
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.common.control.ControllerWidget

@Immutable
class FloatProperty<Config : ControllerWidget>(
    getValue: (Config) -> Float,
    setValue: (Config, Float) -> Config,
    private val range: ClosedFloatingPointRange<Float> = 0f..1f,
    private val messageFormatter: (Float) -> Text,
) : ControllerWidget.Property<Config, Float>(getValue, setValue) {
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
            Slider(
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

fun <Config : ControllerWidget, Value> ControllerWidget.Property<Config, Value>.scaleProperty(
    getScale: (Value) -> Float?,
    setScale: (Value, Float) -> Value,
    range: ClosedFloatingPointRange<Float> = 0f..1f,
    name: Text,
) = FloatProperty<Config>(
    getValue = { getScale(getValue(it)) ?: 0f },
    setValue = { config, value -> setValue(config, setScale(getValue(config), value)) },
    range = range,
    messageFormatter = {
        Text.format(
            Texts.SCREEN_CONFIG_PERCENT,
            TextFactory.toNative(name),
            (it * 100).toInt().toString()
        )
    },
)
