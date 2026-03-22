package top.fifthlight.touchcontroller.common.ui.layer.tab.builtin

import androidx.compose.runtime.Composable
import top.fifthlight.combine.data.Identifier
import top.fifthlight.combine.data.Text
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.placement.fillMaxSize
import top.fifthlight.combine.modifier.placement.padding
import top.fifthlight.combine.modifier.scroll.verticalScroll
import top.fifthlight.combine.widget.layout.FlowRow
import top.fifthlight.combine.widget.ui.Text
import top.fifthlight.touchcontroller.assets.Texts
import top.fifthlight.touchcontroller.assets.Textures
import top.fifthlight.touchcontroller.common.config.condition.input.BuiltinLayerCondition
import top.fifthlight.touchcontroller.common.config.condition.BuiltinLayerConditionKey
import top.fifthlight.touchcontroller.common.ui.layer.tab.LayerConditionTab
import top.fifthlight.touchcontroller.common.ui.layer.tab.LocalLayerConditionTabContext
import top.fifthlight.touchcontroller.common.ui.widget.ListButton

object BuiltInTab : LayerConditionTab() {
    @Composable
    override fun Icon() {
        top.fifthlight.combine.widget.ui.Icon(Textures.icon_motion)
    }

    override val name: Identifier
        get() = Texts.SCREEN_LAYER_EDITOR_BUILTIN

    @Composable
    override fun Content() {
        val layerConditionTabContext = LocalLayerConditionTabContext.current
        FlowRow(
            modifier = Modifier.Companion
                .padding(4)
                .verticalScroll()
                .fillMaxSize(),
            maxColumns = 2,
            expandColumnWidth = true,
        ) {
            for (key in BuiltinLayerCondition.entries) {
                ListButton(
                    onClick = {
                        layerConditionTabContext.onConditionAdded(
                            BuiltinLayerConditionKey(key)
                        )
                    }
                ) {
                    Text(Text.translatable(key.text))
                }
            }
        }
    }
}
