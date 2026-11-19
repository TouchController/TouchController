/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.widgets.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.*
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import top.fifthlight.touchcontroller.assets.texture.set.BuiltInTextureSets
import top.fifthlight.touchcontroller.common.assets.TextureSet
import top.fifthlight.touchcontroller.common.control.ControllerWidget
import top.fifthlight.touchcontroller.common.control.builtin.BuiltinWidgets
import top.fifthlight.touchcontroller.common.ui.state.combineValues
import top.fifthlight.touchcontroller.common.util.name.TranslatableName
import java.util.*
import kotlin.jvm.optionals.getOrNull

class WidgetsTabComponent(
    componentContext: ComponentContext,
    private val widgetPresets: Value<PersistentList<ControllerWidget>>,
    val selectedWidget: Value<Optional<ControllerWidget>>,
    private val onWidgetPresetsChanged: (PersistentList<ControllerWidget>) -> Unit,
    private val onWidgetAdded: (ControllerWidget) -> Unit,
    private val onAddWidgetPreset: (ControllerWidget) -> Unit,
) : ComponentContext by componentContext {
    data class NewWidgetParams(
        val opacity: Float = .6f,
        val textureSet: TextureSet = BuiltInTextureSets.classic,
    )

    enum class ListState {
        BUILTIN,
        CUSTOM,
    }

    sealed class ListContent {
        data class BuiltIn(private val textureSet: TextureSet) : ListContent() {
            val heroes: PersistentList<ControllerWidget>
            val widgets: PersistentList<ControllerWidget>

            init {
                val heroes = mutableListOf<ControllerWidget>()
                val widgets = mutableListOf<ControllerWidget>()
                BuiltinWidgets.registry.forEach {
                    if (it.hidden?.invoke(textureSet) == true) {
                        return@forEach
                    }
                    val widget = it[textureSet]
                    if (it.hero) {
                        heroes.add(widget)
                    } else {
                        widgets.add(widget)
                    }
                }
                this.heroes = heroes.toPersistentList()
                this.widgets = widgets.toPersistentList()
            }
        }

        data class Custom(
            val widgets: PersistentList<ControllerWidget>,
        ) : ListContent()
    }

    data class UiState(
        val listContent: ListContent,
        val listState: ListState,
        val newWidgetParams: NewWidgetParams,
    )

    sealed interface DialogConfig {
        data object ChangeNewWidgetParams : DialogConfig

        data class RenameWidgetPresetItem(val index: Int) : DialogConfig
    }

    sealed interface DialogComponent {
        class ChangeNewWidgetParams(params: NewWidgetParams) : DialogComponent {
            val uiState = MutableValue(params)

            fun update(transform: NewWidgetParams.() -> NewWidgetParams) {
                uiState.value = uiState.value.transform()
            }
        }

        class RenameWidgetPresetItem(
            val index: Int,
            name: TranslatableName,
        ) : DialogComponent {
            val uiState = MutableValue(name)

            fun update(name: TranslatableName) {
                uiState.value = name
            }
        }
    }

    private val listState = MutableValue(ListState.BUILTIN)
    private val newWidgetParams = MutableValue(NewWidgetParams())

    val uiState: Value<UiState> = combineValues(
        listState,
        newWidgetParams,
        widgetPresets,
    ) { listState, newWidgetParams, widgetPresets ->
        UiState(
            listContent = when (listState) {
                ListState.BUILTIN -> ListContent.BuiltIn(textureSet = newWidgetParams.textureSet)
                ListState.CUSTOM -> ListContent.Custom(widgets = widgetPresets)
            },
            listState = listState,
            newWidgetParams = newWidgetParams,
        )
    }

    private val dialogNavigation = SlotNavigation<DialogConfig>()

    val dialog: Value<ChildSlot<DialogConfig, DialogComponent>> = childSlot(
        source = dialogNavigation,
        serializer = null,
        handleBackButton = true,
        initialConfiguration = { null },
        childFactory = { config, _ ->
            when (config) {
                DialogConfig.ChangeNewWidgetParams -> DialogComponent.ChangeNewWidgetParams(newWidgetParams.value)

                is DialogConfig.RenameWidgetPresetItem -> DialogComponent.RenameWidgetPresetItem(
                    index = config.index,
                    name = widgetPresets.value.getOrNull(config.index)?.name ?: TranslatableName.Literal(""),
                )
            }
        },
    )

    fun selectBuiltinTab() {
        listState.value = ListState.BUILTIN
    }

    fun selectCustomTab() {
        listState.value = ListState.CUSTOM
    }

    fun openChangeNewWidgetParamsDialog() {
        dialogNavigation.activate(DialogConfig.ChangeNewWidgetParams)
    }

    fun openRenameWidgetPresetItemDialog(index: Int) {
        dialogNavigation.activate(DialogConfig.RenameWidgetPresetItem(index))
    }

    fun closeDialog() {
        dialogNavigation.dismiss()
    }

    fun updateNewWidgetParams(params: NewWidgetParams) {
        newWidgetParams.value = params
    }

    fun addWidget(widget: ControllerWidget) {
        onWidgetAdded(widget.cloneBase(opacity = newWidgetParams.value.opacity))
    }

    fun addWidgetPreset() {
        selectedWidget.value.getOrNull()?.let(onAddWidgetPreset)
    }

    fun renameWidgetPresetItem(index: Int, name: TranslatableName) {
        val presets = widgetPresets.value
        val widget = presets.getOrNull(index) ?: return
        onWidgetPresetsChanged(presets.set(index, widget.cloneBase(name = name)))
    }

    fun deleteWidgetPresetItem(index: Int) {
        val presets = widgetPresets.value
        if (index !in presets.indices) {
            return
        }
        onWidgetPresetsChanged(presets.removeAt(index))
    }
}
