/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.gal.key.serialization

import top.fifthlight.touchcontroller.common.gal.key.DefaultKeyBindingType
import top.fifthlight.touchcontroller.common.serialization.enumByName

val JDefaultKeyBindingType = enumByName<DefaultKeyBindingType> {
    "attack" encodes DefaultKeyBindingType.ATTACK
    "use" encodes DefaultKeyBindingType.USE
    "inventory" encodes DefaultKeyBindingType.INVENTORY
    "swap_hands" encodes DefaultKeyBindingType.SWAP_HANDS
    "sneak" encodes DefaultKeyBindingType.SNEAK
    "sprint" encodes DefaultKeyBindingType.SPRINT
    "jump" encodes DefaultKeyBindingType.JUMP
    "player_list" encodes DefaultKeyBindingType.PLAYER_LIST
    "left" encodes DefaultKeyBindingType.LEFT
    "right" encodes DefaultKeyBindingType.RIGHT
    "up" encodes DefaultKeyBindingType.UP
    "down" encodes DefaultKeyBindingType.DOWN
}
