package top.fifthlight.touchcontroller.common.ui.component

import androidx.compose.runtime.*
import top.fifthlight.combine.data.Text
import top.fifthlight.combine.layout.Alignment
import top.fifthlight.combine.layout.Arrangement
import top.fifthlight.combine.layout.Layout
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.ParentDataModifierNode
import top.fifthlight.combine.modifier.drawing.background
import top.fifthlight.combine.modifier.drawing.border
import top.fifthlight.combine.modifier.placement.fillMaxHeight
import top.fifthlight.combine.modifier.placement.fillMaxSize
import top.fifthlight.combine.modifier.placement.fillMaxWidth
import top.fifthlight.combine.modifier.placement.padding
import top.fifthlight.combine.modifier.scroll.verticalScroll
import top.fifthlight.combine.node.LocalScreenSize
import top.fifthlight.combine.widget.layout.Column
import top.fifthlight.combine.widget.layout.Row
import top.fifthlight.combine.widget.ui.*
import top.fifthlight.data.IntSize
import top.fifthlight.touchcontroller.assets.Texts
import top.fifthlight.touchcontroller.assets.TextureSet
import top.fifthlight.touchcontroller.common.config.layout.ControllerLayout
import top.fifthlight.touchcontroller.common.config.preset.builtin.key.BuiltinPresetKey
import top.fifthlight.touchcontroller.common.control.ControllerWidget
import top.fifthlight.touchcontroller.common.layout.data.ContextInput
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme
import kotlin.math.min

private data class ControllerWidgetModifierNode(
    val widget: ControllerWidget,
) : ParentDataModifierNode, Modifier.Node<ControllerWidgetModifierNode> {
    override fun modifierParentData(parentData: Any?): ControllerWidget = widget
}

@Composable
private fun PresetPreview(
    modifier: Modifier = Modifier,
    preset: ControllerLayout = ControllerLayout(),
    minimumLogicalSize: IntSize = IntSize(480, 270),
) {
    var scale by remember { mutableStateOf<Float?>(null) }
    Layout(
        modifier = modifier,
        measurePolicy = { measurables, constraints ->
            val size = IntSize(constraints.maxWidth, constraints.maxHeight)
            val displayScale = min(
                size.width.toFloat() / minimumLogicalSize.width.toFloat(),
                size.height.toFloat() / minimumLogicalSize.height.toFloat(),
            ).coerceAtMost(1f)
            scale = displayScale
            val logicalSize = (size.toSize() / displayScale).toIntSize()
            val childConstraint = constraints.copy(
                minWidth = 0,
                minHeight = 0,
            )
            val placeables = measurables.map {
                it.measure(childConstraint)
            }
            layout(size) {
                for ((index, placeable) in placeables.withIndex()) {
                    val measurable = measurables[index]
                    val widget = (measurable.parentData as? ControllerWidget)
                        ?: error("Bad parent data: ${measurable.parentData}")
                    val offset =
                        widget.align.alignOffset(logicalSize, widget.size(), widget.offset).toOffset() * displayScale
                    placeable.placeAt(offset.toIntOffset())
                }
            }
        }
    ) {
        val currentScale = scale
        if (currentScale == null) {
            return@Layout
        }
        for (layer in preset.layers) {
            if (!layer.conditions.check(ContextInput.EMPTY)) {
                continue
            }
            for (widget in layer.widgets) {
                top.fifthlight.touchcontroller.common.ui.control.ControllerWidget(
                    modifier = Modifier.then(ControllerWidgetModifierNode(widget)),
                    widget = widget,
                    scale = currentScale,
                )
            }
        }
    }
}

