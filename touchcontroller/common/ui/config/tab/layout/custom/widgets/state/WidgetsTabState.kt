/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.widgets.state

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import top.fifthlight.touchcontroller.assets.texture.set.BuiltInTextureSets
import top.fifthlight.touchcontroller.common.assets.TextureSet
import top.fifthlight.touchcontroller.common.control.ControllerWidget
import top.fifthlight.touchcontroller.common.control.builtin.BuiltinWidgets

data class WidgetsTabState(
    val listContent: ListContent,
    val tabState: TabState = TabState(),
) {
    data class TabState(
        val listState: ListState = ListState.BUILTIN,
        val dialogState: DialogState = DialogState.Empty,
        val newWidgetParams: NewWidgetParams = NewWidgetParams(),
    )

    data class NewWidgetParams(
        val opacity: Float = .6f,
        val textureSet: TextureSet = BuiltInTextureSets.classic,
    )

    sealed class DialogState {
        data object Empty : DialogState()

        data class ChangeNewWidgetParams(
            val opacity: Float = .6f,
            val textureSet: TextureSet = BuiltInTextureSets.classic,
        ) : DialogState() {
            constructor(params: NewWidgetParams) : this(opacity = params.opacity, textureSet = params.textureSet)

            fun toParams() = NewWidgetParams(
                opacity = opacity,
                textureSet = textureSet,
            )
        }

        data class RenameWidgetPresetItem(
            val index: Int,
            val widget: ControllerWidget,
            val name: ControllerWidget.Name = widget.name,
        ) : DialogState()
    }

    enum class ListState {
        BUILTIN,
        CUSTOM
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
}
