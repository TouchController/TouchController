package top.fifthlight.armorstand.ui.component

import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.tabs.Tab
import net.minecraft.client.gui.layouts.Layout
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.network.chat.Component
import java.util.function.Consumer

class LayoutScreenTab<T>(
    private val title: Component,
    val padding: Insets = Insets.ZERO,
    private val layoutFactory: () -> T,
) : Tab where T : ResizableLayout, T : Layout {
    override fun getTabTitle(): Component = this.title

    override fun getTabExtraNarration(): Component = Component.empty()

    val layout by lazy {
        layoutFactory()
    }

    override fun visitChildren(consumer: Consumer<AbstractWidget>) = layout.visitWidgets(consumer)

    override fun doLayout(tabArea: ScreenRectangle) {
        layout.setPosition(
            tabArea.position.x + padding.left,
            tabArea.position.y + padding.top,
        )
        layout.setDimensions(
            width = tabArea.width - padding.left - padding.right,
            height = tabArea.height - padding.top - padding.bottom,
        )
        layout.arrangeElements()
    }
}
