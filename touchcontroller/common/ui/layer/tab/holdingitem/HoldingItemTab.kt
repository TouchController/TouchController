/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.layer.tab.holdingitem

import androidx.compose.runtime.Composable
import top.fifthlight.touchcontroller.common.ui.item.screen.ItemChooser
import top.fifthlight.touchcontroller.common.ui.layer.tab.LayerConditionTabComponent

@Composable
fun HoldingItemTab(component: LayerConditionTabComponent.HoldingItem) {
    ItemChooser(component.itemChooser)
}
