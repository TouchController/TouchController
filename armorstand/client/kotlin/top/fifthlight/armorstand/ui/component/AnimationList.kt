package top.fifthlight.armorstand.ui.component

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.ObjectSelectionList
import net.minecraft.network.chat.Component
import net.minecraft.util.CommonColors
import top.fifthlight.armorstand.ui.state.AnimationScreenState

class AnimationList(
    client: Minecraft,
    width: Int = 0,
    height: Int = 0,
    x: Int = 0,
    y: Int = 0,
    val onClicked: (AnimationScreenState.AnimationItem) -> Unit,
) : ObjectSelectionList<AnimationList.Entry>(
    client,
    width,
    height,
    y,
    42,
) {
    override fun renderListBackground(context: GuiGraphics) = Unit
    override fun renderListSeparators(context: GuiGraphics) = Unit

    init {
        this.x = x
    }

    companion object {
        private const val ENTRY_PADDING = 8
    }

    override fun scrollBarX() = right - 6

    override fun getRowLeft() = x

    override fun getRowWidth() = width - 9

    override fun renderSelection(
        context: GuiGraphics,
        y: Int,
        entryWidth: Int,
        entryHeight: Int,
        borderColor: Int,
        fillColor: Int,
    ) {
        context.fill(rowLeft, y - 2, rowRight, y + entryHeight + 2, borderColor)
        context.fill(rowLeft + 1, y - 1, rowRight - 1, y + entryHeight + 1, fillColor)
    }

    fun setEntries(entries: List<AnimationScreenState.AnimationItem>) {
        clearEntries()
        for (item in entries) {
            addEntry(Entry(item))
        }
    }

    inner class Entry(val item: AnimationScreenState.AnimationItem) : ObjectSelectionList.Entry<Entry>() {
        private val name =
            item.name?.let { name -> Component.literal(name) } ?: Component.translatable("armorstand.animation.name.unnamed")
        private val length = item.duration?.let { duration ->
            val minutes = (duration / 60).toInt().toString().padStart(2, '0')
            val seconds = (duration % 60).toInt().toString().padStart(2, '0')
            Component.literal("$minutes:$seconds")
        }
        private val source = when (val source = item.source) {
            is AnimationScreenState.AnimationItem.Source.Embed -> Component.translatable("armorstand.animation.source.embed")
            is AnimationScreenState.AnimationItem.Source.External -> Component.translatable(
                "armorstand.animation.source.external",
                source.path.fileName
            )
        }

        override fun getNarration(): Component = Component.empty()

        override fun render(
            context: GuiGraphics,
            index: Int,
            y: Int,
            x: Int,
            entryWidth: Int,
            entryHeight: Int,
            mouseX: Int,
            mouseY: Int,
            hovered: Boolean,
            tickProgress: Float,
        ) {
            val font = minecraft.font
            val length = length
            val timeWidth = length?.let { length -> font.width(length) }
            val nameTextWidth = font.width(name)
            val timeExtraPadding = 2
            val nameAreaWidth = if (timeWidth != null) {
                entryWidth - ENTRY_PADDING * 3 - timeWidth - timeExtraPadding
            } else {
                entryWidth - ENTRY_PADDING * 2
            }
            renderScrollingString(
                context,
                font,
                name,
                x + ENTRY_PADDING + nameTextWidth / 2,
                x + ENTRY_PADDING,
                y + ENTRY_PADDING,
                x + ENTRY_PADDING + nameAreaWidth,
                y + ENTRY_PADDING + font.lineHeight,
                CommonColors.WHITE,
            )
            length?.let { length ->
                context.drawString(
                    font,
                    length,
                    x + entryWidth - ENTRY_PADDING - timeWidth!! - timeExtraPadding,
                    y + ENTRY_PADDING,
                    CommonColors.GRAY,
                )
            }
            val sourceTextWidth = font.width(source)
            renderScrollingString(
                context,
                font,
                source,
                x + ENTRY_PADDING + sourceTextWidth / 2,
                x + ENTRY_PADDING,
                y + entryHeight - ENTRY_PADDING - font.lineHeight,
                x + entryWidth - ENTRY_PADDING,
                y + entryHeight - ENTRY_PADDING,
                CommonColors.GRAY,
            )
        }

        override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
            onClicked(item)
            return true
        }
    }
}
