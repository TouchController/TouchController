/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.control.property.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.layout.Alignment
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.widget.layout.Row
import top.fifthlight.combine.core.widget.layout.Spacer
import top.fifthlight.combine.widget.Switch
import top.fifthlight.combine.widget.Text
import top.fifthlight.touchcontroller.common.control.ControllerWidget

@Immutable
class BooleanProperty<Config : ControllerWidget>(
    getValue: (Config) -> Boolean,
    setValue: (Config, Boolean) -> Config,
    private val name: Text,
) : ControllerWidget.Property<Config, Boolean>(getValue, setValue) {
    @Composable
    override fun controller(
        modifier: Modifier,
        config: ControllerWidget,
        context: ConfigContext,
        onConfigChanged: (ControllerWidget) -> Unit,
    ) {
        @Suppress("UNCHECKED_CAST")
        val widgetConfig = config as Config
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(name)
            Spacer(modifier.weight(1f))
            Switch(
                value = getValue(widgetConfig),
                onValueChanged = {
                    onConfigChanged(setValue(widgetConfig, it))
                }
            )
        }
    }
}

