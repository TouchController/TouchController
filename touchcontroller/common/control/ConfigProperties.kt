/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.control

import androidx.compose.runtime.*
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import top.fifthlight.combine.data.Text
import top.fifthlight.combine.data.TextFactory
import top.fifthlight.combine.layout.Alignment
import top.fifthlight.combine.layout.Arrangement
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.drawing.background
import top.fifthlight.combine.modifier.drawing.innerLine
import top.fifthlight.combine.modifier.placement.*
import top.fifthlight.combine.modifier.pointer.clickable
import top.fifthlight.combine.modifier.scroll.verticalScroll
import top.fifthlight.combine.paint.Color
import top.fifthlight.combine.paint.Colors
import top.fifthlight.combine.paint.Texture
import top.fifthlight.combine.widget.layout.Column
import top.fifthlight.combine.widget.layout.FlowRow
import top.fifthlight.combine.widget.layout.Row
import top.fifthlight.combine.widget.layout.Spacer
import top.fifthlight.combine.widget.ui.*
import top.fifthlight.data.IntOffset
import top.fifthlight.data.IntPadding
import top.fifthlight.data.IntSize
import top.fifthlight.touchcontroller.assets.Texts
import top.fifthlight.touchcontroller.assets.TextureSet
import top.fifthlight.touchcontroller.assets.Textures
import top.fifthlight.touchcontroller.common.control.action.ButtonTrigger
import top.fifthlight.touchcontroller.common.control.action.GameActions
import top.fifthlight.touchcontroller.common.control.action.WidgetTriggerAction
import top.fifthlight.touchcontroller.common.control.property.TextureCoordinate
import top.fifthlight.touchcontroller.common.gal.key.KeyBindingHandler
import top.fifthlight.touchcontroller.common.gal.key.KeyBindingHandlerFactory
import top.fifthlight.touchcontroller.common.layout.align.Align
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme
import top.fifthlight.touchcontroller.common.ui.widget.*
import top.fifthlight.touchcontroller.common.ui.widget.navigation.AppBar
import top.fifthlight.touchcontroller.common.ui.widget.navigation.BackButton

fun <Config : ControllerWidget, Value> ControllerWidget.Property<Config, Value>.paddingProperty(
    getPadding: (Value) -> IntPadding?,
    setPadding: (Value, IntPadding) -> Value,
    name: Text,
) = IntPaddingProperty<Config>(
    getValue = { getPadding(getValue(it)) ?: IntPadding.ZERO },
    setValue = { config, value -> setValue(config, setPadding(getValue(config), value)) },
    name = name,
)

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

fun <Config : ControllerWidget, Value> ControllerWidget.Property<Config, Value>.colorProperty(
    getColor: (Value) -> Color?,
    setColor: (Value, Color) -> Value,
    name: Text,
) = ColorProperty<Config>(
    getValue = { getColor(getValue(it)) ?: Colors.BLACK },
    setValue = { config, value -> setValue(config, setColor(getValue(config), value)) },
    name = name,
)

fun <Config : ControllerWidget, Value> ControllerWidget.Property<Config, Value>.textureCoordinateProperty(
    getCoordinate: (Value) -> TextureCoordinate?,
    setCoordinate: (Value, TextureCoordinate) -> Value,
    name: Text,
) = TextureCoordinateProperty<Config>(
    getValue = { getCoordinate(getValue(it)) ?: TextureCoordinate() },
    setValue = { config, value -> setValue(config, setCoordinate(getValue(config), value)) },
    name = name,
)

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

fun <Config : ControllerWidget, Value, T> ControllerWidget.Property<Config, Value>.enumProperty(
    getEnum: (Value) -> T?,
    setEnum: (Value, T) -> Value,
    defaultValue: T,
    name: Text,
    items: PersistentList<Pair<T, Text>>,
) = EnumProperty<Config, T>(
    getValue = { getEnum(getValue(it)) ?: defaultValue },
    setValue = { config, value -> setValue(config, setEnum(getValue(config), value)) },
    name = name,
    items = items,
)

fun <Config : ControllerWidget, Value> ControllerWidget.Property<Config, Value>.triggerActionProperty(
    getAction: (Value) -> WidgetTriggerAction?,
    setAction: (Value, WidgetTriggerAction?) -> Value,
    name: Text,
) = TriggerActionProperty<Config>(
    getValue = { getAction(getValue(it)) },
    setValue = { config, value -> setValue(config, setAction(getValue(config), value)) },
    name = name,
)

