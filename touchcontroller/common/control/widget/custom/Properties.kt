/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.control.widget.custom

import androidx.compose.runtime.*
import kotlinx.collections.immutable.toPersistentList
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.placement.fillMaxWidth
import top.fifthlight.combine.core.modifier.scroll.verticalScroll
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.core.widget.layout.Spacer
import top.fifthlight.combine.widget.ui.DropdownItemList
import top.fifthlight.combine.widget.ui.Select
import top.fifthlight.combine.widget.ui.SelectIcon
import top.fifthlight.combine.widget.ui.Text
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.assets.texture.empty.EmptyTexture
import top.fifthlight.touchcontroller.common.control.*

fun <Config : ControllerWidget, Value> ControllerWidget.Property<Config, Value>.buttonTextureProperty(
    getTexture: (Value) -> ButtonTexture?,
    setTexture: (Value, ButtonTexture) -> Value,
    name: Text,
) = ButtonTextureProperty<Config>(
    getValue = { getTexture(getValue(it)) ?: ButtonTexture.Empty() },
    setValue = { config, value -> setValue(config, setTexture(getValue(config), value)) },
    name = name,
)

@Immutable
class ButtonTextureProperty<Config : ControllerWidget>(
    getValue: (Config) -> ButtonTexture,
    setValue: (Config, ButtonTexture) -> Config,
    private val name: Text,
) : ControllerWidget.Property<Config, ButtonTexture>(getValue, setValue) {
    private val emptyTexturePaddingProperty = paddingProperty(
        getPadding = { (it as? ButtonTexture.Empty)?.extraPadding },
        setPadding = { texture, value ->
            when (texture) {
                is ButtonTexture.Empty -> texture.copy(extraPadding = value)
                else -> ButtonTexture.Empty(extraPadding = value)
            }
        },
        name = Text.translatable(Texts.WIDGET_TEXTURE_EXTRA_PADDING),
    )

    private val fillTextureBorderWidthProperty = intProperty(
        getInt = { (it as? ButtonTexture.Fill)?.borderWidth },
        setInt = { texture, value ->
            when (texture) {
                is ButtonTexture.Fill -> texture.copy(borderWidth = value)
                else -> ButtonTexture.Fill(borderWidth = value)
            }
        },
        range = 0..16,
        name = Text.translatable(Texts.WIDGET_TEXTURE_FILL_BORDER_WIDTH),
    )

    private val fillTexturePaddingProperty = paddingProperty(
        getPadding = { (it as? ButtonTexture.Fill)?.extraPadding },
        setPadding = { texture, value ->
            when (texture) {
                is ButtonTexture.Fill -> texture.copy(extraPadding = value)
                else -> ButtonTexture.Fill(extraPadding = value)
            }
        },
        name = Text.translatable(Texts.WIDGET_TEXTURE_EXTRA_PADDING),
    )

    private val fillTextureBorderColorProperty = colorProperty(
        getColor = { (it as? ButtonTexture.Fill)?.borderColor },
        setColor = { texture, value ->
            when (texture) {
                is ButtonTexture.Fill -> texture.copy(borderColor = value)
                else -> ButtonTexture.Fill(borderColor = value)
            }
        },
        name = Text.translatable(Texts.WIDGET_TEXTURE_FILL_BORDER_COLOR),
    )

    private val fillTextureBackgroundColorProperty = colorProperty(
        getColor = { (it as? ButtonTexture.Fill)?.backgroundColor },
        setColor = { texture, value ->
            when (texture) {
                is ButtonTexture.Fill -> texture.copy(backgroundColor = value)
                else -> ButtonTexture.Fill(backgroundColor = value)
            }
        },
        name = Text.translatable(Texts.WIDGET_TEXTURE_FILL_BACKGROUND_COLOR),
    )

    private val fixedTextureCoordinateProperty = textureCoordinateProperty(
        getCoordinate = { (it as? ButtonTexture.Fixed)?.texture },
        setCoordinate = { texture, value ->
            when (texture) {
                is ButtonTexture.Fixed -> texture.copy(texture = value)
                else -> ButtonTexture.Fixed(texture = value)
            }
        },
        name = Text.translatable(Texts.WIDGET_TEXTURE_FIXED_TEXTURE),
    )

    private val fixedTextureScaleProperty = scaleProperty(
        getScale = { (it as? ButtonTexture.Fixed)?.scale },
        setScale = { texture, value ->
            when (texture) {
                is ButtonTexture.Fixed -> texture.copy(scale = value)
                else -> ButtonTexture.Fixed(scale = value)
            }
        },
        range = .5f..4f,
        name = Text.translatable(Texts.WIDGET_TEXTURE_FIXED_SCALE),
    )

    private val ninePatchTextureTextureProperty = enumProperty(
        getEnum = { (it as? ButtonTexture.NinePatch)?.texture },
        setEnum = { texture, value ->
            when (texture) {
                is ButtonTexture.NinePatch -> texture.copy(texture = value)
                else -> ButtonTexture.NinePatch(texture = value)
            }
        },
        defaultValue = EmptyTexture.EMPTY_1,
        name = Text.translatable(Texts.WIDGET_TEXTURE_NINE_PATCH_TEXTURE),
        items = EmptyTexture.entries.map { Pair(it, Text.translatable(it.nameId)) }.toPersistentList(),
    )

    private val ninePatchTexturePaddingProperty = paddingProperty(
        getPadding = { (it as? ButtonTexture.NinePatch)?.extraPadding },
        setPadding = { texture, value ->
            when (texture) {
                is ButtonTexture.NinePatch -> texture.copy(extraPadding = value)
                else -> ButtonTexture.NinePatch(extraPadding = value)
            }
        },
        name = Text.translatable(Texts.WIDGET_TEXTURE_EXTRA_PADDING),
    )

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
            var expanded by remember { mutableStateOf(false) }
            Select(
                modifier = Modifier.fillMaxWidth(),
                expanded = expanded,
                onExpandedChanged = { expanded = it },
                dropDownContent = {
                    DropdownItemList(
                        modifier = Modifier.verticalScroll(),
                        items = ButtonTexture.Type.entries,
                        textProvider = { Text.translatable(it.nameId) },
                        selectedIndex = ButtonTexture.Type.entries.indexOf(value.type),
                        onItemSelected = { item, _ ->
                            if (value.type != item) {
                                onConfigChanged(
                                    setValue(
                                        widgetConfig, when (item) {
                                            ButtonTexture.Type.EMPTY -> ButtonTexture.Empty()
                                            ButtonTexture.Type.FILL -> ButtonTexture.Fill()
                                            ButtonTexture.Type.FIXED -> ButtonTexture.Fixed()
                                            ButtonTexture.Type.NINE_PATCH -> ButtonTexture.NinePatch()
                                        }
                                    )
                                )
                            }
                            expanded = false
                        }
                    )
                }
            ) {
                Text(Text.translatable(value.type.nameId))
                Spacer(modifier = Modifier.weight(1f))
                SelectIcon(expanded = expanded)
            }

            @Composable
            fun <Config : ControllerWidget> ControllerWidget.Property<Config, *>.controller() = controller(
                modifier = Modifier.fillMaxWidth(),
                config = config,
                context = context,
                onConfigChanged = onConfigChanged,
            )

            when (value) {
                is ButtonTexture.Empty -> {
                    emptyTexturePaddingProperty.controller()
                }

                is ButtonTexture.Fill -> {
                    fillTexturePaddingProperty.controller()
                    fillTextureBorderWidthProperty.controller()
                    fillTextureBorderColorProperty.controller()
                    fillTextureBackgroundColorProperty.controller()
                }

                is ButtonTexture.Fixed -> {
                    fixedTextureCoordinateProperty.controller()
                    fixedTextureScaleProperty.controller()
                }

                is ButtonTexture.NinePatch -> {
                    ninePatchTextureTextureProperty.controller()
                    ninePatchTexturePaddingProperty.controller()
                }
            }
        }
    }
}