@Composable
fun BuiltInPresetKeySelector(
    modifier: Modifier = Modifier,
    value: BuiltinPresetKey,
    onValueChanged: (BuiltinPresetKey) -> Unit,
) {
    @Composable
    fun StyleBox(
        modifier: Modifier = Modifier,
        itemModifier: Modifier = Modifier,
    ) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(4),
        ) {
            Text(Text.translatable(Texts.SCREEN_MANAGE_CONTROL_PRESET_TEXTURE_STYLE))
            RadioColumn {
                for (textureSet in TextureSet.TextureSetKey.entries) {
                    RadioBoxItem(
                        modifier = itemModifier,
                        value = value.textureSet == textureSet,
                        onValueChanged = {
                            onValueChanged(value.copy(textureSet = textureSet))
                        },
                    ) {
                        Text(Text.translatable(textureSet.titleText))
                    }
                }
            }
        }
    }

    @Composable
    fun OptionBox(modifier: Modifier = Modifier) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(4),
        ) {
            Text(
                Text.format(
                    Texts.SCREEN_CONFIG_PERCENT,
                    Text.translatable(Texts.SCREEN_MANAGE_CONTROL_PRESET_OPACITY),
                    (value.opacity * 100).toInt().toString()
                )
            )
            Slider(
                modifier = Modifier.fillMaxWidth(),
                range = 0f..1f,
                value = value.opacity,
                onValueChanged = {
                    onValueChanged(value.copy(opacity = it))
                },
            )

            Text(
                Text.format(
                    Texts.SCREEN_CONFIG_PERCENT,
                    Text.translatable(Texts.SCREEN_MANAGE_CONTROL_PRESET_SCALE),
                    (value.scale * 100).toInt().toString()
                )
            )
            Slider(
                modifier = Modifier.fillMaxWidth(),
                range = .5f..4f,
                value = value.scale,
                onValueChanged = {
                    onValueChanged(value.copy(scale = it))
                },
            )
        }
    }

    Row(
        modifier = Modifier
            .background(LocalTouchControllerTheme.current.background)
            .then(modifier)
    ) {
        Column(
            modifier = Modifier
                .weight(6f)
                .fillMaxHeight(),
        ) {
            PresetPreview(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                preset = value.preset.layout,
            )
            if (LocalScreenSize.current.width > 600) {
                Row(
                    modifier = Modifier
                        .padding(4)
                        .border(LocalTouchControllerTheme.current.borderBackgroundDark)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4),
                ) {
                    StyleBox()
                    OptionBox(Modifier.weight(1f))
                }
            } else {
                OptionBox(
                    modifier = Modifier
                        .padding(4)
                        .border(LocalTouchControllerTheme.current.borderBackgroundDark)
                        .fillMaxWidth(),
                )
            }
        }
        Column(
            modifier = Modifier
                .padding(4)
                .weight(4f)
                .verticalScroll()
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(4),
        ) {
            if (LocalScreenSize.current.width < 600) {
                StyleBox(
                    modifier = Modifier.fillMaxWidth(),
                    itemModifier = Modifier.fillMaxWidth(),
                )
            }

            Text(Text.translatable(Texts.SCREEN_MANAGE_CONTROL_PRESET_CONTROL_STYLE))
            RadioColumn(modifier = Modifier.fillMaxWidth()) {
                RadioBoxItem(
                    value = value.controlStyle == BuiltinPresetKey.ControlStyle.TouchGesture,
                    onValueChanged = {
                        if (value.controlStyle != BuiltinPresetKey.ControlStyle.TouchGesture) {
                            onValueChanged(value.copy(controlStyle = BuiltinPresetKey.ControlStyle.TouchGesture))
                        }
                    }
                ) {
                    Text(Text.translatable(Texts.SCREEN_MANAGE_CONTROL_PRESET_CONTROL_STYLE_CLICK_TO_INTERACT))
                }

                RadioBoxItem(
                    value = value.controlStyle is BuiltinPresetKey.ControlStyle.SplitControls,
                    onValueChanged = {
                        if (value.controlStyle !is BuiltinPresetKey.ControlStyle.SplitControls) {
                            onValueChanged(value.copy(controlStyle = BuiltinPresetKey.ControlStyle.SplitControls()))
                        }
                    }
                ) {
                    Text(Text.translatable(Texts.SCREEN_MANAGE_CONTROL_PRESET_CONTROL_STYLE_AIMING_BY_CROSSHAIR))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = Text.translatable(Texts.SCREEN_MANAGE_CONTROL_PRESET_ATTACK_AND_INTERACT_BY_BUTTON),
                )

                val controlStyle = value.controlStyle
                Switch(
                    enabled = controlStyle is BuiltinPresetKey.ControlStyle.SplitControls,
                    value = (controlStyle as? BuiltinPresetKey.ControlStyle.SplitControls)?.buttonInteraction == true,
                    onValueChanged = {
                        if (controlStyle is BuiltinPresetKey.ControlStyle.SplitControls) {
                            onValueChanged(value.copy(controlStyle = controlStyle.copy(buttonInteraction = it)))
                        }
                    },
                )
            }

            Text(Text.translatable(Texts.SCREEN_MANAGE_CONTROL_PRESET_MOVE_METHOD))
            RadioColumn(modifier = Modifier.fillMaxWidth()) {
                RadioBoxItem(
                    value = value.moveMethod is BuiltinPresetKey.MoveMethod.Dpad,
                    onValueChanged = {
                        if (value.moveMethod !is BuiltinPresetKey.MoveMethod.Dpad) {
                            onValueChanged(value.copy(moveMethod = BuiltinPresetKey.MoveMethod.Dpad()))
                        }
                    }
                ) {
                    Text(Text.translatable(Texts.SCREEN_MANAGE_CONTROL_PRESET_MOVE_METHOD_DPAD))
                }

                RadioBoxItem(
                    value = value.moveMethod is BuiltinPresetKey.MoveMethod.Joystick,
                    onValueChanged = {
                        if (value.moveMethod !is BuiltinPresetKey.MoveMethod.Joystick) {
                            onValueChanged(value.copy(moveMethod = BuiltinPresetKey.MoveMethod.Joystick()))
                        }
                    }
                ) {
                    Text(Text.translatable(Texts.SCREEN_MANAGE_CONTROL_PRESET_MOVE_METHOD_JOYSTICK))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = Text.translatable(Texts.SCREEN_MANAGE_CONTROL_PRESET_SPRINT_USING_JOYSTICK),
                )

                val moveMethod = value.moveMethod
                Switch(
                    enabled = moveMethod is BuiltinPresetKey.MoveMethod.Joystick,
                    value = (moveMethod as? BuiltinPresetKey.MoveMethod.Joystick)?.triggerSprint == true,
                    onValueChanged = {
                        if (moveMethod is BuiltinPresetKey.MoveMethod.Joystick) {
                            onValueChanged(value.copy(moveMethod = moveMethod.copy(triggerSprint = it)))
                        }
                    },
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = Text.translatable(Texts.SCREEN_MANAGE_CONTROL_PRESET_SWAP_JUMP_AND_SNEAK),
                )

                val moveMethod = value.moveMethod
                Switch(
                    enabled = moveMethod is BuiltinPresetKey.MoveMethod.Dpad && (value.controlStyle as? BuiltinPresetKey.ControlStyle.SplitControls)?.buttonInteraction != true,
                    value = (moveMethod as? BuiltinPresetKey.MoveMethod.Dpad)?.swapJumpAndSneak == true,
                    onValueChanged = {
                        if (moveMethod is BuiltinPresetKey.MoveMethod.Dpad) {
                            onValueChanged(value.copy(moveMethod = moveMethod.copy(swapJumpAndSneak = it)))
                        }
                    },
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = Text.translatable(Texts.SCREEN_MANAGE_CONTROL_PRESET_SPRINT),
                )
                var expanded by remember { mutableStateOf(false) }
                Select(
                    expanded = expanded,
                    onExpandedChanged = { expanded = it },
                    dropDownContent = {
                        val selectedIndex =
                            BuiltinPresetKey.SprintButtonLocation.entries.indexOf(value.sprintButtonLocation)
                        DropdownItemList(
                            modifier = Modifier.verticalScroll(),
                            items = BuiltinPresetKey.SprintButtonLocation.entries,
                            textProvider = { Text.translatable(it.nameId) },
                            selectedIndex = selectedIndex,
                            onItemSelected = { index ->
                                expanded = false
                                onValueChanged(value.copy(sprintButtonLocation = BuiltinPresetKey.SprintButtonLocation.entries[index]))
                            }
                        )
                    }
                ) {
                    Text(Text.translatable(value.sprintButtonLocation.nameId))
                    SelectIcon(expanded = expanded)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = Text.translatable(Texts.SCREEN_MANAGE_CONTROL_PRESET_USE_VANILLA_CHAT),
                )
                Switch(
                    value = value.useVanillaChat,
                    onValueChanged = {
                        onValueChanged(value.copy(useVanillaChat = it))
                    },
                )
            }
        }
    }
}
