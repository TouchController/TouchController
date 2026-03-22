package top.fifthlight.armorstand.ui.screen

import kotlinx.coroutines.flow.map
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.StringWidget
import net.minecraft.client.gui.layouts.LayoutSettings
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import top.fifthlight.armorstand.config.GlobalConfig
import top.fifthlight.armorstand.ui.component.*
import top.fifthlight.armorstand.ui.model.RendererSelectViewModel
import top.fifthlight.armorstand.ui.util.checkbox

class RendererSelectScreen(parent: Screen? = null) : ArmorStandScreen<RendererSelectScreen, RendererSelectViewModel>(
    title = Component.translatable("armorstand.renderer"),
    viewModelFactory = ::RendererSelectViewModel,
    parent = parent,
) {
    private val rendererData = GlobalConfig.RendererKey.entries.map { Pair(it, RendererData(it)) }

    private data class RendererData(
        val speed: Speed,
        val isShaderCompatible: Boolean,
        val isAvailable: Boolean,
    ) {
        constructor(key: GlobalConfig.RendererKey) : this(
            speed = when (key) {
                GlobalConfig.RendererKey.VERTEX_SHADER_TRANSFORM -> Speed.FAST
                GlobalConfig.RendererKey.COMPUTE_SHADER_TRANSFORM -> Speed.MEDIUM
                GlobalConfig.RendererKey.CPU_TRANSFORM -> Speed.SLOW
            },
            isShaderCompatible = when (key) {
                GlobalConfig.RendererKey.VERTEX_SHADER_TRANSFORM -> false
                GlobalConfig.RendererKey.COMPUTE_SHADER_TRANSFORM -> true
                GlobalConfig.RendererKey.CPU_TRANSFORM -> true
            },
            isAvailable = key.type.isAvailable,
        )

        enum class Speed(val nameKey: String) {
            SLOW("armorstand.renderer.speed.slow"),
            MEDIUM("armorstand.renderer.speed.medium"),
            FAST("armorstand.renderer.speed.fast"),
        }
    }

    private val topBar by lazy {
        StringWidget(width, 32, title, currentMinecraft.font)
    }
    private val dataTable by lazy {
        GridLayout(
            surface = Surface.listBackgroundWithSeparator(),
            gridPadding = Insets(8),
        ).apply {
            val font = currentMinecraft.font
            var row = 0
            listOf(
                "armorstand.renderer.name",
                "armorstand.renderer.speed",
                "armorstand.renderer.shader_compatible",
                "armorstand.renderer.is_available",
                "armorstand.renderer.is_current",
            ).forEachIndexed { column, value ->
                add(
                    column = column,
                    row = row,
                    widget = StringWidget(Component.translatable(value), font),
                )
            }
            row++
            for ((key, value) in rendererData) {
                add(
                    column = 0,
                    row = row,
                    widget = StringWidget(Component.translatable(key.nameKey), font),
                )
                add(
                    column = 1,
                    row = row,
                    widget = StringWidget(Component.translatable(value.speed.nameKey), font),
                )
                add(
                    column = 2,
                    row = row,
                    widget = StringWidget(
                        if (value.isShaderCompatible) CommonComponents.GUI_YES else CommonComponents.GUI_NO,
                        font,
                    ),
                )
                add(
                    column = 3,
                    row = row,
                    widget = StringWidget(
                        if (value.isAvailable) CommonComponents.GUI_YES else CommonComponents.GUI_NO,
                        font,
                    ),
                )
                add(
                    column = 4,
                    row = row,
                    widget = checkbox(
                        value = viewModel.uiState.map { it.currentRenderer == key },
                        enabled = value.isAvailable,
                    ) {
                        viewModel.setCurrentRenderer(key)
                    },
                )
                row++
            }
            pack()
        }
    }
    private val closeButton = Button.builder(CommonComponents.GUI_BACK) { onClose() }.build()

    override fun init() {
        val rootLayout = BorderLayout(
            width = width,
            height = height,
            direction = BorderLayout.Direction.VERTICAL,
        )
        rootLayout.setFirstElement(topBar) { topBar, width, _ -> topBar.width = width }
        rootLayout.setCenterElement(
            LinearLayout(
                width = width,
                direction = LinearLayout.Direction.VERTICAL,
                align = LinearLayout.Align.CENTER,
            ).apply {
                add(dataTable, LayoutSettings.defaults().alignHorizontallyCenter())
            },
        )
        rootLayout.setSecondElement(
            LinearLayout(
                width = width,
                height = 32,
                direction = LinearLayout.Direction.HORIZONTAL,
                align = LinearLayout.Align.CENTER,
                gap = 8,
            ).apply {
                add(closeButton, LayoutSettings.defaults().alignVerticallyMiddle())
            }
        )
        rootLayout.arrangeElements()
        rootLayout.visitWidgets { addRenderableWidget(it) }
    }

    override fun renderBlurredBackground(graphics: GuiGraphics) {
        if (minecraft?.level == null) {
            super.renderBlurredBackground(graphics)
        }
    }
}
