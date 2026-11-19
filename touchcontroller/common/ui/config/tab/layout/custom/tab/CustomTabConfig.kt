/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.config.tab.layout.custom.tab

import androidx.compose.runtime.Composable
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import top.fifthlight.combine.widget.Icon
import top.fifthlight.touchcontroller.assets.texture.Textures

sealed class CustomTabConfig {
    @Composable
    abstract fun Icon()

    data object Properties : CustomTabConfig() {
        @Composable
        override fun Icon() {
            Icon(Textures.icon_properties)
        }
    }

    data object Widgets : CustomTabConfig() {
        @Composable
        override fun Icon() {
            Icon(Textures.icon_widget)
        }
    }

    data object Layers : CustomTabConfig() {
        @Composable
        override fun Icon() {
            Icon(Textures.icon_layer)
        }
    }

    data object Presets : CustomTabConfig() {
        @Composable
        override fun Icon() {
            Icon(Textures.icon_preset)
        }
    }

    companion object {
        val all: PersistentList<CustomTabConfig>
            get() = persistentListOf(Properties, Widgets, Layers, Presets)
    }
}
