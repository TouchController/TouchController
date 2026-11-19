/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.properties

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.layout.Alignment
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.placement.fillMaxHeight
import top.fifthlight.combine.core.modifier.placement.fillMaxSize
import top.fifthlight.combine.core.modifier.placement.fillMaxWidth
import top.fifthlight.combine.core.modifier.placement.padding
import top.fifthlight.combine.core.modifier.scroll.verticalScroll
import top.fifthlight.combine.core.widget.layout.Box
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.core.widget.layout.ColumnScope
import top.fifthlight.combine.core.widget.layout.RowScope
import top.fifthlight.combine.widget.Button
import top.fifthlight.combine.widget.Icon
import top.fifthlight.combine.widget.IconButton
import top.fifthlight.combine.widget.Text
import top.fifthlight.data.IntSize
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.assets.texture.Textures
import top.fifthlight.touchcontroller.common.config.preset.info.PresetControlInfo
import top.fifthlight.touchcontroller.common.control.ControllerWidget
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.widget.SideBarScaffold

@Composable
fun ColumnScope.PropertiesTabActions(
    moveLocked: Boolean,
    highlight: Boolean,
    presetSelected: Boolean,
    selectedWidget: ControllerWidget?,
    onMoveLockedChanged: (Boolean) -> Unit,
    onHighlightChanged: (Boolean) -> Unit,
    onAddWidgetPreset: (ControllerWidget) -> Unit,
) {
    IconButton(
        onClick = { onMoveLockedChanged(!moveLocked) },
        selected = moveLocked,
        enabled = presetSelected,
    ) {
        Icon(Textures.icon_lock)
    }

    IconButton(
        onClick = { onHighlightChanged(!highlight) },
        selected = highlight,
        enabled = presetSelected,
    ) {
        Icon(Textures.icon_light)
    }

    IconButton(
        enabled = selectedWidget != null,
        onClick = { selectedWidget?.let(onAddWidgetPreset) },
    ) {
        Icon(Textures.icon_save)
    }
}

@Composable
fun PropertiesTab(
    selectedWidget: ControllerWidget?,
    selectedWidgetIndex: Int,
    editAreaSize: State<IntSize>,
    presetControlInfo: PresetControlInfo?,
    onWidgetEdited: (Int, ControllerWidget) -> Unit,
    onWidgetCopied: (ControllerWidget) -> Unit,
    onWidgetDeleted: (Int) -> Unit,
) {
    @Composable
    fun RowScope.Actions(selectedWidget: ControllerWidget) {
        Button(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            onClick = {
                onWidgetCopied(selectedWidget)
            }
        ) {
            Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_COPY))
        }
        Button(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            onClick = {
                onWidgetCopied(selectedWidget)
                onWidgetDeleted(selectedWidgetIndex)
            }
        ) {
            Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_CUT))
        }
    }

    SideBarScaffold(
        modifier = Modifier.fillMaxSize(),
        title = {
            Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_PROPERTIES))
        },
        actions = if (selectedWidget != null) {
            { Actions(selectedWidget) }
        } else {
            null
        }
    ) {
        if (selectedWidget != null) {
            Column(
                modifier = Modifier
                    .padding(4)
                    .verticalScroll()
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4),
            ) {
                for (property in selectedWidget.properties) {
                    val editAreaSize by editAreaSize
                    property.controller(
                        modifier = Modifier.fillMaxWidth(),
                        config = selectedWidget,
                        context = ControllerWidget.Property.ConfigContext(
                            presetControlInfo = presetControlInfo,
                            editAreaSize = editAreaSize,
                        ),
                        onConfigChanged = { onWidgetEdited(selectedWidgetIndex, it) },
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                alignment = Alignment.Center,
            ) {
                Text(Text.translatable(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_NO_WIDGET_SELECTED))
            }
        }
    }
}
