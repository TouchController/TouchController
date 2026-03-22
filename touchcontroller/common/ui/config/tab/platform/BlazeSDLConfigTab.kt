package top.fifthlight.touchcontroller.common.ui.config.tab.platform

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
import top.fifthlight.touchcontroller.common.config.data.StatusConfig
import top.fifthlight.touchcontroller.common.config.platform.BlazeSDLPlatformConfig
import top.fifthlight.touchcontroller.common.ui.config.model.LocalConfigScreenModel
import top.fifthlight.touchcontroller.common.ui.config.tab.Tab
import top.fifthlight.touchcontroller.common.ui.config.tab.TabGroup
import top.fifthlight.touchcontroller.common.ui.config.tab.TabOptions
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme
import top.fifthlight.touchcontroller.common.ui.widget.IntSliderPreferenceItem
import top.fifthlight.touchcontroller.common.ui.widget.SliderPreferenceItem

object BlazeSDLConfigTab : Tab() {
    override val options = TabOptions(
        titleId = Texts.SCREEN_CONFIG_PLATFORM_TITLE,
        group = TabGroup.SystemGroup,
        index = 2,
        onReset = { copy(platform = platform.copy(blazesdl = BlazeSDLPlatformConfig())) },
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
            fun update(editor: BlazeSDLPlatformConfig.() -> BlazeSDLPlatformConfig) {
                screenModel.updateConfig {
                    copy(
                        platform = platform.copy(
                            blazesdl = editor(platform.blazesdl),
                        ),
                    )
                }
            }
            SliderPreferenceItem(
                title = Text.translatable(Texts.SCREEN_CONFIG_PLATFORM_BLAZESDL_VIBRATION_STRENGTH_TITLE),
                description = Text.translatable(Texts.SCREEN_CONFIG_PLATFORM_BLAZESDL_VIBRATION_STRENGTH_DESCRIPTION),
                value = globalConfig.platform.blazesdl.vibrationStrength,
                range = 0f..1f,
                onValueChanged = { update { copy(vibrationStrength = it) } }
            )
            IntSliderPreferenceItem(
                title = Text.translatable(Texts.SCREEN_CONFIG_PLATFORM_BLAZESDL_VIBRATION_LENGTH_TITLE),
                description = Text.translatable(Texts.SCREEN_CONFIG_PLATFORM_BLAZESDL_VIBRATION_LENGTH_DESCRIPTION),
                value = globalConfig.platform.blazesdl.vibrationLength,
                range = 0..1000,
                onValueChanged = { update { copy(vibrationLength = it) } }
            )
        }
    }
}
