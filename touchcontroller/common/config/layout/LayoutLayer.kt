/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.config.layout

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.common.config.condition.LayerConditions
import top.fifthlight.touchcontroller.common.control.ControllerWidget
import top.fifthlight.touchcontroller.common.util.name.TranslatableName

data class LayoutLayer(
    val name: TranslatableName = DEFAULT_LAYER_NAME,
    val widgets: PersistentList<ControllerWidget> = persistentListOf(),
    val conditions: LayerConditions = LayerConditions(),
) {
    companion object {
        val DEFAULT_LAYER_NAME = TranslatableName.Translatable(Texts.LAYER_NAME_UNNAMED)
    }

    operator fun plus(widget: ControllerWidget?) = widget?.let {
        copy(widgets = widgets + widget.newId())
    } ?: this
}
