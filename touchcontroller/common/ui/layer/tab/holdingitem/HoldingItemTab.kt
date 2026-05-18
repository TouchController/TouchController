/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.layer.tab.holdingitem

import androidx.compose.runtime.Composable
import top.fifthlight.combine.core.data.Identifier
import top.fifthlight.combine.widget.ui.Icon
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.assets.texture.Textures
import top.fifthlight.touchcontroller.common.config.condition.HoldingItemLayerConditionKey
import top.fifthlight.touchcontroller.common.ui.item.screen.ItemChooser
import top.fifthlight.touchcontroller.common.ui.layer.tab.LayerConditionTab
import top.fifthlight.touchcontroller.common.ui.layer.tab.LocalLayerConditionTabContext

object HoldingItemTab : LayerConditionTab() {
    @Composable
    override fun Icon() {
        Icon(Textures.icon_block)
    }

    override val name: Identifier
        get() = Texts.SCREEN_LAYER_EDITOR_HOLDING_ITEM

    override val needBorder: Boolean
        get() = false

    @Composable
    override fun Content() {
        val layerConditionTabContext = LocalLayerConditionTabContext.current
        ItemChooser(
            onItemChosen = {
                layerConditionTabContext.onConditionAdded(HoldingItemLayerConditionKey(it))
            },
        )
    }
}
