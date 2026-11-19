/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.tab

import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.layers.component.LayersTabComponent
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.presets.component.PresetsTabComponent
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.widgets.component.WidgetsTabComponent
import top.fifthlight.touchcontroller.common.ui.state.ImmutableValue

sealed interface CustomTabComponent {
    companion object {
        private val defaultShowTabUi = ImmutableValue(true)
    }
    val showTabUi: Value<Boolean>
        get() = defaultShowTabUi

    data object Properties : CustomTabComponent

    data class Widgets(val component: WidgetsTabComponent) : CustomTabComponent

    data class Layers(val component: LayersTabComponent) : CustomTabComponent {
        override val showTabUi = component.childStack.map { it.items.size <= 1 }
    }

    data class Presets(val component: PresetsTabComponent) : CustomTabComponent {
        override val showTabUi = component.childStack.map { it.items.size <= 1 }
    }
}
