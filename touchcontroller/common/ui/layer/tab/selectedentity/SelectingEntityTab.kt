package top.fifthlight.touchcontroller.common.ui.layer.tab.selectedentity

import androidx.compose.runtime.Composable
import top.fifthlight.combine.data.Identifier
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.drawing.border
import top.fifthlight.combine.widget.ui.Icon
import top.fifthlight.touchcontroller.assets.Texts
import top.fifthlight.touchcontroller.assets.Textures
import top.fifthlight.touchcontroller.common.config.condition.RidingEntityLayerConditionKey
import top.fifthlight.touchcontroller.common.config.condition.SelectEntityLayerConditionKey
import top.fifthlight.touchcontroller.common.ui.entitypicker.EntityPicker
import top.fifthlight.touchcontroller.common.ui.layer.tab.LayerConditionTab
import top.fifthlight.touchcontroller.common.ui.layer.tab.LocalLayerConditionTabContext
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme

object SelectingEntityTab : LayerConditionTab() {
    @Composable
    override fun Icon() {
        Icon(Textures.icon_entity)
    }

    override val name: Identifier
        get() = Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_SELECTING_ENTITY_TYPE

    override val needBorder: Boolean
        get() = false

    @Composable
    override fun Content() {
        val layerConditionTabContext = LocalLayerConditionTabContext.current
        EntityPicker(
            modifier = Modifier.border(LocalTouchControllerTheme.current.borderBackgroundDark),
            onEntityChosen = {
                layerConditionTabContext.onConditionAdded(SelectEntityLayerConditionKey(it))
            },
        )
    }
}
