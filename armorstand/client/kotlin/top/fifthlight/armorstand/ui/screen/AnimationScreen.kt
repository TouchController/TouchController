package top.fifthlight.armorstand.ui.screen

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.StringWidget
import net.minecraft.client.gui.layouts.LayoutSettings
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import top.fifthlight.armorstand.PlayerRenderer
import top.fifthlight.armorstand.ui.component.*
import top.fifthlight.armorstand.ui.model.AnimationViewModel
import top.fifthlight.armorstand.ui.state.AnimationScreenState
import top.fifthlight.armorstand.ui.util.slider

class AnimationScreen(parent: Screen? = null) : ArmorStandScreen<AnimationScreen, AnimationViewModel>(
    title = Component.translatable("armorstand.animation"),
    viewModelFactory = ::AnimationViewModel,
    parent = parent,
) {
    private val playButton = PlayButton(
        width = 20,
        height = 20,
        playing = false,
    ) {
        viewModel.togglePlay()
    }.also {
        scope.launch {
            viewModel.uiState.collect { state ->
                when (val state = state.playState) {
                    is AnimationScreenState.PlayState.None -> {
                        it.active = false
                        it.playing = false
                    }

                    is AnimationScreenState.PlayState.Paused -> {
                        it.active = !state.readonly
                        it.playing = false
                    }

                    is AnimationScreenState.PlayState.Playing -> {
                        it.active = !state.readonly
                        it.playing = true
                    }
                }
            }
        }
    }

    private val speedSlider = slider(
        textFactory = { slider, value ->
            Component.translatable("armorstand.animation.speed", value)
        },
        width = 100,
        height = 20,
        min = 0.1,
        max = 4.0,
        decimalPlaces = 2,
        value = viewModel.uiState.map {
            when (val state = it.playState) {
                AnimationScreenState.PlayState.None -> 1.0
                is AnimationScreenState.PlayState.Paused -> state.speed.toDouble()
                is AnimationScreenState.PlayState.Playing -> state.speed.toDouble()
            }
        },
        onValueChanged = { userTriggered, value ->
            if (userTriggered) {
                viewModel.updatePlaySpeed(value.toFloat())
            }
        },
    ).also {
        scope.launch {
            viewModel.uiState.collect { state ->
                when (val playState = state.playState) {
                    is AnimationScreenState.PlayState.None -> {
                        it.active = false
                    }

                    is AnimationScreenState.PlayState.Paused -> {
                        it.active = !playState.readonly
                    }

                    is AnimationScreenState.PlayState.Playing -> {
                        it.active = !playState.readonly
                    }
                }
            }
        }
    }

    private val progressSlider = slider(
        textFactory = { slider, text ->
            fun Double.toTime(): String {
                val minutes = (this / 60).toInt().toString().padStart(2, '0')
                val seconds = (this % 60).toInt().toString().padStart(2, '0')
                return "$minutes:$seconds"
            }
            Component.translatable(
                "armorstand.animation.progress",
                slider.realValue.toTime(),
                slider.max.toTime(),
            )
        },
        width = 100,
        height = 20,
        min = 0.0,
        max = 60.0,
        decimalPlaces = null,
        value = viewModel.uiState.map { state ->
            when (val playState = state.playState) {
                is AnimationScreenState.PlayState.None -> 0.0
                is AnimationScreenState.PlayState.Paused -> playState.progress.toDouble()
                is AnimationScreenState.PlayState.Playing -> playState.progress.toDouble()
            }
        },
        onValueChanged = { userTriggered, value ->
            if (userTriggered) {
                viewModel.updateProgress(value.toFloat())
            }
        }
    ).also {
        scope.launch {
            viewModel.uiState.collect { state ->
                when (val playState = state.playState) {
                    is AnimationScreenState.PlayState.None -> {
                        it.active = false
                        it.updateRange(0.0, 1.0)
                    }

                    is AnimationScreenState.PlayState.Paused -> {
                        it.active = !playState.readonly
                        it.updateRange(0.0, playState.length.toDouble())
                    }

                    is AnimationScreenState.PlayState.Playing -> {
                        it.active = !playState.readonly
                        it.updateRange(0.0, playState.length.toDouble())
                    }
                }
            }
        }
    }

    private val animationList = AnimationList(
        client = currentMinecraft,
        width = 150,
        onClicked = { item ->
            viewModel.switchAnimation(item)
        }
    ).also { list ->
        scope.launch {
            viewModel.uiState.map { state ->
                state.animations
            }.distinctUntilChanged().collect { animations ->
                list.setEntries(animations)
            }
        }
    }

    private val ikList = IkList(
        client = currentMinecraft,
        width = 128,
        onClicked = viewModel::setIkEnabled,
    ).also {
        scope.launch {
            viewModel.uiState.map { state ->
                state.ikList
            }.distinctUntilChanged().collect { ikList ->
                it.setList(ikList)
            }
        }
    }

    private val refreshAnimationButton = Button.builder(Component.translatable("armorstand.animation.refresh")) {
        viewModel.refreshAnimations()
    }.build()

    private val switchCameraButton = Button.builder(Component.translatable("armorstand.animation.no_camera")) {
        viewModel.switchCamera()
    }.width(100).build().also {
        scope.launch {
            PlayerRenderer.totalCameras.combine(PlayerRenderer.selectedCameraIndex, ::Pair).collect { (total, index) ->
                if (total?.isEmpty() ?: true) {
                    it.active = false
                    it.message = Component.translatable("armorstand.animation.no_camera")
                } else {
                    it.active = true
                    val current = index?.let { index -> total.getOrNull(index) }
                    it.message = if (current == null) {
                        Component.translatable("armorstand.animation.no_camera")
                    } else {
                        Component.translatable("armorstand.animation.current_camera_name", current.name ?: "#$index")
                    }
                }
            }
        }
    }

    override fun init() {
        val controlBarHeight = 36
        val animationPanelWidth = 128
        val animationPanelHeight = 256.coerceAtMost(height / 3 * 2)
        val ikPanelWidth = 128
        val ikPanelHeight = 192.coerceAtMost(height / 3 * 2)

        val controlBar = BorderLayout(
            x = 16,
            y = 16,
            width = width - 16 * 3 - animationPanelWidth,
            height = controlBarHeight,
            direction = BorderLayout.Direction.HORIZONTAL,
            surface = Surface.listBackground(),
        )
        controlBar.setFirstElement(
            LinearLayout(
                padding = Insets(8),
                gap = 8,
            ).apply {
                add(playButton)
                add(speedSlider)
                pack()
            }
        )
        controlBar.setCenterElement(
            widget = progressSlider,
            layoutSettings = LayoutSettings.defaults().padding(0, 8, 8, 8),
        )
        controlBar.setSecondElement(
            widget = switchCameraButton,
            layoutSettings = LayoutSettings.defaults().padding(0, 8, 8, 8),
        )

        controlBar.arrangeElements()
        addRenderableOnly(controlBar)
        controlBar.visitWidgets { addRenderableWidget(it) }

        val animationPanel = BorderLayout(
            x = width - animationPanelWidth - 16,
            y = 16,
            width = animationPanelWidth,
            height = animationPanelHeight,
            surface = Surface.listBackground(),
            direction = BorderLayout.Direction.VERTICAL,
        ).apply {
            setFirstElement(
                widget = StringWidget(
                    animationPanelWidth - 16,
                    font.lineHeight,
                    Component.translatable("armorstand.animation.title").withStyle(ChatFormatting.BOLD)
                        .withStyle(ChatFormatting.UNDERLINE),
                    font,
                ),
                layoutSettings = LayoutSettings.defaults().padding(8, 8),
            )
            animationList.width = animationPanelWidth - 16
            setCenterElement(
                widget = animationList,
                layoutSettings = LayoutSettings.defaults().padding(8, 0, 8, 8),
            )
            setSecondElement(
                widget = refreshAnimationButton,
                layoutSettings = LayoutSettings.defaults().padding(8),
            )
        }
        animationPanel.arrangeElements()
        addRenderableOnly(animationPanel)
        animationPanel.visitWidgets { addRenderableWidget(it) }

        val ikPanel = BorderLayout(
            x = 16,
            y = 16 * 2 + controlBarHeight,
            width = ikPanelWidth,
            height = ikPanelHeight,
            surface = Surface.listBackground(),
            direction = BorderLayout.Direction.VERTICAL,
        ).apply {
            setFirstElement(
                widget = StringWidget(
                    ikPanelWidth - 16,
                    font.lineHeight,
                    Component.translatable("armorstand.animation.ik_title").withStyle(ChatFormatting.BOLD)
                        .withStyle(ChatFormatting.UNDERLINE),
                    font,
                ),
                layoutSettings = LayoutSettings.defaults().padding(8, 8),
            )
            animationList.width = ikPanelWidth - 16
            setCenterElement(
                widget = ikList,
                layoutSettings = LayoutSettings.defaults().padding(8, 0, 8, 8),
            )
        }
        ikPanel.arrangeElements()
        addRenderableOnly(ikPanel)
        ikPanel.visitWidgets { addRenderableWidget(it) }
    }

    override fun tick() {
        viewModel.tick()
    }

    override fun isPauseScreen() = false
    override fun renderBlurredBackground(context: GuiGraphics) {}
    override fun renderMenuBackground(context: GuiGraphics) {}
}
