/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.layers.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.*
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.common.config.layout.LayoutLayer
import top.fifthlight.touchcontroller.common.config.preset.LayoutPreset
import top.fifthlight.touchcontroller.common.config.preset.info.LayerCustomConditions
import top.fifthlight.touchcontroller.common.ui.layer.component.LayerEditorComponent
import java.util.*
import kotlin.jvm.optionals.getOrNull

class LayersTabComponent(
    componentContext: ComponentContext,
    val selectedPreset: Value<Optional<LayoutPreset>>,
    val selectedLayerIndex: Value<Int>,
    private val onLayerSelected: (Int) -> Unit,
    private val onLayerCreated: (LayoutLayer) -> Unit,
    private val onLayerEdited: (Int, LayoutLayer) -> Unit,
    private val onLayerCopied: (LayoutLayer) -> Unit,
    private val onLayerDeleted: (Int) -> Unit,
    private val onLayerMoved: (Int, Int) -> Unit,
    private val onCustomConditionsChanged: (LayerCustomConditions) -> Unit,
) : ComponentContext by componentContext {
    sealed interface StackConfig {
        data object List : StackConfig

        data object Create : StackConfig

        data class Edit(val index: Int) : StackConfig
    }

    sealed interface StackComponent {
        data object List : StackComponent

        data class Editor(val component: LayerEditorComponent) : StackComponent
    }

    sealed interface DialogConfig {
        data class Delete(val index: Int) : DialogConfig
    }

    sealed interface DialogComponent {
        data class Delete(
            val layerName: String,
            val onDeleted: () -> Unit,
        ) : DialogComponent
    }

    private val stackNavigation = StackNavigation<StackConfig>()

    val childStack: Value<ChildStack<StackConfig, StackComponent>> = childStack(
        source = stackNavigation,
        serializer = null,
        initialStack = { listOf(StackConfig.List) },
        handleBackButton = true,
        childFactory = { config, context ->
            when (config) {
                StackConfig.List -> StackComponent.List

                StackConfig.Create -> StackComponent.Editor(
                    LayerEditorComponent(
                        componentContext = context,
                        screenName = Texts.SCREEN_LAYER_EDITOR_CREATE,
                        preset = selectedPreset,
                        initialValue = LayoutLayer(),
                        onCustomConditionsChanged = onCustomConditionsChanged,
                        onValueChanged = onLayerCreated,
                        onBack = { stackNavigation.pop() },
                    )
                )

                is StackConfig.Edit -> {
                    val layer = selectedPreset.value.getOrNull()?.layout?.getOrNull(config.index)
                    if (layer == null) {
                        StackComponent.List
                    } else {
                        StackComponent.Editor(
                            LayerEditorComponent(
                                componentContext = context,
                                screenName = Texts.SCREEN_LAYER_EDITOR_EDIT,
                                preset = selectedPreset,
                                initialValue = layer,
                                onCustomConditionsChanged = onCustomConditionsChanged,
                                onValueChanged = { onLayerEdited(config.index, it) },
                                onBack = { stackNavigation.pop() },
                            )
                        )
                    }
                }
            }
        },
    )

    private val dialogNavigation = SlotNavigation<DialogConfig>()

    val dialog: Value<ChildSlot<DialogConfig, DialogComponent>> = childSlot(
        source = dialogNavigation,
        serializer = null,
        handleBackButton = true,
        initialConfiguration = { null },
        childFactory = { config, _ ->
            when (config) {
                is DialogConfig.Delete -> DialogComponent.Delete(
                    layerName = selectedPreset.value.getOrNull()
                        ?.layout?.getOrNull(config.index)?.name
                        ?.asString() ?: "",
                    onDeleted = { onLayerDeleted(config.index) },
                )
            }
        },
    )

    fun selectLayer(index: Int) {
        onLayerSelected(index)
    }

    fun openCreateLayerEditor() {
        stackNavigation.pushNew(StackConfig.Create)
    }

    fun openEditLayerEditor(index: Int) {
        stackNavigation.pushNew(StackConfig.Edit(index))
    }

    fun openDeleteLayerDialog(index: Int) {
        dialogNavigation.activate(DialogConfig.Delete(index))
    }

    fun copyLayer(layer: LayoutLayer) {
        onLayerCopied(layer)
    }

    fun moveLayer(index: Int, offset: Int) {
        onLayerMoved(index, offset)
    }

    fun closeDialog() {
        dialogNavigation.dismiss()
    }
}
