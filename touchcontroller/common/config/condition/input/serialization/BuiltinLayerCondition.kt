/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.config.condition.input.serialization

import top.fifthlight.touchcontroller.common.config.condition.input.BuiltinLayerCondition
import top.fifthlight.touchcontroller.common.serialization.enumByName

val JBuiltinLayerCondition = enumByName<BuiltinLayerCondition> {
    "swimming" encodes BuiltinLayerCondition.SWIMMING
    "underwater" encodes BuiltinLayerCondition.UNDERWATER
    "flying" encodes BuiltinLayerCondition.FLYING
    "can_fly" encodes BuiltinLayerCondition.CAN_FLY
    "sneaking" encodes BuiltinLayerCondition.SNEAKING
    "sprinting" encodes BuiltinLayerCondition.SPRINTING
    "on_ground" encodes BuiltinLayerCondition.ON_GROUND
    listOf("not_on_ground", "no_on_ground") encodes BuiltinLayerCondition.NOT_ON_GROUND
    "using_item" encodes BuiltinLayerCondition.USING_ITEM
    "riding" encodes BuiltinLayerCondition.RIDING
    "entity_selected" encodes BuiltinLayerCondition.ENTITY_SELECTED
    "block_selected" encodes BuiltinLayerCondition.BLOCK_SELECTED
}
