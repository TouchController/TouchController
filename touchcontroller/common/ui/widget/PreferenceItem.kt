/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.widget

import androidx.compose.runtime.Composable
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.layout.Alignment
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.placement.fillMaxWidth
import top.fifthlight.combine.core.modifier.placement.minHeight
import top.fifthlight.combine.core.paint.Color
import top.fifthlight.combine.core.paint.Colors
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.core.widget.layout.ColumnScope
import top.fifthlight.combine.core.widget.layout.Row
import top.fifthlight.combine.core.widget.layout.RowScope
import top.fifthlight.combine.widget.ui.*
import top.fifthlight.touchcontroller.assets.lang.Texts

@Composable
fun VerticalPreferenceItem(
    modifier: Modifier = Modifier,
    title: Text,
    description: Text? = null,
    control: @Composable ColumnScope.() -> Unit = {},
) {
    Column(
        modifier = Modifier
            .minHeight(24)
            .then(modifier),
        verticalArrangement = Arrangement.spacedBy(4),
    ) {
        Text(title)
        description?.let { description ->
            Text(
                text = description,
                color = Colors.SECONDARY_WHITE,
            )
        }

        control()
    }
}


@Composable
fun HorizontalPreferenceItem(
    modifier: Modifier = Modifier,
    title: Text,
    description: Text? = null,
    control: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = Modifier
            .minHeight(24)
            .then(modifier),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4)
        ) {
            Text(title)
            description?.let { description ->
                Text(
                    text = description,
                    color = Colors.SECONDARY_WHITE,
                )
            }
        }

        control()
    }
}

@Composable
fun SwitchPreferenceItem(
    modifier: Modifier = Modifier,
    title: Text,
    description: Text? = null,
    value: Boolean,
    onValueChanged: (Boolean) -> Unit,
) {
    HorizontalPreferenceItem(
        modifier = modifier,
        title = title,
        description = description,
    ) {
        Switch(
            value = value,
            onValueChanged = onValueChanged,
        )
    }
}

@Composable
fun SliderPreferenceItem(
    modifier: Modifier = Modifier,
    title: Text,
    description: Text? = null,
    percent: Boolean = true,
    range: ClosedFloatingPointRange<Float>,
    value: Float,
    onValueChanged: (Float) -> Unit,
) {
    VerticalPreferenceItem(
        modifier = modifier,
        title = if (percent) {
            Text.format(Texts.SCREEN_CONFIG_PERCENT, title, (value * 100).toInt().toString())
        } else {
            Text.format(Texts.SCREEN_CONFIG_VALUE, title, value.toInt().toString())
        },
        description = description,
    ) {
        Slider(
            modifier = Modifier.fillMaxWidth(),
            range = range,
            value = value,
            onValueChanged = onValueChanged,
        )
    }
}

@Composable
fun IntSliderPreferenceItem(
    modifier: Modifier = Modifier,
    title: Text,
    description: Text? = null,
    range: IntRange,
    value: Int,
    onValueChanged: (Int) -> Unit,
) {
    VerticalPreferenceItem(
        modifier = modifier,
        title = Text.format(Texts.SCREEN_CONFIG_VALUE, title, value.toString()),
        description = description,
    ) {
        IntSlider(
            modifier = Modifier.fillMaxWidth(),
            range = range,
            value = value,
            onValueChanged = onValueChanged,
        )
    }
}

@Composable
fun ColorPreferenceItem(
    modifier: Modifier = Modifier,
    title: Text,
    description: Text? = null,
    value: Color,
    onValueChanged: (Color) -> Unit,
) {
    HorizontalPreferenceItem(
        modifier = modifier,
        title = title,
        description = description,
    ) {
        ColorPicker(
            value = value,
            onValueChanged = { onValueChanged(it) },
        )
    }
}
