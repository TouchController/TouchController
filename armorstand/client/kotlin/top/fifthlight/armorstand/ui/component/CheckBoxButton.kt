package top.fifthlight.armorstand.ui.component

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.Checkbox
import net.minecraft.client.gui.narration.NarratedElementType
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component

class CheckBoxButton(
    var checked: Boolean,
    private val onClicked: () -> Unit,
) : AbstractWidget(0, 0, SIZE, SIZE, Component.empty()) {
    companion object {
        private const val SIZE = 17
    }

    override fun renderWidget(
        context: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        deltaTicks: Float,
    ) {
        val identifier = if (checked) {
            if (this.isFocused) {
                Checkbox.CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE
            } else {
                Checkbox.CHECKBOX_SELECTED_SPRITE
            }
        } else {
            if (this.isFocused) {
                Checkbox.CHECKBOX_HIGHLIGHTED_SPRITE
            } else {
                Checkbox.CHECKBOX_SPRITE
            }
        }

        context.blitSprite(
            RenderPipelines.GUI_TEXTURED,
            identifier,
            x,
            y,
            SIZE,
            SIZE,
        )
    }

    override fun onClick(mouseX: Double, mouseY: Double) = onClicked()

    override fun updateWidgetNarration(builder: NarrationElementOutput) {
        builder.add(NarratedElementType.TITLE, message)
        if (active) {
            if (isFocused) {
                builder.add(NarratedElementType.USAGE, Component.translatable("narration.checkbox.usage.focused"))
            } else {
                builder.add(NarratedElementType.USAGE, Component.translatable("narration.checkbox.usage.hovered"))
            }
        }
    }
}
