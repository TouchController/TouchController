/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.layer.tab.builtin

import androidx.compose.runtime.Composable
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.placement.fillMaxSize
import top.fifthlight.combine.core.modifier.placement.padding
import top.fifthlight.combine.core.modifier.scroll.verticalScroll
import top.fifthlight.combine.core.widget.layout.FlowRow
import top.fifthlight.combine.theme.blackstone.widget.ListButton
import top.fifthlight.combine.widget.Text
import top.fifthlight.touchcontroller.common.config.condition.BuiltinLayerConditionKey
import top.fifthlight.touchcontroller.common.config.condition.input.BuiltinLayerCondition
import top.fifthlight.touchcontroller.common.ui.layer.tab.LayerConditionTabComponent

@Composable
fun BuiltInTab(component: LayerConditionTabComponent.BuiltIn) {
    FlowRow(
        modifier = Modifier
            .padding(4)
            .verticalScroll()
            .fillMaxSize(),
        maxColumns = 2,
        expandColumnWidth = true,
    ) {
        for (key in BuiltinLayerCondition.entries) {
            ListButton(
                onClick = {
                    component.onConditionAdded(
                        BuiltinLayerConditionKey(key)
                    )
                }
            ) {
                Text(Text.translatable(key.text))
            }
        }
    }
}
