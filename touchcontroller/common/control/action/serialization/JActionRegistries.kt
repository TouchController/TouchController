/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.control.action.serialization

import top.fifthlight.touchcontroller.common.control.action.GameActions
import top.fifthlight.touchcontroller.common.control.action.PlayerActions
import top.fifthlight.touchcontroller.common.util.registry.serialization.registryConverter

val JGameActionInstance = registryConverter(GameActions.registry) { GameActions.unknown }

val JPlayerActionInstance = registryConverter(PlayerActions.registry) { PlayerActions.unknown }