fun <Config : ControllerWidget, Value> ControllerWidget.Property<Config, Value>.keyBindingProperty(
    getKeyBinding: (Value) -> String?,
    setKeyBinding: (Value, String?) -> Value,
    name: Text,
) = KeyBindingProperty<Config>(
    getValue = { getKeyBinding(getValue(it)) },
    setValue = { config, value -> setValue(config, setKeyBinding(getValue(config), value)) },
    name = name,
)

fun <Config : ControllerWidget, Value> ControllerWidget.Property<Config, Value>.doubleClickActionProperty(
    getAction: (Value) -> ButtonTrigger.DoubleClickTrigger,
    setAction: (Value, ButtonTrigger.DoubleClickTrigger) -> Value,
    name: Text,
) = DoubleClickTriggerProperty<Config>(
    getValue = { getAction(getValue(it)) },
    setValue = { config, value -> setValue(config, setAction(getValue(config), value)) },
    name = name,
)

fun <Config : ControllerWidget, Value> ControllerWidget.Property<Config, Value>.triggerProperty(
    getTrigger: (Value) -> ButtonTrigger?,
    setTrigger: (Value, ButtonTrigger) -> Value,
) = ButtonTriggerProperty<Config>(
    getValue = { getTrigger(getValue(it)) ?: ButtonTrigger() },
    setValue = { config, value -> setValue(config, setTrigger(getValue(config), value)) },
)

@Immutable
class NameProperty<Config : ControllerWidget>(
    getValue: (Config) -> ControllerWidget.Name,
    setValue: (Config, ControllerWidget.Name) -> Config,
    private val name: Text,
) : ControllerWidget.Property<Config, ControllerWidget.Name>(getValue, setValue) {
    @Composable
    override fun controller(
        modifier: Modifier,
        config: ControllerWidget,
        context: ConfigContext,
        onConfigChanged: (ControllerWidget) -> Unit,
    ) {
        @Suppress("UNCHECKED_CAST")
        val widgetConfig = config as Config
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(4),
        ) {
            Text(name)
            EditText(
                modifier = Modifier.fillMaxWidth(),
                value = getValue(widgetConfig).asString(),
                onValueChanged = {
                    onConfigChanged(setValue(config, ControllerWidget.Name.Literal(it)))
                }
            )
        }
    }
}

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

