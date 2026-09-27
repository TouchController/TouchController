/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.control.property.trigger

import androidx.compose.runtime.*
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.layout.Alignment
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.placement.fillMaxHeight
import top.fifthlight.combine.core.modifier.placement.fillMaxWidth
import top.fifthlight.combine.core.modifier.placement.padding
import top.fifthlight.combine.core.modifier.placement.width
import top.fifthlight.combine.core.modifier.scroll.verticalScroll
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.core.widget.layout.Row
import top.fifthlight.combine.theme.blackstone.widget.AlertDialog
import top.fifthlight.combine.theme.blackstone.widget.ListButton
import top.fifthlight.combine.theme.blackstone.widget.NavigationButton
import top.fifthlight.combine.theme.blackstone.widget.RadioRow
import top.fifthlight.combine.widget.*
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.assets.texture.Textures
import top.fifthlight.touchcontroller.common.control.ControllerWidget
import top.fifthlight.touchcontroller.common.control.action.ButtonTrigger
import top.fifthlight.touchcontroller.common.control.action.GameActions
import top.fifthlight.touchcontroller.common.control.action.PlayerActions
import top.fifthlight.touchcontroller.common.control.action.WidgetTriggerAction
import top.fifthlight.touchcontroller.common.gal.key.KeyBindingHandler
import top.fifthlight.touchcontroller.common.gal.key.KeyBindingHandlerFactory

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
                actions = {
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
                            NavigationButton(
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
                                        onConfigChanged(
                                            setValue(
                                                config,
                                                WidgetTriggerAction.Game(GameActions.gameMenu)
                                            )
                                        )
                                    }
                                },
                                Pair(Text.translatable(WidgetTriggerAction.Type.PLAYER.nameId)) {
                                    if (value !is WidgetTriggerAction.Player) {
                                        onConfigChanged(
                                            setValue(
                                                config,
                                                WidgetTriggerAction.Player(PlayerActions.startSprint)
                                            )
                                        )
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
                                        if (action.hidden) return@mapNotNull null
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
                                    items = PlayerActions.registry.mapNotNull { action ->
                                        if (action.hidden) return@mapNotNull null
                                        Pair(action.name) {
                                            onConfigChanged(setValue(config, WidgetTriggerAction.Player(action)))
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
