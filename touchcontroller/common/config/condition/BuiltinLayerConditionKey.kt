/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.config.condition

import top.fifthlight.touchcontroller.common.config.condition.input.BuiltinLayerCondition
import top.fifthlight.touchcontroller.common.config.condition.input.LayerConditionInput

data class BuiltinLayerConditionKey(
    val condition: BuiltinLayerCondition,
) : LayerConditions.Key {
    override fun isFulfilled(input: LayerConditionInput): Boolean = condition in input.builtinCondition
}
