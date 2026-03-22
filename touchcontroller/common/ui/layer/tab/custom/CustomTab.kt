package top.fifthlight.touchcontroller.common.ui.layer.tab.custom

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.model.rememberScreenModel
import top.fifthlight.combine.data.Identifier
import top.fifthlight.combine.data.Text
import top.fifthlight.combine.layout.Alignment
import top.fifthlight.combine.layout.Arrangement
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.placement.*
import top.fifthlight.combine.modifier.scroll.verticalScroll
import top.fifthlight.combine.widget.layout.Column
import top.fifthlight.combine.widget.layout.Row
import top.fifthlight.combine.widget.ui.*
import top.fifthlight.touchcontroller.assets.Texts
import top.fifthlight.touchcontroller.assets.Textures
import top.fifthlight.touchcontroller.common.config.condition.CustomLayerConditionKey
import top.fifthlight.touchcontroller.common.ui.layer.tab.LayerConditionTab
import top.fifthlight.touchcontroller.common.ui.layer.tab.LocalLayerConditionTabContext
import top.fifthlight.touchcontroller.common.ui.widget.ListButton
import top.fifthlight.touchcontroller.common.ui.layer.tab.custom.model.CustomTabModel

object CustomTab : LayerConditionTab() {
    @Composable
    override fun Icon() {
        Icon(Textures.icon_edit)
    }

    override val name: Identifier
        get() = Texts.SCREEN_LAYER_EDITOR_CUSTOM_CONDITION

    @Composable
    override fun Content() {
        val layerConditionTabContext = LocalLayerConditionTabContext.current
        val tabModel = rememberScreenModel { CustomTabModel(layerConditionTabContext) }
        val preset = layerConditionTabContext.preset
        val onConditionAdded = layerConditionTabContext.onConditionAdded
        val tabState by tabModel.uiState.collectAsState()

        AlertDialog(
            modifier = Modifier.fillMaxWidth(.4f),
            value = tabState,
            valueTransformer = { it.editState },
            title = {
                Text(Text.translatable(Texts.SCREEN_LAYER_EDITOR_CUSTOM_CONDITION_EDIT))
            },
            action = {
                val editState = tabState.editState
                GuideButton(
                    onClick = {
                        editState?.let { editState ->
                            preset?.let { preset ->
                                tabModel.editCondition(
                                    preset = preset,
                                    index = editState.index,
                                    newCondition = editState.edit(
                                        preset.controlInfo.customConditions.conditions[editState.index]
                                    ),
                                )
                            }
                            tabModel.closeEditConditionDialog()
                        }
                    },
                ) {
                    Text(Text.translatable(Texts.SCREEN_LAYER_EDITOR_CUSTOM_CONDITION_EDIT_OK))
                }
                Button(
                    onClick = {
                        tabModel.closeEditConditionDialog()
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
                    tabModel.updateEditState {
                        copy(name = it)
                    }
                },
                placeholder = Text.translatable(Texts.SCREEN_LAYER_EDITOR_CUSTOM_CONDITION_NAME_PLACEHOLDER),
            )
        }

        Column(
            modifier = Modifier
                .padding(4),
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
                                    onConditionAdded(CustomLayerConditionKey(condition.uuid))
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
                                    tabModel.openEditConditionDialog(index, condition)
                                },
                            ) {
                                Icon(Textures.icon_edit)
                            }
                            IconButton(
                                modifier = Modifier.fillMaxHeight(),
                                onClick = {
                                    tabModel.removeCondition(preset, index)
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
                    preset?.let { tabModel.addCondition(it) }
                },
            ) {
                Text(Text.translatable(Texts.SCREEN_LAYER_EDITOR_CUSTOM_CONDITION_ADD))
            }
        }
    }
}
