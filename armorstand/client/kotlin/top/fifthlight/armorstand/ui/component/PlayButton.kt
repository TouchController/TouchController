package top.fifthlight.armorstand.ui.component

import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

class PlayButton(
    x: Int = 0,
    y: Int = 0,
    width: Int = 20,
    height: Int = 20,
    playing: Boolean = false,
    onPress: OnPress,
) : Button(
    x,
    y,
    width,
    height,
    if (playing) {
        PAUSE_TEXT
    } else {
        PLAY_TEXT
    },
    onPress,
    DEFAULT_NARRATION,
) {
    companion object {
        private val PAUSE_TEXT = Component.translatable("armorstand.animation.pause")
        private val PLAY_TEXT = Component.translatable("armorstand.animation.play")
        private val PAUSE_ICON = ResourceLocation.fromNamespaceAndPath("armorstand", "pause")
        private val PLAY_ICON = ResourceLocation.fromNamespaceAndPath("armorstand", "play")
        private const val ICON_WIDTH = 8
        private const val ICON_HEIGHT = 8
    }

    var playing = playing
        set(value) {
            field = value
            message = if (value) {
                PAUSE_TEXT
            } else {
                PLAY_TEXT
            }
        }

    override fun renderString(graphics: GuiGraphics, font: Font, color: Int) {
        val icon = if (playing) {
            PAUSE_ICON
        } else {
            PLAY_ICON
        }
        graphics.blitSprite(
            RenderPipelines.GUI_TEXTURED,
            icon,
            x + (width - ICON_WIDTH) / 2,
            y + (height - ICON_HEIGHT) / 2,
            ICON_WIDTH,
            ICON_HEIGHT,
        )
    }
}
