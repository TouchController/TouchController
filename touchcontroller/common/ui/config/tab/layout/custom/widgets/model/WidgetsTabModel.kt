/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.widgets.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import top.fifthlight.touchcontroller.common.config.widget.WidgetPresetManager
import top.fifthlight.touchcontroller.common.control.ControllerWidget
import top.fifthlight.touchcontroller.common.control.builtin.BuiltinWidgets
import top.fifthlight.touchcontroller.common.ext.combineStates
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.model.CustomControlLayoutTabModel
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.widgets.state.WidgetsTabState
import top.fifthlight.touchcontroller.common.ui.model.TouchControllerScreenModel

class WidgetsTabModel(
    private val screenModel: CustomControlLayoutTabModel
) : TouchControllerScreenModel() {
    private val tabState = MutableStateFlow(WidgetsTabState.TabState())
    val uiState = combineStates(tabState, WidgetPresetManager.presets) { tabState, presets ->
        WidgetsTabState(
            listContent = when (tabState.listState) {
                WidgetsTabState.ListState.BUILTIN -> WidgetsTabState.ListContent.BuiltIn(textureSet = tabState.newWidgetParams.textureSet)
                WidgetsTabState.ListState.CUSTOM -> WidgetsTabState.ListContent.Custom(widgets = presets)
            },
            tabState = tabState,
        )
    }

    fun selectBuiltinTab() {
        tabState.getAndUpdate {
            it.copy(listState = WidgetsTabState.ListState.BUILTIN)
        }
    }

    fun selectCustomTab() {
        tabState.getAndUpdate {
            it.copy(listState = WidgetsTabState.ListState.CUSTOM)
        }
    }

    fun openNewWidgetParamsDialog() {
        tabState.getAndUpdate {
            it.copy(dialogState = WidgetsTabState.DialogState.ChangeNewWidgetParams(it.newWidgetParams))
        }
    }

    fun updateNewWidgetParamsDialog(editor: WidgetsTabState.DialogState.ChangeNewWidgetParams.() -> WidgetsTabState.DialogState.ChangeNewWidgetParams) {
        tabState.getAndUpdate {
            var params = it.dialogState
            if (params is WidgetsTabState.DialogState.ChangeNewWidgetParams) {
                params = editor(params)
            }
            it.copy(dialogState = params)
        }
    }

    fun openRenameWidgetPresetItemDialog(index: Int, widget: ControllerWidget) {
        tabState.getAndUpdate {
            it.copy(
                dialogState = WidgetsTabState.DialogState.RenameWidgetPresetItem(
                    index = index,
                    widget = widget,
                )
            )
        }
    }

    fun updateRenameWidgetPresetItemDialog(newName: String) {
        tabState.getAndUpdate {
            var params = it.dialogState
            if (params is WidgetsTabState.DialogState.RenameWidgetPresetItem) {
                params = params.copy(name = ControllerWidget.Name.Literal(newName))
            }
            it.copy(dialogState = params)
        }
    }

    fun closeDialog() {
        tabState.getAndUpdate {
            it.copy(dialogState = WidgetsTabState.DialogState.Empty)
        }
    }

    fun updateNewWidgetParams(params: WidgetsTabState.NewWidgetParams) {
        tabState.getAndUpdate {
            it.copy(newWidgetParams = params)
        }
    }

    fun renameWidgetPresetItem(index: Int, newName: ControllerWidget.Name) {
        val presets = WidgetPresetManager.presets.value
        val widget = presets[index].cloneBase(name = newName)
        WidgetPresetManager.save(presets.set(index, widget))
    }

    fun deleteWidgetPresetItem(index: Int) {
        val presets = WidgetPresetManager.presets.value
        WidgetPresetManager.save(presets.removeAt(index))
    }
}