@Immutable
class AnchorProperty<Config : ControllerWidget> : ControllerWidget.Property<Config, Align>(
    getValue = { it.align },
    setValue = { config, value ->
        @Suppress("UNCHECKED_CAST")
        config.cloneBase(align = value) as Config
    },
) {
    @Composable
    override fun controller(
        modifier: Modifier,
        config: ControllerWidget,
        context: ConfigContext,
        onConfigChanged: (ControllerWidget) -> Unit,
    ) {
        @Composable
        fun getItemText(align: Align): Text = when (align) {
            Align.LEFT_TOP -> Text.translatable(Texts.WIDGET_GENERAL_PROPERTY_ANCHOR_TOP_LEFT)
            Align.LEFT_CENTER -> Text.translatable(Texts.WIDGET_GENERAL_PROPERTY_ANCHOR_CENTER_LEFT)
            Align.LEFT_BOTTOM -> Text.translatable(Texts.WIDGET_GENERAL_PROPERTY_ANCHOR_BOTTOM_LEFT)
            Align.CENTER_TOP -> Text.translatable(Texts.WIDGET_GENERAL_PROPERTY_ANCHOR_TOP_CENTER)
            Align.CENTER_CENTER -> Text.translatable(Texts.WIDGET_GENERAL_PROPERTY_ANCHOR_CENTER_CENTER)
            Align.CENTER_BOTTOM -> Text.translatable(Texts.WIDGET_GENERAL_PROPERTY_ANCHOR_BOTTOM_CENTER)
            Align.RIGHT_TOP -> Text.translatable(Texts.WIDGET_GENERAL_PROPERTY_ANCHOR_TOP_RIGHT)
            Align.RIGHT_CENTER -> Text.translatable(Texts.WIDGET_GENERAL_PROPERTY_ANCHOR_CENTER_RIGHT)
            Align.RIGHT_BOTTOM -> Text.translatable(Texts.WIDGET_GENERAL_PROPERTY_ANCHOR_BOTTOM_RIGHT)
        }

        @Composable
        fun getItemIcon(align: Align): Texture = when (align) {
            Align.LEFT_TOP -> Textures.icon_up_left
            Align.LEFT_CENTER -> Textures.icon_left
            Align.LEFT_BOTTOM -> Textures.icon_down_left
            Align.CENTER_TOP -> Textures.icon_up
            Align.CENTER_CENTER -> Textures.icon_middle
            Align.CENTER_BOTTOM -> Textures.icon_down
            Align.RIGHT_TOP -> Textures.icon_up_right
            Align.RIGHT_CENTER -> Textures.icon_right
            Align.RIGHT_BOTTOM -> Textures.icon_down_right
        }

        @Suppress("UNCHECKED_CAST")
        val widgetConfig = config as Config
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(4),
        ) {
            Text(Text.translatable(Texts.WIDGET_GENERAL_PROPERTY_ANCHOR_NAME))

            var expanded by remember { mutableStateOf(false) }
            Select(
                modifier = Modifier.fillMaxWidth(),
                expanded = expanded,
                onExpandedChanged = { expanded = it },
                dropDownContent = {
                    val buttonWidth = contentWidth / 3

                    @Composable
                    fun AnchorButton(
                        anchor: Align,
                    ) = IconButton(
                        minSize = IntSize(
                            width = buttonWidth,
                            height = 20,
                        ),
                        selected = config.align == anchor,
                        onClick = {
                            onConfigChanged(
                                config.cloneBase(
                                    align = anchor,
                                    offset = IntOffset.ZERO,
                                )
                            )
                        }
                    ) {
                        Icon(getItemIcon(anchor))
                    }

                    Column(
                        modifier = Modifier.width(contentWidth),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4),
                    ) {
                        FlowRow(maxColumns = 3) {
                            for (align in Align.entries) {
                                AnchorButton(align)
                            }
                        }
                    }
                }
            ) {
                Text(getItemText(widgetConfig.align))
                Spacer(modifier = Modifier.weight(1f))
                SelectIcon(expanded = expanded)
            }
        }
    }
}

@Immutable
class EnumProperty<Config : ControllerWidget, T>(
    getValue: (Config) -> T,
    setValue: (Config, T) -> Config,
    private val name: Text,
    private val items: PersistentList<Pair<T, Text>>,
) : ControllerWidget.Property<Config, T>(getValue, setValue) {
    private fun getItemText(item: T): Text =
        items.firstOrNull { it.first == item }?.second ?: Text.literal(item.toString())

    @Composable
    override fun controller(
        modifier: Modifier,
        config: ControllerWidget,
        context: ConfigContext,
        onConfigChanged: (ControllerWidget) -> Unit,
    ) {
        @Suppress("UNCHECKED_CAST")
        val widgetConfig = config as Config
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
                    val value = getValue(widgetConfig)
                    val selectedIndex = items.indexOfFirst { it.first == value }
                    DropdownItemList(
                        modifier = Modifier.verticalScroll(),
                        items = items,
                        textProvider = Pair<T, Text>::second,
                        selectedIndex = selectedIndex,
                        onItemSelected = {
                            val item = items[it].first
                            onConfigChanged(setValue(widgetConfig, item))
                            expanded = false
                        }
                    )
                }
            ) {
                Text(getItemText(getValue(widgetConfig)))
                Spacer(modifier = Modifier.weight(1f))
                SelectIcon(expanded = expanded)
            }
        }
    }
}

