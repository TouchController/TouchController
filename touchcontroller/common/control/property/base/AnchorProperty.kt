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
import top.fifthlight.combine.core.modifier.placement.fillMaxWidth
import top.fifthlight.combine.core.modifier.placement.width
import top.fifthlight.combine.core.paint.Texture
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.core.widget.layout.FlowRow
import top.fifthlight.combine.core.widget.layout.Spacer
import top.fifthlight.combine.widget.*
import top.fifthlight.data.IntOffset
import top.fifthlight.data.IntSize
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.assets.texture.Textures
import top.fifthlight.touchcontroller.common.control.ControllerWidget
import top.fifthlight.touchcontroller.common.layout.align.Align

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
                            val windowSize = context.editAreaSize
                            val size = config.size()
                            onConfigChanged(
                                config.cloneBase(
                                    align = anchor,
                                    offset = windowSize?.let { windowSize ->
                                        anchor.offsetAt(
                                            windowSize = windowSize,
                                            size = size,
                                            absolutePos = config.align.alignOffset(windowSize, size, config.offset)
                                        )
                                    } ?: IntOffset.ZERO,
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

