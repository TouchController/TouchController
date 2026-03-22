package top.fifthlight.armorstand.ui.screen

import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.StringWidget
import net.minecraft.client.gui.layouts.LayoutSettings
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import top.fifthlight.armorstand.ui.component.BorderLayout
import top.fifthlight.armorstand.ui.component.LinearLayout

class DebugScreen(parent: Screen? = null) : BaseArmorStandScreen<DebugScreen>(
    title = Component.translatable("armorstand.debug_screen"),
    parent = parent
) {
    private val topBar by lazy {
        StringWidget(width, 32, title, currentMinecraft.font)
    }
    private val closeButton = Button.builder(CommonComponents.GUI_BACK) { onClose() }.build()
    private val debugTip by lazy {
        StringWidget(Component.translatable("armorstand.debug_screen.tip"), currentMinecraft.font)
    }
    private val buttons = listOf(
        Button.builder(Component.translatable("armorstand.debug_screen.database")) {
            currentMinecraft.setScreen(DatabaseScreen(this@DebugScreen))
        }.build()
    )

    override fun init() {
        val rootLayout = BorderLayout(
            width = width,
            height = height,
            direction = BorderLayout.Direction.VERTICAL,
        )
        rootLayout.setFirstElement(topBar) { topBar, width, height -> topBar.width = width }
        rootLayout.setCenterElement(
            LinearLayout(
                direction = LinearLayout.Direction.VERTICAL,
                width = width,
                gap = 8
            ).apply {
                add(debugTip, LayoutSettings.defaults().apply { alignHorizontallyCenter() })
                buttons.forEach { button ->
                    add(button, LayoutSettings.defaults().apply { alignHorizontallyCenter() })
                }
            }
        )
        rootLayout.setSecondElement(
            LinearLayout(
                width = width,
                height = 32,
                direction = LinearLayout.Direction.HORIZONTAL,
                align = LinearLayout.Align.CENTER,
                gap = 8,
            ).apply {
                add(closeButton, LayoutSettings.defaults().apply { alignVerticallyMiddle() })
            })
        rootLayout.arrangeElements()
        rootLayout.visitWidgets { addRenderableWidget(it) }
    }
}
