/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.control.property.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.placement.fillMaxWidth
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.widget.EditText
import top.fifthlight.combine.widget.Text
import top.fifthlight.touchcontroller.common.control.ControllerWidget

@Immutable
class StringProperty<Config : ControllerWidget>(
    getValue: (Config) -> String,
    setValue: (Config, String) -> Config,
    private val name: Text,
) : ControllerWidget.Property<Config, String>(getValue, setValue) {
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
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(4),
        ) {
            Text(name)
            EditText(
                modifier = Modifier.fillMaxWidth(),
                value = value,
                onValueChanged = { onConfigChanged(setValue(config, it)) }
            )
        }
    }
}