fun <Config : ControllerWidget> TextureSetProperty(
    getValue: (Config) -> TextureSet.TextureSetKey,
    setValue: (Config, TextureSet.TextureSetKey) -> Config,
    name: Text,
) = EnumProperty(
    getValue = getValue,
    setValue = setValue,
    items = TextureSet.TextureSetKey.entries.map {
        Pair(it, Text.translatable(it.nameText))
    }.toPersistentList(),
    name = name,
)

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
                                            items = TextureSet.TextureSetKey.entries,
                                            textProvider = { Text.translatable(it.nameText) },
                                            selectedIndex = TextureSet.TextureSetKey.entries.indexOf(value.textureSet),
                                            onItemSelected = {
                                                val item = TextureSet.TextureSetKey.entries[it]
                                                onConfigChanged(setValue(config, value.copy(textureSet = item)))
                                                expanded = false
                                            }
                                        )
                                    }
                                ) {
                                    Text(Text.translatable(value.textureSet.nameText))
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
                                for (key in TextureSet.TextureKey.all) {
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
                                        val texture = remember(value.textureSet.textureSet, key) {
                                            key.get(value.textureSet.textureSet)
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

@Immutable
class KeyBindingProperty<Config : ControllerWidget>(
    getValue: (Config) -> String?,
    setValue: (Config, String?) -> Config,
    private val name: Text,
) : ControllerWidget.Property<Config, String?>(getValue, setValue) {
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

        val keyBindingHandler: KeyBindingHandler = KeyBindingHandlerFactory.of()
        val keyBindings = remember(keyBindingHandler) { keyBindingHandler.getAllStates() }
        val (keyBindingsWithCategories, keyCategories) = remember(keyBindings) {
            val keyBindingsWithCategories = keyBindings.values
                .groupBy { it.categoryId }
                .mapValues { (_, bindings) -> bindings.sortedBy { it.id } }
            val keyCategories = keyBindingsWithCategories.keys.toList().sorted()
            Pair(keyBindingsWithCategories, keyCategories)
        }

        val keyBinding = remember(value, keyBindings) { value?.let { keyBindings[it] } }

        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(4),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = name,
            )

            if (value == null) {
                Text(Text.translatable(Texts.WIDGET_KEY_BINDING_EMPTY))
            } else {
                Text(text = keyBinding?.name ?: Text.translatable(Texts.WIDGET_KEY_BINDING_UNKNOWN))
            }

            var showDialog by remember { mutableStateOf(false) }
            IconButton(onClick = {
                showDialog = true
            }) {
                Icon(Textures.icon_edit)
            }

            IconButton(onClick = {
                onConfigChanged(setValue(config, null))
            }) {
                Icon(Textures.icon_delete)
            }

            AlertDialog(
                visible = showDialog,
                modifier = Modifier
                    .fillMaxWidth(.6f)
                    .fillMaxHeight(.8f),
                onDismissRequest = {
                    showDialog = false
                },
                title = {
                    Text(Text.translatable(Texts.WIDGET_KEY_BINDING_SELECT_TITLE))
                },
                action = {
                    Button(onClick = {
                        showDialog = false
                    }) {
                        Text(Text.translatable(Texts.WIDGET_KEY_BINDING_SELECT_FINISH))
                    }
                },
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2),
                ) {
                    var selectedCategory by remember {
                        mutableStateOf(
                            keyBinding?.categoryId ?: keyCategories.first()
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(3f)
                            .padding(right = 3)
                            .verticalScroll(),
                    ) {
                        for (category in keyCategories) {
                            val firstKey = keyBindingsWithCategories[category]?.firstOrNull() ?: continue
                            TabButton(
                                modifier = Modifier.fillMaxWidth(),
                                checked = selectedCategory == category,
                                onClick = { selectedCategory = category }
                            ) {
                                Text(firstKey.categoryName)
                            }
                        }
                    }
                    Column(
                        modifier = Modifier
                            .weight(7f)
                            .verticalScroll(),
                    ) {
                        val categoryKeys = keyBindingsWithCategories[selectedCategory] ?: return@Column
                        for (key in categoryKeys) {
                            ListButton(
                                modifier = Modifier.fillMaxWidth(),
                                checked = value == key.id,
                                onClick = {
                                    onConfigChanged(setValue(config, key.id))
                                },
                            ) {
                                Text(key.name)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Immutable
class TriggerActionProperty<Config : ControllerWidget>(
    getValue: (Config) -> WidgetTriggerAction?,
    setValue: (Config, WidgetTriggerAction?) -> Config,
    private val name: Text,
) : ControllerWidget.Property<Config, WidgetTriggerAction?>(getValue, setValue) {
    private val keyClickBindingProperty = keyBindingProperty(
        getKeyBinding = { (it as? WidgetTriggerAction.Key.Click)?.keyBinding },
        setKeyBinding = { config, value ->
            when (config) {
                is WidgetTriggerAction.Key.Click -> config.copy(keyBinding = value)
                else -> config
            }
        },
        name = Text.translatable(Texts.WIDGET_TRIGGER_KEY_BINDING),
    )

    private val keyLockBindingProperty = keyBindingProperty(
        getKeyBinding = { (it as? WidgetTriggerAction.Key.Lock)?.keyBinding },
        setKeyBinding = { config, value ->
            when (config) {
                is WidgetTriggerAction.Key.Lock -> config.copy(keyBinding = value)
                else -> config
            }
        },
        name = Text.translatable(Texts.WIDGET_TRIGGER_KEY_BINDING),
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(name)

                var expanded by remember { mutableStateOf(false) }
                Select(
                    expanded = expanded,
                    onExpandedChanged = { expanded = it },
                    dropDownContent = {
                        DropdownItemList(
                            modifier = Modifier.verticalScroll(),
                            onItemSelected = { expanded = false },
                            items = persistentListOf(
                                Pair(Text.translatable(WidgetTriggerAction.Type.NONE.nameId)) {
                                    onConfigChanged(setValue(config, null))
                                },
                                Pair(Text.translatable(WidgetTriggerAction.Type.KEY.nameId)) {
                                    if (value !is WidgetTriggerAction.Key) {
                                        onConfigChanged(setValue(config, WidgetTriggerAction.Key.Click()))
                                    }
                                },
                                Pair(Text.translatable(WidgetTriggerAction.Type.GAME.nameId)) {
                                    if (value !is WidgetTriggerAction.Game) {
                                        onConfigChanged(setValue(config, WidgetTriggerAction.Game(GameActions.gameMenu)))
                                    }
                                },
                                Pair(Text.translatable(WidgetTriggerAction.Type.PLAYER.nameId)) {
                                    if (value !is WidgetTriggerAction.Player) {
                                        onConfigChanged(setValue(config, WidgetTriggerAction.Player.StartSprint))
                                    }
                                },
                                Pair(Text.translatable(WidgetTriggerAction.Type.LAYER_CONDITION.nameId)) {
                                    if (value !is WidgetTriggerAction.LayerCondition) {
                                        onConfigChanged(setValue(config, WidgetTriggerAction.LayerCondition.Toggle()))
                                    }
                                },
                            ),
                        )
                    }
                ) {
                    val actionType = value?.actionType ?: WidgetTriggerAction.Type.NONE
                    Text(Text.translatable(actionType.nameId))
                    SelectIcon(expanded = expanded)
                }
            }
            when (value) {
                is WidgetTriggerAction.Key -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = Text.translatable(Texts.WIDGET_TRIGGER_KEY_TYPE)
                        )
                        RadioRow {
                            RadioBoxItem(
                                value = value is WidgetTriggerAction.Key.Click,
                                onValueChanged = { checked ->
                                    if (checked && value !is WidgetTriggerAction.Key.Click) {
                                        onConfigChanged(setValue(config, WidgetTriggerAction.Key.Click()))
                                    }
                                },
                            ) {
                                Text(Text.translatable(Texts.WIDGET_TRIGGER_KEY_CLICK))
                            }
                            RadioBoxItem(
                                value = value is WidgetTriggerAction.Key.Lock,
                                onValueChanged = { checked ->
                                    if (checked && value !is WidgetTriggerAction.Key.Lock) {
                                        onConfigChanged(setValue(config, WidgetTriggerAction.Key.Lock()))
                                    }
                                },
                            ) {
                                Text(Text.translatable(Texts.WIDGET_TRIGGER_KEY_LOCK))
                            }
                        }
                    }
                    when (value) {
                        is WidgetTriggerAction.Key.Click -> {
                            keyClickBindingProperty.controller()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    modifier = Modifier.weight(1f),
                                    text = Text.translatable(Texts.WIDGET_TRIGGER_KEY_KEEP_FOR_CLIENT_TICK),
                                )
                                Switch(
                                    value = value.keepInClientTick,
                                    onValueChanged = {
                                        onConfigChanged(setValue(config, value.copy(keepInClientTick = it)))
                                    }
                                )
                            }
                        }

                        is WidgetTriggerAction.Key.Lock -> {
                            keyLockBindingProperty.controller()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    modifier = Modifier.weight(1f),
                                    text = Text.translatable(Texts.WIDGET_TRIGGER_KEY_LOCK_TYPE),
                                )
                                var expanded by remember { mutableStateOf(false) }
                                Select(
                                    expanded = expanded,
                                    onExpandedChanged = { expanded = it },
                                    dropDownContent = {
                                        DropdownItemList(
                                            modifier = Modifier.verticalScroll(),
                                            onItemSelected = { expanded = false },
                                            items = WidgetTriggerAction.Key.Lock.LockActionType.entries.map {
                                                Pair(Text.translatable(it.nameId)) {
                                                    onConfigChanged(setValue(config, value.copy(lockType = it)))
                                                }
                                            }.toPersistentList()
                                        )
                                    }
                                ) {
                                    Text(Text.translatable(value.lockType.nameId))
                                    SelectIcon(expanded = expanded)
                                }
                            }
                        }
                    }
                }

                is WidgetTriggerAction.Game -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = Text.translatable(Texts.WIDGET_TRIGGER_GAME_ACTION_TYPE),
                        )
                        var expanded by remember { mutableStateOf(false) }
                        Select(
                            expanded = expanded,
                            onExpandedChanged = { expanded = it },
                            dropDownContent = {
                                DropdownItemList(
                                    modifier = Modifier.verticalScroll(),
                                    onItemSelected = { expanded = false },
                                    items = GameActions.registry.mapNotNull { action ->
                                        if (action.hidden) return null
                                        Pair(action.name) {
                                            onConfigChanged(setValue(config, WidgetTriggerAction.Game(action)))
                                        }
                                    }.toPersistentList()
                                )
                            }
                        ) {
                            Text(value.action.name)
                            SelectIcon(expanded = expanded)
                        }
                    }
                }

                is WidgetTriggerAction.Player -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = Text.translatable(Texts.WIDGET_TRIGGER_PLAYER_ACTION_TYPE),
                        )
                        var expanded by remember { mutableStateOf(false) }
                        Select(
                            expanded = expanded,
                            onExpandedChanged = { expanded = it },
                            dropDownContent = {
                                DropdownItemList(
                                    modifier = Modifier.verticalScroll(),
                                    onItemSelected = { expanded = false },
                                    items = WidgetTriggerAction.Player.all.map {
                                        Pair(Text.translatable(it.nameId)) {
                                            onConfigChanged(setValue(config, it))
                                        }
                                    }.toPersistentList()
                                )
                            }
                        ) {
                            Text(Text.translatable(value.nameId))
                            SelectIcon(expanded = expanded)
                        }
                    }
                }

                is WidgetTriggerAction.LayerCondition -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = Text.translatable(Texts.WIDGET_TRIGGER_LAYER_CONDITION_TYPE),
                        )
                        var expanded by remember { mutableStateOf(false) }
                        Select(
                            expanded = expanded,
                            onExpandedChanged = { expanded = it },
                            dropDownContent = {
                                DropdownItemList(
                                    modifier = Modifier.verticalScroll(),
                                    onItemSelected = { expanded = false },
                                    items = persistentListOf(
                                        Pair(Text.translatable(Texts.WIDGET_TRIGGER_LAYER_CONDITION_TOGGLE)) {
                                            onConfigChanged(
                                                setValue(
                                                    config, WidgetTriggerAction.LayerCondition.Toggle(
                                                        conditionUuid = value.conditionUuid,
                                                    )
                                                )
                                            )
                                        },
                                        Pair(Text.translatable(Texts.WIDGET_TRIGGER_LAYER_CONDITION_ENABLE)) {
                                            onConfigChanged(
                                                setValue(
                                                    config, WidgetTriggerAction.LayerCondition.Enable(
                                                        conditionUuid = value.conditionUuid,
                                                    )
                                                )
                                            )
                                        },
                                        Pair(Text.translatable(Texts.WIDGET_TRIGGER_LAYER_CONDITION_DISABLE)) {
                                            onConfigChanged(
                                                setValue(
                                                    config, WidgetTriggerAction.LayerCondition.Disable(
                                                        conditionUuid = value.conditionUuid,
                                                    )
                                                )
                                            )
                                        },
                                    ),
                                )
                            }
                        ) {
                            Text(Text.translatable(value.nameId))
                            SelectIcon(expanded = expanded)
                        }
                    }
                    context.presetControlInfo?.let { presetControlInfo ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                modifier = Modifier.weight(1f),
                                text = Text.translatable(Texts.WIDGET_TRIGGER_LAYER_CONDITION_CONDITION),
                            )
                            var expanded by remember { mutableStateOf(false) }
                            Select(
                                expanded = expanded,
                                onExpandedChanged = { expanded = it },
                                dropDownContent = {
                                    DropdownItemList(
                                        modifier = Modifier.verticalScroll(),
                                        onItemSelected = { expanded = false },
                                        items = presetControlInfo.customConditions.conditions.map { condition ->
                                            val name = condition.name?.let { Text.literal(it) }
                                                ?: Text.translatable(Texts.SCREEN_LAYER_EDITOR_CUSTOM_CONDITION_UNNAMED)
                                            Pair(name) {
                                                onConfigChanged(
                                                    setValue(
                                                        config, value.clone(condition.uuid)
                                                    )
                                                )
                                            }
                                        }.toPersistentList()
                                    )
                                }
                            ) {
                                val name = if (value.conditionUuid == null) {
                                    Text.translatable(Texts.WIDGET_TRIGGER_LAYER_CONDITION_CONDITION_EMPTY)
                                } else {
                                    val condition =
                                        presetControlInfo.customConditions.conditions.firstOrNull { it.uuid == value.conditionUuid }
                                    if (condition == null) {
                                        Text.translatable(Texts.SCREEN_LAYER_EDITOR_CUSTOM_CONDITION_UNKNOWN)
                                    } else {
                                        condition.name?.let { Text.literal(it) }
                                            ?: Text.translatable(Texts.SCREEN_LAYER_EDITOR_CUSTOM_CONDITION_UNNAMED)
                                    }
                                }

                                Text(name)
                                SelectIcon(expanded = expanded)
                            }
                        }
                    }
                }

                null -> {}
            }
        }
    }
}

