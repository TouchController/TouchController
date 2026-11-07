/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.layer.state

import top.fifthlight.touchcontroller.common.config.condition.LayerConditions
import top.fifthlight.touchcontroller.common.config.layout.LayoutLayer
import top.fifthlight.touchcontroller.common.util.name.TranslatableName

data class LayerEditorScreenState(
    val name: TranslatableName,
    val conditions: LayerConditions,
) {
    constructor(layer: LayoutLayer) : this(
        name = layer.name,
        conditions = layer.conditions,
    )

    fun edit(layer: LayoutLayer) = layer.copy(
        name = name,
        conditions = conditions,
    )
}
