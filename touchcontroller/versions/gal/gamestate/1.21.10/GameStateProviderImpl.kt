/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.gal.gamestate.v1_21_10

import net.minecraft.client.CameraType
import net.minecraft.client.Minecraft
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.common.gal.gamestate.CameraPerspective
import top.fifthlight.touchcontroller.common.gal.gamestate.GameState
import top.fifthlight.touchcontroller.common.gal.gamestate.GameStateProvider

private object GameStateImpl : GameState {
    private val client = Minecraft.getInstance()

    override val inGame: Boolean
        get() = client.player != null

    override val inGui: Boolean
        get() = client.screen != null

    override val perspective: CameraPerspective
        get() = when (client.options.cameraType) {
            CameraType.FIRST_PERSON -> CameraPerspective.FIRST_PERSON
            CameraType.THIRD_PERSON_BACK -> CameraPerspective.THIRD_PERSON_BACK
            CameraType.THIRD_PERSON_FRONT -> CameraPerspective.THIRD_PERSON_FRONT
        }
}

@ActualImpl(GameStateProvider::class)
object GameStateProviderImpl : GameStateProvider {
    @JvmStatic
    @ActualConstructor
    fun of(): GameStateProvider = this

    override val currentState: GameState
        get() = GameStateImpl
}