@Immutable
class DoubleClickTriggerProperty<Config : ControllerWidget>(
    getValue: (Config) -> ButtonTrigger.DoubleClickTrigger,
    setValue: (Config, ButtonTrigger.DoubleClickTrigger) -> Config,
    private val name: Text,
) : ControllerWidget.Property<Config, ButtonTrigger.DoubleClickTrigger>(getValue, setValue) {
    private val actionProperty = triggerActionProperty(
        getAction = { it.action },
        setAction = { config, value -> config.copy(action = value) },
        name = Text.translatable(Texts.WIDGET_DOUBLE_TRIGGER_ACTION)
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
            Text(name)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4),
            ) {
                Text(Text.translatable(Texts.WIDGET_DOUBLE_TRIGGER_INTERVAL))
                IntSlider(
                    modifier = Modifier.weight(1f),
                    range = 1..40,
                    value = value.interval,
                    onValueChanged = {
                        onConfigChanged(setValue(config, value.copy(interval = it)))
                    }
                )
                var text by remember(value.interval) { mutableStateOf(value.interval.toString()) }
                LaunchedEffect(text) {
                    val newInterval = text.toIntOrNull() ?: return@LaunchedEffect
                    if (newInterval == value.interval) {
                        return@LaunchedEffect
                    }
                    onConfigChanged(setValue(config, value.copy(interval = newInterval)))
                }
                EditText(
                    modifier = Modifier.width(48),
                    value = text,
                    onValueChanged = { text = it },
                )
            }
            actionProperty.controller()
        }
    }
}

