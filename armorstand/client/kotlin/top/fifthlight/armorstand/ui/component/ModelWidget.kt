package top.fifthlight.armorstand.ui.component

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.client.sounds.SoundManager
import net.minecraft.network.chat.Component
import kotlin.math.min

class ModelWidget(
    x: Int = 0,
    y: Int = 0,
    width: Int = 1,
    height: Int = 1,
    private val surface: Surface = Surface.empty,
) : AbstractWidget(x, y, width, height, Component.empty()) {
    override fun isActive() = false

    override fun renderWidget(
        context: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        deltaTicks: Float,
    ) {
        surface.draw(context, x, y, width, height)
        val minecraft = Minecraft.getInstance()
        minecraft.player?.let { player ->
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                context,
                x,
                y,
                x + width,
                y + height,
                min(width, height) / 2,
                .0625f,
                mouseX.toFloat(),
                mouseY.toFloat(),
                player,
            )
        }
    }

    override fun playDownSound(soundManager: SoundManager) {}

    override fun updateWidgetNarration(builder: NarrationElementOutput) {}
}
