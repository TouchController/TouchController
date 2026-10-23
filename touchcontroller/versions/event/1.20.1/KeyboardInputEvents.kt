/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.event.v1_20_1

import net.minecraft.client.Minecraft
import net.minecraft.client.player.KeyboardInput
import top.fifthlight.touchcontroller.common.model.ControllerHudModel

object KeyboardInputEvents {
    fun onEndTick(input: KeyboardInput) {
        val client = Minecraft.getInstance()
        if (client.screen != null) {
            return
        }

        val result = ControllerHudModel.result

        input.forwardImpulse += result.forward
        input.leftImpulse += result.left
        input.forwardImpulse = input.forwardImpulse.coerceIn(-1f, 1f)
        input.leftImpulse = input.leftImpulse.coerceIn(-1f, 1f)
        input.up = input.up || result.forward > 0.5f || (result.boatLeft && result.boatRight)
        input.down = input.down || result.forward < -0.5f
        input.left = input.left || result.left > 0.5f || (!result.boatLeft && result.boatRight)
        input.right = input.right || result.left < -0.5f || (result.boatLeft && !result.boatRight)
    }
}
