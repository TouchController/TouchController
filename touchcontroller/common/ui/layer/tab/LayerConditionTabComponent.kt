/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.layer.tab

import top.fifthlight.touchcontroller.common.config.condition.LayerConditions
import top.fifthlight.touchcontroller.common.ui.item.component.ItemChooserComponent
import top.fifthlight.touchcontroller.common.ui.layer.tab.custom.component.CustomTabComponent

sealed interface LayerConditionTabComponent {
    data class BuiltIn(
        val onConditionAdded: (LayerConditions.Key) -> Unit,
    ) : LayerConditionTabComponent

    data class HoldingItem(
        val itemChooser: ItemChooserComponent,
    ) : LayerConditionTabComponent

    data class RidingEntity(
        val onConditionAdded: (LayerConditions.Key) -> Unit,
    ) : LayerConditionTabComponent

    data class SelectingEntity(
        val onConditionAdded: (LayerConditions.Key) -> Unit,
    ) : LayerConditionTabComponent

    data class Custom(
        val component: CustomTabComponent,
    ) : LayerConditionTabComponent
}
