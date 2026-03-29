/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.gal.action.v1_21_10

import net.minecraft.client.Minecraft
import net.minecraft.client.Screenshot
import net.minecraft.client.gui.components.ChatComponent
import top.fifthlight.combine.backend.minecraft.text.v1_21_10.toMinecraft
import top.fifthlight.combine.data.Text
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.common.gal.action.GameAction

@ActualImpl(GameAction::class)
object GameActionImpl: GameAction {
    @JvmStatic
    @ActualConstructor
    fun of(): GameAction = this

    private val client: Minecraft = Minecraft.getInstance()

    override fun openChatScreen() {
        client.openChatScreen(ChatComponent.ChatMethod.MESSAGE)
    }

    override fun openGameMenu() {
        client.pauseGame(false)
    }

    override fun sendMessage(text: Text) {
        client.gui.chat.addMessage(text.toMinecraft())
    }

    override fun nextPerspective() {
        val perspective = client.options.cameraType
        client.options.cameraType = client.options.cameraType.cycle()
        if (perspective.isFirstPerson != client.options.cameraType.isFirstPerson) {
            val newCameraEntity = client.getCameraEntity().takeIf { client.options.cameraType.isFirstPerson }
            client.gameRenderer.checkEntityPostEffect(newCameraEntity)
        }
    }

    override fun takeScreenshot() {
        Screenshot.grab(
            client.gameDirectory,
            client.mainRenderTarget,
        ) { message ->
            this.client.execute {
                this.client.gui.chat.addMessage(message)
            }
        }
    }

    override var hudHidden: Boolean
        get() = client.options.hideGui
        set(value) {
            client.options.hideGui = value
        }

    override fun takePanorama() {
        client.grabPanoramixScreenshot(
            client.gameDirectory,
        ).let { message ->
            this.client.execute {
                this.client.gui.chat.addMessage(message)
            }
        }
    }
}
