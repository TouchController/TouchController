/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.gal.action.v26_2

import net.minecraft.client.Minecraft
import net.minecraft.client.Screenshot
import net.minecraft.client.gui.components.ChatComponent
import top.fifthlight.combine.backend.minecraft.text.v26_2.toMinecraft
import top.fifthlight.combine.core.data.Text
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.common.gal.action.GameAction

@ActualImpl(GameAction::class)
object GameActionImpl : GameAction {
    @JvmStatic
    @ActualConstructor
    fun of(): GameAction = this

    private val client: Minecraft = Minecraft.getInstance()

    override fun openChatScreen() {
        client.gui.openChatScreen(ChatComponent.ChatMethod.MESSAGE)
    }

    override fun openGameMenu() {
        client.pauseGame(false)
    }

    override fun sendMessage(text: Text) {
        client.gui.hud.chat.addClientSystemMessage(text.toMinecraft())
    }

    override fun nextPerspective() {
        val perspective = client.options.cameraType
        client.options.cameraType = client.options.cameraType.cycle()
        if (perspective.isFirstPerson != client.options.cameraType.isFirstPerson) {
            val newCameraEntity = client.cameraEntity.takeIf { client.options.cameraType.isFirstPerson }
            client.gameRenderer.checkEntityPostEffect(newCameraEntity)
        }
    }

    override fun takeScreenshot() {
        Screenshot.grab(client, false)
    }

    override var hudHidden: Boolean
        get() = client.gui.hud.isHidden
        set(value) {
            if (value != client.gui.hud.isHidden) {
                client.gui.hud.toggle()
            }
        }

    override fun takePanorama() {
        client.showDebugChat(client.grabPanoramixScreenshot(client.gameDirectory));
    }
}
