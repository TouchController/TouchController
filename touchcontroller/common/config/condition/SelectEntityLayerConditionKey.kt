/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.config.condition

import top.fifthlight.touchcontroller.common.config.condition.input.LayerConditionInput
import top.fifthlight.touchcontroller.common.gal.entity.EntityType
import top.fifthlight.touchcontroller.common.gal.view.CrosshairTarget

data class SelectEntityLayerConditionKey(
    val entityType: EntityType,
) : LayerConditions.Key {
    override fun isFulfilled(input: LayerConditionInput): Boolean =
        (input.crosshairTarget as? CrosshairTarget.Entity)?.entityType == entityType
}