@Immutable
class ButtonActiveTextureProperty<Config : ControllerWidget>(
    getValue: (Config) -> ButtonActiveTexture,
    setValue: (Config, ButtonActiveTexture) -> Config,
    private val name: Text,
) : ControllerWidget.Property<Config, ButtonActiveTexture>(getValue, setValue) {
    private val textureProperty = buttonTextureProperty(
        getTexture = { (it as? ButtonActiveTexture.Texture)?.texture },
        setTexture = { texture, value ->
            when (texture) {
                is ButtonActiveTexture.Texture -> texture.copy(texture = value)
                else -> ButtonActiveTexture.Texture(texture = value)
            }
        },
        name = Text.translatable(Texts.WIDGET_ACTIVE_TEXTURE_TYPE),
    )

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
            var expanded by remember { mutableStateOf(false) }
            Select(
                modifier = Modifier.fillMaxWidth(),
                expanded = expanded,
                onExpandedChanged = { expanded = it },
                dropDownContent = {
                    DropdownItemList(
                        modifier = Modifier.verticalScroll(),
                        items = ButtonActiveTexture.Type.entries,
                        textProvider = { Text.translatable(it.nameId) },
                        selectedIndex = ButtonActiveTexture.Type.entries.indexOf(value.type),
                        onItemSelected = { item, _ ->
                            if (value.type != item) {
                                onConfigChanged(
                                    setValue(
                                        widgetConfig, when (item) {
                                            ButtonActiveTexture.Type.SAME -> ButtonActiveTexture.Same
                                            ButtonActiveTexture.Type.GRAY -> ButtonActiveTexture.Gray
                                            ButtonActiveTexture.Type.TEXTURE -> ButtonActiveTexture.Texture()
                                        }
                                    )
                                )
                            }
                            expanded = false
                        }
                    )
                }
            ) {
                Text(Text.translatable(value.type.nameId))
                Spacer(modifier = Modifier.weight(1f))
                SelectIcon(expanded = expanded)
            }

            @Composable
            fun <Config : ControllerWidget> ControllerWidget.Property<Config, *>.controller() = controller(
                modifier = Modifier.fillMaxWidth(),
                config = config,
                context = context,
                onConfigChanged = onConfigChanged,
            )

            if (value is ButtonActiveTexture.Texture) {
                textureProperty.controller()
            }
        }
    }
}
