package top.fifthlight.touchcontroller.common.ui.config.tab.general

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import top.fifthlight.combine.data.Text
import top.fifthlight.combine.layout.Arrangement
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.placement.fillMaxSize
import top.fifthlight.combine.modifier.placement.padding
import top.fifthlight.combine.modifier.scroll.verticalScroll
import top.fifthlight.combine.widget.layout.Column
import top.fifthlight.touchcontroller.assets.Texts
import top.fifthlight.touchcontroller.common.config.data.ControlConfig
import top.fifthlight.touchcontroller.common.ui.config.model.LocalConfigScreenModel
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme
import top.fifthlight.touchcontroller.common.ui.widget.IntSliderPreferenceItem
import top.fifthlight.touchcontroller.common.ui.widget.SliderPreferenceItem
import top.fifthlight.touchcontroller.common.ui.config.tab.Tab
import top.fifthlight.touchcontroller.common.ui.config.tab.TabGroup
import top.fifthlight.touchcontroller.common.ui.config.tab.TabOptions

object ControlTab : Tab() {
    override val options = TabOptions(
        titleId = Texts.SCREEN_CONFIG_GENERAL_CONTROL_TITLE,
        group = TabGroup.GeneralGroup,
        index = 1,
        onReset = { copy(control = ControlConfig()) },
    )

    @Composable
    override fun Content() {
        val screenModel = LocalConfigScreenModel.current
        Column(
            modifier = Modifier
                .padding(8)
                .verticalScroll(background = LocalTouchControllerTheme.current.background)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8),
        ) {
            val uiState by screenModel.uiState.collectAsState()
            val globalConfig = uiState.config
            fun update(editor: ControlConfig.() -> ControlConfig) {
                screenModel.updateConfig { copy(control = editor(control)) }
            }
            SliderPreferenceItem(
                title = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_CONTROL_VIEW_MOVEMENT_SENSITIVITY_TITLE),
                description = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_CONTROL_VIEW_MOVEMENT_SENSITIVITY_DESCRIPTION),
                percent = false,
                range = 0f..1800f,
                value = globalConfig.control.viewMovementSensitivity,
                onValueChanged = { update { copy(viewMovementSensitivity = it) } }
            )
            IntSliderPreferenceItem(
                title = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_CONTROL_VIEW_HOLD_DETECT_THRESHOLD_TITLE),
                description = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_CONTROL_VIEW_HOLD_DETECT_THRESHOLD_DESCRIPTION),
                range = 0..10,
                value = globalConfig.control.viewHoldDetectThreshold,
                onValueChanged = { update { copy(viewHoldDetectThreshold = it) } }
            )
            IntSliderPreferenceItem(
                title = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_CONTROL_VIEW_HOLD_DETECT_TICKS_TITLE),
                description = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_CONTROL_VIEW_HOLD_DETECT_TICKS_DESCRIPTION),
                range = 1..60,
                value = globalConfig.control.viewHoldDetectTicks,
                onValueChanged = { update { copy(viewHoldDetectTicks = it) } }
            )
            IntSliderPreferenceItem(
                title = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_CONTROL_CREATIVE_BREAK_HOLD_TICKS_TITLE),
                description = Text.translatable(Texts.SCREEN_CONFIG_GENERAL_CONTROL_CREATIVE_BREAK_HOLD_TICKS_DESCRIPTION),
                range = 1..60,
                value = globalConfig.control.creativeBreakDetectTicks,
                onValueChanged = { update { copy(creativeBreakDetectTicks = it) } }
            )
        }
    }
}
