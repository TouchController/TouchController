/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.control.widget.dpad

import androidx.compose.runtime.*
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
import top.fifthlight.touchcontroller.common.control.*

private fun <Config : ControllerWidget, Value> ControllerWidget.Property<Config, Value>.dpadActiveTextureProperty(
    getTexture: (Value) -> DPadExtraButton.ActiveTexture?,
    setTexture: (Value, DPadExtraButton.ActiveTexture) -> Value,
) = DPadActiveTextureProperty<Config>(
    getValue = { getTexture(getValue(it)) ?: DPadExtraButton.ActiveTexture.Same },
    setValue = { config, value -> setValue(config, setTexture(getValue(config), value)) },
)

private fun <Config : ControllerWidget, Value> ControllerWidget.Property<Config, Value>.dpadButtonInfoProperty(
    getInfo: (Value) -> DPadExtraButton.ButtonInfo?,
    setInfo: (Value, DPadExtraButton.ButtonInfo) -> Value,
) = DPadButtonInfoProperty<Config>(
    getValue = { getInfo(getValue(it)) ?: DPadExtraButton.ButtonInfo() },
    setValue = { config, value -> setValue(config, setInfo(getValue(config), value)) },
)

@Immutable
class DPadActiveTextureProperty<Config : ControllerWidget>(
    getValue: (Config) -> DPadExtraButton.ActiveTexture,
    setValue: (Config, DPadExtraButton.ActiveTexture) -> Config,
) : ControllerWidget.Property<Config, DPadExtraButton.ActiveTexture>(getValue, setValue) {
    private val textureProperty = textureCoordinateProperty(
        getCoordinate = { (it as? DPadExtraButton.ActiveTexture.Texture)?.texture },
        setCoordinate = { texture, value ->
            when (texture) {
                is DPadExtraButton.ActiveTexture.Texture -> texture.copy(texture = value)
                else -> texture
            }
        },
        name = Text.translatable(Texts.WIDGET_DPAD_PROPERTY_EXTRA_BUTTON_ACTIVE_TEXTURE_TYPE),
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

        @Composable
        fun <Config : ControllerWidget> ControllerWidget.Property<Config, *>.controller() = controller(
            modifier = Modifier.fillMaxWidth(),
            config = config,
            context = context,
            onConfigChanged = onConfigChanged,
        )

        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(4),
        ) {
            Text(Text.translatable(Texts.WIDGET_DPAD_PROPERTY_EXTRA_BUTTON_ACTIVE_TEXTURE))
            var expanded by remember { mutableStateOf(false) }
            Select(
                modifier = Modifier.fillMaxWidth(),
                expanded = expanded,
                onExpandedChanged = { expanded = it },
                dropDownContent = {
                    DropdownItemList(
                        modifier = Modifier.verticalScroll(),
                        items = DPadExtraButton.ActiveTexture.Type.entries,
                        textProvider = { Text.translatable(it.nameId) },
                        selectedIndex = DPadExtraButton.ActiveTexture.Type.entries.indexOf(value.type),
                        onItemSelected = { item, _ ->
                            if (value.type != item) {
                                onConfigChanged(
                                    setValue(
                                        widgetConfig, when (item) {
                                            DPadExtraButton.ActiveTexture.Type.SAME -> DPadExtraButton.ActiveTexture.Same
                                            DPadExtraButton.ActiveTexture.Type.GRAY -> DPadExtraButton.ActiveTexture.Gray
                                            DPadExtraButton.ActiveTexture.Type.TEXTURE -> DPadExtraButton.ActiveTexture.Texture()
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

            if (value is DPadExtraButton.ActiveTexture.Texture) {
                textureProperty.controller()
            }
        }
    }
}

@Immutable
class DPadButtonInfoProperty<Config : ControllerWidget>(
    getValue: (Config) -> DPadExtraButton.ButtonInfo,
    setValue: (Config, DPadExtraButton.ButtonInfo) -> Config,
) : ControllerWidget.Property<Config, DPadExtraButton.ButtonInfo>(getValue, setValue) {
    private val sizeProperty = intProperty(
        getInt = { it.size },
        setInt = { config, value -> config.copy(size = value) },
        range = 12..22,
        name = Text.translatable(Texts.WIDGET_DPAD_PROPERTY_EXTRA_BUTTON_SIZE)
    )

    private val textureProperty = textureCoordinateProperty(
        getCoordinate = { it.texture },
        setCoordinate = { config, value -> config.copy(texture = value) },
        name = Text.translatable(Texts.WIDGET_DPAD_PROPERTY_EXTRA_BUTTON_TEXTURE)
    )

    private val activeTextureProperty = dpadActiveTextureProperty(
        getTexture = { it.activeTexture },
        setTexture = { config, value -> config.copy(activeTexture = value) },
    )

    @Composable
    override fun controller(
        modifier: Modifier,
        config: ControllerWidget,
        context: ConfigContext,
        onConfigChanged: (ControllerWidget) -> Unit,
    ) {
        @Composable
        fun <Config : ControllerWidget> ControllerWidget.Property<Config, *>.controller() = controller(
            modifier = Modifier.fillMaxWidth(),
            config = config,
            context = context,
            onConfigChanged = onConfigChanged,
        )

        sizeProperty.controller()
        textureProperty.controller()
        activeTextureProperty.controller()
    }
}

@Immutable
class DPadExtraButtonProperty<Config : ControllerWidget>(
    getValue: (Config) -> DPadExtraButton,
    setValue: (Config, DPadExtraButton) -> Config,
) : ControllerWidget.Property<Config, DPadExtraButton>(getValue, setValue) {
    private val normalTriggerProperty = triggerProperty(
        getTrigger = { (it as? DPadExtraButton.Normal)?.trigger },
        setTrigger = { config, value ->
            when (config) {
                is DPadExtraButton.Normal -> config.copy(
                    trigger = value
                )

                else -> config
            }
        }
    )

    private val normalButtonInfoProperty = dpadButtonInfoProperty(
        getInfo = { (it as? DPadExtraButton.Normal)?.info },
        setInfo = { config, value ->
            when (config) {
                is DPadExtraButton.Normal -> config.copy(
                    info = value
                )

                else -> config
            }
        }
    )

    private val swipeTriggerProperty = triggerProperty(
        getTrigger = { (it as? DPadExtraButton.Swipe)?.trigger },
        setTrigger = { config, value ->
            when (config) {
                is DPadExtraButton.Swipe -> config.copy(
                    trigger = value
                )

                else -> config
            }
        }
    )

    private val swipeButtonInfoProperty = dpadButtonInfoProperty(
        getInfo = { (it as? DPadExtraButton.Swipe)?.info },
        setInfo = { config, value ->
            when (config) {
                is DPadExtraButton.Swipe -> config.copy(
                    info = value
                )

                else -> config
            }
        }
    )

    private val swipeLockingTriggerProperty = keyBindingProperty(
        getKeyBinding = { (it as? DPadExtraButton.SwipeLocking)?.press },
        setKeyBinding = { config, value ->
            when (config) {
                is DPadExtraButton.SwipeLocking -> config.copy(
                    press = value
                )

                else -> config
            }
        },
        name = Text.translatable(Texts.WIDGET_DPAD_PROPERTY_EXTRA_BUTTON_SWIPE_LOCKING_KEY_BINDING),
    )

    private val swipeLockingButtonInfoProperty = dpadButtonInfoProperty(
        getInfo = { (it as? DPadExtraButton.SwipeLocking)?.info },
        setInfo = { config, value ->
            when (config) {
                is DPadExtraButton.SwipeLocking -> config.copy(
                    info = value
                )

                else -> config
            }
        }
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

        @Composable
        fun <Config : ControllerWidget> ControllerWidget.Property<Config, *>.controller() = controller(
            modifier = Modifier.fillMaxWidth(),
            config = config,
            context = context,
            onConfigChanged = onConfigChanged,
        )

        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(4),
        ) {
            Text(Text.translatable(Texts.WIDGET_DPAD_PROPERTY_EXTRA_BUTTON))

            var expanded by remember { mutableStateOf(false) }
            Select(
                modifier = Modifier.fillMaxWidth(),
                expanded = expanded,
                onExpandedChanged = { expanded = it },
                dropDownContent = {
                    DropdownItemList(
                        modifier = Modifier.verticalScroll(),
                        items = DPadExtraButton.Type.entries,
                        textProvider = { Text.translatable(it.nameId) },
                        selectedIndex = DPadExtraButton.Type.entries.indexOf(value.type),
                        onItemSelected = { item, _ ->
                            if (value.type != item) {
                                onConfigChanged(
                                    setValue(
                                        widgetConfig, when (item) {
                                            DPadExtraButton.Type.NONE -> DPadExtraButton.None
                                            DPadExtraButton.Type.NORMAL -> DPadExtraButton.Normal()
                                            DPadExtraButton.Type.SWIPE -> DPadExtraButton.Swipe()
                                            DPadExtraButton.Type.SWIPE_LOCKING -> DPadExtraButton.SwipeLocking()
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

            when (value) {
                DPadExtraButton.None -> {}
                is DPadExtraButton.Normal -> {
                    normalTriggerProperty.controller()
                    normalButtonInfoProperty.controller()
                }

                is DPadExtraButton.Swipe -> {
                    swipeTriggerProperty.controller()
                    swipeButtonInfoProperty.controller()
                }

                is DPadExtraButton.SwipeLocking -> {
                    swipeLockingTriggerProperty.controller()
                    swipeLockingButtonInfoProperty.controller()
                }
            }
        }
    }
}
