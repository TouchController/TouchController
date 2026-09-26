/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.config.condition

import top.fifthlight.combine.item.data.Item
import top.fifthlight.touchcontroller.common.config.condition.LayerConditions.Key
import top.fifthlight.touchcontroller.common.config.condition.input.LayerConditionInput

data class HoldingItemLayerConditionKey(
    val item: Item,
) : Key {
    override fun isFulfilled(input: LayerConditionInput) = input.holdingItem(item)
}
