package top.fifthlight.armorstand.ui.screen

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.StringWidget
import net.minecraft.client.gui.layouts.LayoutSettings
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import top.fifthlight.armorstand.config.ConfigHolder
import top.fifthlight.armorstand.ui.component.BorderLayout
import top.fifthlight.armorstand.ui.component.Insets
import top.fifthlight.armorstand.ui.component.LinearLayout
import top.fifthlight.armorstand.ui.component.Surface
import top.fifthlight.armorstand.ui.model.VmcConfigScreenModel
import top.fifthlight.armorstand.ui.util.textField
import top.fifthlight.armorstand.vmc.VmcMarionetteManager

class VmcConfigScreen(parent: Screen? = null) : ArmorStandScreen<VmcConfigScreen, VmcConfigScreenModel>(
    title = Component.translatable("armorstand.vmc"),
    parent = parent,
    viewModelFactory = ::VmcConfigScreenModel,
) {
    private val topBar by lazy {
        StringWidget(width, 32, title, currentMinecraft.font)
    }
    private val closeButton = Button.builder(CommonComponents.GUI_BACK) { onClose() }.build()

    private val statusLabel by lazy {
        StringWidget(Component.empty(), currentMinecraft.font).apply {
            scope.launch {
                viewModel.uiState.collect {
                    message = when (it.state) {
                        is VmcMarionetteManager.State.Running -> Component.translatable(
                            "armorstand.vmc.running",
                            it.state.port
                        )

                        is VmcMarionetteManager.State.Stopped -> Component.translatable("armorstand.vmc.stopped")

                        is VmcMarionetteManager.State.Failed -> Component.translatable(
                            "armorstand.vmc.failed",
                            it.state.exception.message
                        )
                    }
                }
            }
        }
    }

    private val portLabel by lazy {
        StringWidget(Component.translatable("armorstand.vmc.port"), currentMinecraft.font)
    }

    private val portField by lazy {
        val textContent = MutableStateFlow(viewModel.uiState.value.portNumber.toString())
        scope.launch {
            textContent.collect { portStr ->
                portStr.toIntOrNull()?.let { port ->
                    ConfigHolder.update {
                        copy(vmcUdpPort = port)
                    }
                }
            }
        }
        textField(
            text = textContent,
            onChanged = {
                textContent.value = it
            },
        )
    }

    private val startButton = Button.builder(Component.translatable("armorstand.vmc.start")) {
        when (viewModel.uiState.value.state) {
            VmcMarionetteManager.State.Stopped, is VmcMarionetteManager.State.Failed -> viewModel.startVmcClient()
            is VmcMarionetteManager.State.Running -> viewModel.stopVmcClient()
        }
    }.build().apply {
        scope.launch {
            viewModel.uiState.collect {
                message = when (it.state) {
                    VmcMarionetteManager.State.Stopped, is VmcMarionetteManager.State.Failed ->
                        Component.translatable("armorstand.vmc.start")

                    is VmcMarionetteManager.State.Running -> Component.translatable("armorstand.vmc.stop")
                }
            }
        }
    }

    override fun init() {
        val rootLayout = BorderLayout(
            width = width,
            height = height,
            direction = BorderLayout.Direction.VERTICAL,
        )
        rootLayout.setFirstElement(topBar) { topBar, width, height -> topBar.width = width }
        rootLayout.setCenterElement(
            widget = LinearLayout(
                width = 192,
                direction = LinearLayout.Direction.VERTICAL,
                padding = Insets(8),
                gap = 8,
                surface = Surface.listBackgroundWithSeparator(),
            ).apply {
                add(statusLabel, expand = true)
                add(
                    widget = BorderLayout(
                        width = 192,
                        height = portField.height,
                        direction = BorderLayout.Direction.HORIZONTAL,
                    ).apply {
                        setFirstElement(portLabel, LayoutSettings.defaults().paddingRight(8))
                        setCenterElement(portField)
                    },
                    expand = true,
                )
                add(startButton, expand = true)
                pack()
                addRenderableOnly(this)
            },
            layoutSettings = LayoutSettings.defaults().alignVerticallyMiddle().alignHorizontallyCenter(),
            onSizeChanged = { _, _, _ -> },
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
            })
        rootLayout.arrangeElements()
        rootLayout.visitWidgets { addRenderableWidget(it) }
    }
}