@Immutable
class ButtonTriggerProperty<Config : ControllerWidget>(
    getValue: (Config) -> ButtonTrigger,
    setValue: (Config, ButtonTrigger) -> Config,
) : ControllerWidget.Property<Config, ButtonTrigger>(getValue, setValue) {
    private val downTriggerActionProperty = triggerActionProperty(
        getAction = { it.down },
        setAction = { config, value -> config.copy(down = value) },
        name = Text.translatable(Texts.WIDGET_TRIGGER_DOWN)
    )

    private val pressKeyBindingProperty = keyBindingProperty(
        getKeyBinding = { it.press },
        setKeyBinding = { config, value -> config.copy(press = value) },
        name = Text.translatable(Texts.WIDGET_TRIGGER_PRESS)
    )

    private val releaseTriggerActionProperty = triggerActionProperty(
        getAction = { it.release },
        setAction = { config, value -> config.copy(release = value) },
        name = Text.translatable(Texts.WIDGET_TRIGGER_RELEASE)
    )

    private val doubleClickTriggerActionProperty = doubleClickActionProperty(
        getAction = { it.doubleClick },
        setAction = { config, value -> config.copy(doubleClick = value) },
        name = Text.translatable(Texts.WIDGET_TRIGGER_DOUBLE_CLICK)
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

        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(4),
        ) {
            downTriggerActionProperty.controller()
            pressKeyBindingProperty.controller()
            releaseTriggerActionProperty.controller()
            doubleClickTriggerActionProperty.controller()
        }
    }
}
