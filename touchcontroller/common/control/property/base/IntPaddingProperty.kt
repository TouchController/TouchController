/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.control.property.base

import androidx.compose.runtime.*
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.layout.Alignment
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.placement.width
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.core.widget.layout.Row
import top.fifthlight.combine.widget.EditText
import top.fifthlight.combine.widget.IntSlider
import top.fifthlight.combine.widget.Text
import top.fifthlight.data.IntPadding
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.common.control.ControllerWidget

@Immutable
class IntPaddingProperty<Config : ControllerWidget>(
    getValue: (Config) -> IntPadding,
    setValue: (Config, IntPadding) -> Config,
    private val name: Text,
) : ControllerWidget.Property<Config, IntPadding>(getValue, setValue) {
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
            @Composable
            fun PaddingItem(
                text: Text,
                getSize: (IntPadding) -> Int,
                setSize: (IntPadding, Int) -> IntPadding,
            ) {
                val sizeValue = getSize(value)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text)
                    IntSlider(
                        modifier = Modifier.weight(1f),
                        range = 0..32,
                        value = sizeValue,
                        onValueChanged = {
                            onConfigChanged(setValue(config, setSize(value, it)))
                        }
                    )
                    var text by remember(sizeValue) { mutableStateOf(sizeValue.toString()) }
                    LaunchedEffect(text) {
                        val newSize = text.toIntOrNull() ?: return@LaunchedEffect
                        if (newSize == sizeValue) {
                            return@LaunchedEffect
                        }
                        onConfigChanged(setValue(config, setSize(value, newSize)))
                    }
                    EditText(
                        modifier = Modifier.width(48),
                        value = text,
                        onValueChanged = { text = it },
                    )
                }
            }

            Text(name)

            PaddingItem(
                text = Text.translatable(Texts.WIDGET_TEXTURE_EXTRA_PADDING_LEFT),
                getSize = IntPadding::left,
                setSize = { padding, size -> padding.copy(left = size) },
            )
            PaddingItem(
                text = Text.translatable(Texts.WIDGET_TEXTURE_EXTRA_PADDING_TOP),
                getSize = IntPadding::top,
                setSize = { padding, size -> padding.copy(top = size) },
            )
            PaddingItem(
                text = Text.translatable(Texts.WIDGET_TEXTURE_EXTRA_PADDING_RIGHT),
                getSize = IntPadding::right,
                setSize = { padding, size -> padding.copy(right = size) },
            )
            PaddingItem(
                text = Text.translatable(Texts.WIDGET_TEXTURE_EXTRA_PADDING_BOTTOM),
                getSize = IntPadding::bottom,
                setSize = { padding, size -> padding.copy(bottom = size) },
            )
        }
    }
}

