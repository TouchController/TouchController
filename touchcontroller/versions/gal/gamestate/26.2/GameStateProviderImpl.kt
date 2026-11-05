/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.gal.gamestate.v26_2

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.common.gal.gamestate.CameraPerspective
import top.fifthlight.touchcontroller.common.gal.gamestate.GameState
import top.fifthlight.touchcontroller.common.gal.gamestate.GameStateProvider

private object GameStateImpl : GameState {
    private val client = Minecraft.getInstance()

    private fun isCloseableGui(screen: Screen): Boolean =
        screen is AbstractContainerScreen<*> ||
            screen is AdvancementsScreen ||
            screen is SocialInteractionsScreen

    override val inGame: Boolean
        get() {
            if (client.player == null || client.gui.overlay() != null) {
                return false
            }
            val screen = client.gui.screen() ?: return true
            return isCloseableGui(screen)
        }

    override val inGui: Boolean
        get() = client.gui.screen()?.let(::isCloseableGui) == true

    override val perspective: CameraPerspective
        get() = CameraPerspectiveImpl.from(client.options.cameraType)
}

@ActualImpl(GameStateProvider::class)
object GameStateProviderImpl : GameStateProvider {
    @JvmStatic
    @ActualConstructor
    fun of(): GameStateProvider = this

    override val currentState: GameState
        get() = GameStateImpl
}
