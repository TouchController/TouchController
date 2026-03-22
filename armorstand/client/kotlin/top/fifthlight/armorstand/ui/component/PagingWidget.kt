package top.fifthlight.armorstand.ui.component

import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.Renderable
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.layouts.LayoutElement
import net.minecraft.client.gui.narration.NarratableEntry
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.network.chat.Component
import net.minecraft.util.CommonColors
import java.util.function.Consumer

class PagingWidget(
    private val textRenderer: Font,
    height: Int = 20,
    var currentPage: Int = 1,
    var totalPages: Int,
    val onPrevPage: () -> Unit,
    val onNextPage: () -> Unit,
) : AbstractContainerEventHandler(), LayoutElement, Renderable, NarratableEntry, ResizableLayout {
    private var _x = 0
    private var _y = 0
    private var _width = 0
    private var _height = height
    private val buttonHeight = 20
    private val prevPageButton = Button.builder(Component.literal("<")) {
        onPrevPage()
    }.apply {
        size(64, buttonHeight)
    }.build()
    private val nextPageButton = Button.builder(Component.literal(">")) {
        onNextPage()
    }.apply {
        size(64, buttonHeight)
    }.build()

    override fun children(): List<GuiEventListener> = listOf(prevPageButton, nextPageButton)

    override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height
    }

    override fun getRectangle(): ScreenRectangle = ScreenRectangle(_x, _y, _width, _height)

    override fun setFocused(focused: Boolean) {
        super.setFocused(focused)
        this.focused?.let { focusedElement ->
            focusedElement.isFocused = focused
        }
    }

    override fun render(
        context: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        deltaTicks: Float,
    ) {
        prevPageButton.render(context, mouseX, mouseY, deltaTicks)
        context.drawCenteredString(
            textRenderer,
            Component.translatable("armorstand.page.switcher", currentPage, totalPages),
            x + width / 2,
            y + (height - textRenderer.lineHeight) / 2,
            CommonColors.WHITE
        )
        nextPageButton.render(context, mouseX, mouseY, deltaTicks)
    }

    fun refresh() {
        prevPageButton.active = currentPage > 1
        nextPageButton.active = currentPage < totalPages
    }

    fun init() {
        val buttonY = y + (height - buttonHeight) / 2
        prevPageButton.x = x
        prevPageButton.y = buttonY
        nextPageButton.x = x + width - nextPageButton.width
        nextPageButton.y = buttonY
    }

    override fun narrationPriority(): NarratableEntry.NarrationPriority = maxOf(prevPageButton.narrationPriority(), nextPageButton.narrationPriority())

    override fun updateNarration(builder: NarrationElementOutput) {
        prevPageButton.updateNarration(builder)
        nextPageButton.updateNarration(builder)
    }

    override fun setDimensions(width: Int, height: Int) {
        _width = width
        _height = height
    }

    override fun setX(x: Int) {
        _x = x
    }

    override fun setY(y: Int) {
        _y = y
    }

    override fun getX() = _x

    override fun getY() = _y

    override fun getWidth() = _width

    override fun getHeight() = _height

    override fun visitWidgets(consumer: Consumer<AbstractWidget>) {}
}
