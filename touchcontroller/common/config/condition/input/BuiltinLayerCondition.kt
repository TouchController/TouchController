/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.config.condition.input

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import top.fifthlight.combine.core.data.Identifier
import top.fifthlight.touchcontroller.assets.lang.Texts

@Serializable
enum class BuiltinLayerCondition(val text: Identifier) {
    @SerialName("swimming")
    SWIMMING(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_SWIMMING),

    @SerialName("underwater")
    UNDERWATER(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_UNDERWATER),

    @SerialName("flying")
    FLYING(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_FLYING),

    @SerialName("can_fly")
    CAN_FLY(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_CAN_FLY),

    @SerialName("sneaking")
    SNEAKING(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_SNEAKING),

    @SerialName("sprinting")
    SPRINTING(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_SPRINTING),

    @SerialName("on_ground")
    ON_GROUND(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_ON_GROUND),

    @SerialName("no_on_ground")
    NOT_ON_GROUND(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_NOT_ON_GROUND),

    @SerialName("using_item")
    USING_ITEM(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_USING_ITEM),

    @SerialName("riding")
    RIDING(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_RIDING),

    @SerialName("entity_selected")
    ENTITY_SELECTED(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_ENTITY_SELECTED),

    @SerialName("block_selected")
    BLOCK_SELECTED(Texts.SCREEN_CUSTOM_CONTROL_LAYOUT_LAYERS_CONDITIONS_BLOCK_SELECTED),
}
