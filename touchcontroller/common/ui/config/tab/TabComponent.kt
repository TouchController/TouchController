/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab

import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import top.fifthlight.touchcontroller.common.config.data.ControlConfig
import top.fifthlight.touchcontroller.common.config.data.DebugConfig
import top.fifthlight.touchcontroller.common.config.data.RegularConfig
import top.fifthlight.touchcontroller.common.config.data.TouchRingConfig
import top.fifthlight.touchcontroller.common.config.platform.SdlPlatformConfig
import top.fifthlight.touchcontroller.common.ui.config.tab.about.component.AboutTabComponent
import top.fifthlight.touchcontroller.common.ui.config.tab.item.component.ItemTabComponent
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.component.CustomControlLayoutTabComponent
import top.fifthlight.touchcontroller.common.ui.config.tab.layout.preset.component.ManageControlPresetsTabComponent
import top.fifthlight.touchcontroller.common.ui.config.tab.status.component.StatusTabComponent
import top.fifthlight.touchcontroller.common.ui.state.ImmutableValue

sealed interface TabComponent {
    companion object {
        private val defaultShowTabUi = ImmutableValue(true)
    }

    val onReset: (() -> Unit)?
        get() = null

    val showTabUi: Value<Boolean>
        get() = defaultShowTabUi

    data class Sdl(
        val config: Value<SdlPlatformConfig>,
        val onConfigChanged: (SdlPlatformConfig) -> Unit,
    ) : TabComponent {
        override val onReset: () -> Unit
            get() = { onConfigChanged(SdlPlatformConfig()) }
    }

    data class About(val component: AboutTabComponent) : TabComponent {
        override val showTabUi = component.childStack.map { it.items.size <= 1 }
    }

    data class Status(val component: StatusTabComponent) : TabComponent {
        override val onReset: () -> Unit
            get() = component::resetConfig
    }

    data class ManageControlPresets(val component: ManageControlPresetsTabComponent) : TabComponent

    data class CustomControlLayout(val component: CustomControlLayoutTabComponent) : TabComponent

    data class Regular(
        val config: Value<RegularConfig>,
        val onConfigChanged: (RegularConfig) -> Unit,
    ) : TabComponent {
        override val onReset: () -> Unit
            get() = { onConfigChanged(RegularConfig()) }
    }

    data class Control(
        val config: Value<ControlConfig>,
        val onConfigChanged: (ControlConfig) -> Unit,
    ) : TabComponent {
        override val onReset: () -> Unit
            get() = { onConfigChanged(ControlConfig()) }
    }

    data class TouchRing(
        val config: Value<TouchRingConfig>,
        val onConfigChanged: (TouchRingConfig) -> Unit,
    ) : TabComponent {
        override val onReset: () -> Unit
            get() = { onConfigChanged(TouchRingConfig()) }
    }

    data class Debug(
        val config: Value<DebugConfig>,
        val onConfigChanged: (DebugConfig) -> Unit,
    ) : TabComponent {
        override val onReset: () -> Unit
            get() = { onConfigChanged(DebugConfig()) }
    }

    data class ItemList(
        val component: ItemTabComponent,
    ) : TabComponent {
        override val showTabUi = component.childStack.map { it.items.size <= 1 }
        override val onReset: () -> Unit
            get() = component::resetConfig
    }
}
