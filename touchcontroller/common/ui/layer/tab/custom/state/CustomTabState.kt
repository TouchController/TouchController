package top.fifthlight.touchcontroller.common.ui.layer.tab.custom.state

import top.fifthlight.touchcontroller.common.config.preset.info.CustomCondition

data class CustomTabState(
    val editState: EditState? = null,
) {
    data class EditState(
        val index: Int,
        val name: String? = null,
    ) {
        fun edit(customCondition: CustomCondition) = customCondition.copy(name = name)
    }
}
