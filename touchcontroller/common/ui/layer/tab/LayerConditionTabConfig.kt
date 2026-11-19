/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.ui.layer.tab

import androidx.compose.runtime.Composable
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import top.fifthlight.combine.core.data.Identifier
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.widget.Icon
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.assets.texture.Textures

sealed class LayerConditionTabConfig(
    private val titleId: Identifier,
    val needBorder: Boolean = true,
) {
    val title: Text
        @Composable
        get() = Text.translatable(titleId)

    @Composable
    abstract fun Icon()

    data object BuiltIn : LayerConditionTabConfig(Texts.SCREEN_LAYER_EDITOR_BUILTIN) {
        @Composable
        override fun Icon() {
            Icon(Textures.icon_motion)
        }
    }

    data object HoldingItem : LayerConditionTabConfig(
        titleId = Texts.SCREEN_LAYER_EDITOR_HOLDING_ITEM,
        needBorder = false,
    ) {
        @Composable
        override fun Icon() {
            Icon(Textures.icon_block)
        }
    }

    data object RidingEntity : LayerConditionTabConfig(
        titleId = Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_RIDING_ENTITY_TYPE,
        needBorder = false,
    ) {
        @Composable
        override fun Icon() {
            Icon(Textures.icon_ride)
        }
    }

    data object SelectingEntity : LayerConditionTabConfig(
        titleId = Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_SELECTING_ENTITY_TYPE,
        needBorder = false,
    ) {
        @Composable
        override fun Icon() {
            Icon(Textures.icon_entity)
        }
    }

    data object Custom : LayerConditionTabConfig(Texts.SCREEN_LAYER_EDITOR_CUSTOM_CONDITION) {
        @Composable
        override fun Icon() {
            Icon(Textures.icon_edit)
        }
    }

    companion object {
        val all: PersistentList<LayerConditionTabConfig>
            get() = persistentListOf(BuiltIn, HoldingItem, RidingEntity, SelectingEntity, Custom)
    }
}
