/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.layer.tab.custom

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.layout.Alignment
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.placement.*
import top.fifthlight.combine.core.modifier.scroll.verticalScroll
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.core.widget.layout.Row
import top.fifthlight.combine.theme.blackstone.widget.AlertDialog
import top.fifthlight.combine.theme.blackstone.widget.ListButton
import top.fifthlight.combine.widget.*
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.assets.texture.Textures
import top.fifthlight.touchcontroller.common.ui.layer.tab.custom.component.CustomTabComponent
import top.fifthlight.touchcontroller.common.ui.state.subscribeAsState
import kotlin.jvm.optionals.getOrNull

@Composable
fun CustomTab(component: CustomTabComponent) {
    val tabState by component.uiState.subscribeAsState()
    val presetOptional by component.preset.subscribeAsState()
    val preset = presetOptional.getOrNull()

    AlertDialog(
        modifier = Modifier.fillMaxWidth(.4f),
        value = tabState,
        valueTransformer = { it.editState },
        title = {
            Text(Text.translatable(Texts.SCREEN_LAYER_EDITOR_CUSTOM_CONDITION_EDIT))
        },
        actions = {
            val editState = tabState.editState
            GuideButton(
                onClick = {
                    if (editState != null && preset != null) {
                        component.editCondition(
                            index = editState.index,
                            newCondition = editState.edit(
                                preset.controlInfo.customConditions.conditions[editState.index]
                            ),
                        )
                    }
                    component.closeEditConditionDialog()
                },
            ) {
                Text(Text.translatable(Texts.SCREEN_LAYER_EDITOR_CUSTOM_CONDITION_EDIT_OK))
            }
            Button(
                onClick = {
                    component.closeEditConditionDialog()
                },
            ) {
                Text(Text.translatable(Texts.SCREEN_LAYER_EDITOR_CUSTOM_CONDITION_EDIT_CANCEL))
            }
        }
    ) { editState ->
        val defaultString = Text.translatable(Texts.SCREEN_LAYER_EDITOR_CUSTOM_CONDITION_UNNAMED).string
        EditText(
            modifier = Modifier.fillMaxWidth(),
            value = editState.name ?: defaultString,
            onValueChanged = {
                component.updateEditState {
                    copy(name = it)
                }
            },
            placeholder = Text.translatable(Texts.SCREEN_LAYER_EDITOR_CUSTOM_CONDITION_NAME_PLACEHOLDER),
        )
    }

    Column(
        modifier = Modifier.padding(4),
        verticalArrangement = Arrangement.spacedBy(4),
    ) {
        Column(
            modifier = Modifier
                .verticalScroll()
                .weight(1f)
                .fillMaxWidth(),
        ) {
            preset?.let { preset ->
                for ((index, condition) in preset.controlInfo.customConditions.conditions.withIndex()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                    ) {
                        ListButton(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            onClick = {
                                component.selectCondition(condition)
                            },
                        ) {
                            Text(
                                modifier = Modifier
                                    .alignment(Alignment.CenterLeft)
                                    .fillMaxWidth(),
                                text = condition.name?.let { Text.literal(it) }
                                    ?: Text.translatable(Texts.SCREEN_LAYER_EDITOR_CUSTOM_CONDITION_UNNAMED),
                            )
                        }

                        IconButton(
                            modifier = Modifier.fillMaxHeight(),
                            onClick = {
                                component.openEditConditionDialog(index, condition)
                            },
                        ) {
                            Icon(Textures.icon_edit)
                        }
                        IconButton(
                            modifier = Modifier.fillMaxHeight(),
                            onClick = {
                                component.removeCondition(index)
                            },
                        ) {
                            Icon(Textures.icon_delete)
                        }
                    }
                }
            }
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                component.addCondition()
            },
        ) {
            Text(Text.translatable(Texts.SCREEN_LAYER_EDITOR_CUSTOM_CONDITION_ADD))
        }
    }
}
