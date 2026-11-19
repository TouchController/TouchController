/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.layer.tab.selectedentity

import androidx.compose.runtime.Composable
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.drawing.border
import top.fifthlight.touchcontroller.common.config.condition.SelectEntityLayerConditionKey
import top.fifthlight.touchcontroller.common.ui.entitypicker.EntityPicker
import top.fifthlight.touchcontroller.common.ui.layer.tab.LayerConditionTabComponent
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme

@Composable
fun SelectingEntityTab(component: LayerConditionTabComponent.SelectingEntity) {
    EntityPicker(
        modifier = Modifier.border(LocalTouchControllerTheme.current.borderBackgroundDark),
        onEntityChosen = {
            component.onConditionAdded(SelectEntityLayerConditionKey(it))
        },
    )
}
