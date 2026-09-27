/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.control.property.texture

import androidx.compose.runtime.*
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.layout.Alignment
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.drawing.background
import top.fifthlight.combine.core.modifier.drawing.innerLine
import top.fifthlight.combine.core.modifier.placement.*
import top.fifthlight.combine.core.modifier.pointer.clickable
import top.fifthlight.combine.core.modifier.scroll.verticalScroll
import top.fifthlight.combine.core.paint.Colors
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.core.widget.layout.FlowRow
import top.fifthlight.combine.core.widget.layout.Row
import top.fifthlight.combine.core.widget.layout.Spacer
import top.fifthlight.combine.widget.*
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.common.assets.TextureItems
import top.fifthlight.touchcontroller.common.assets.TextureSet
import top.fifthlight.touchcontroller.common.assets.TextureSets
import top.fifthlight.touchcontroller.common.control.ControllerWidget
import top.fifthlight.touchcontroller.common.control.texture.TextureCoordinate
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme
import top.fifthlight.touchcontroller.common.ui.widget.Scaffold
import top.fifthlight.touchcontroller.common.ui.widget.navigation.AppBar
import top.fifthlight.touchcontroller.common.ui.widget.navigation.BackButton

fun <Config : ControllerWidget, Value> ControllerWidget.Property<Config, Value>.textureCoordinateProperty(
    getCoordinate: (Value) -> TextureCoordinate?,
    setCoordinate: (Value, TextureCoordinate) -> Value,
    name: Text,
) = TextureCoordinateProperty<Config>(
    getValue = { getCoordinate(getValue(it)) ?: TextureCoordinate() },
    setValue = { config, value -> setValue(config, setCoordinate(getValue(config), value)) },
    name = name,
)

@Immutable
class TextureCoordinateProperty<Config : ControllerWidget>(
    getValue: (Config) -> TextureCoordinate,
    setValue: (Config, TextureCoordinate) -> Config,
    private val name: Text,
) : ControllerWidget.Property<Config, TextureCoordinate>(getValue, setValue) {
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
            modifier = Modifier
                .height(24)
                .then(modifier),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4),
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = name,
            )
            Icon(
                modifier = Modifier.fillMaxHeight(),
                drawable = value.texture,
            )
            Spacer(modifier = Modifier.width(4))
            var showDialog by remember { mutableStateOf(false) }
            Button(
                modifier = Modifier.fillMaxHeight(),
                onClick = {
                    showDialog = true
                }
            ) {
                Text(Text.translatable(Texts.TEXTURE_COORDINATE_EDIT))
            }

            if (showDialog) {
                FullScreenDialog(
                    onDismissRequest = { showDialog = false }
                ) {
                    Scaffold(
                        topBar = {
                            AppBar(
                                modifier = Modifier.fillMaxWidth(),
                                leading = {
                                    BackButton(
                                        screenName = Text.translatable(Texts.SCREEN_TEXTURE_COORDINATE_SELECT),
                                        onClick = {
                                            showDialog = false
                                        },
                                    )
                                },
                            )
                        },
                    ) { modifier ->
                        Column(
                            modifier = Modifier
                                .padding(4)
                                .background(LocalTouchControllerTheme.current.background)
                                .then(modifier),
                            verticalArrangement = Arrangement.spacedBy(4),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4),
                            ) {
                                Text(Text.translatable(Texts.TEXTURE_COORDINATE_TEXTURE_SET))

                                var expanded by remember { mutableStateOf(false) }
                                Select(
                                    expanded = expanded,
                                    onExpandedChanged = { expanded = it },
                                    dropDownContent = {
                                        DropdownItemList(
                                            modifier = Modifier.verticalScroll(),
                                            items = TextureSets.registry.values(),
                                            textProvider = TextureSet::name,
                                            selectedIndex = TextureSets.registry.values().indexOf(value.textureSet),
                                            onItemSelected = { item, _ ->
                                                onConfigChanged(setValue(config, value.copy(textureSet = item)))
                                                expanded = false
                                            }
                                        )
                                    }
                                ) {
                                    Text(value.textureSet.name)
                                    Spacer(modifier = Modifier.width(8))
                                    SelectIcon(expanded = expanded)
                                }
                            }

                            FlowRow(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .verticalScroll()
                            ) {
                                for (key in TextureItems.registry) {
                                    val borderModifier = if (key == value.textureItem) {
                                        Modifier.innerLine(Colors.WHITE)
                                    } else {
                                        Modifier
                                    }
                                    Column(
                                        modifier = Modifier
                                            .then(borderModifier)
                                            .width(72)
                                            .height(86)
                                            .clickable {
                                                onConfigChanged(setValue(config, value.copy(textureItem = key)))
                                            },
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4),
                                    ) {
                                        val texture = remember(value.textureSet, key) {
                                            key.get(value.textureSet)
                                        }
                                        Icon(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxWidth(),
                                            drawable = texture,
                                        )
                                        Text(Text.literal(key.name))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

