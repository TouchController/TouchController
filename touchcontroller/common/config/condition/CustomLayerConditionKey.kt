/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.config.condition

import top.fifthlight.touchcontroller.common.config.condition.input.LayerConditionInput
import kotlin.uuid.Uuid

data class CustomLayerConditionKey(
    val key: Uuid,
) : LayerConditions.Key {
    override fun isFulfilled(input: LayerConditionInput) = key in input.customCondition
}
