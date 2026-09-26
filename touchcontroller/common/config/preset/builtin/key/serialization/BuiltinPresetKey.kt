/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.config.preset.builtin.key.serialization

import com.ubertob.kondor.json.*
import com.ubertob.kondor.json.jsonnode.FieldsValues
import com.ubertob.kondor.json.jsonnode.NodePath
import kotlinx.collections.immutable.toPersistentList
import top.fifthlight.touchcontroller.assets.texture.set.BuiltInTextureSets
import top.fifthlight.touchcontroller.common.assets.serialization.JTextureSet
import top.fifthlight.touchcontroller.common.config.preset.builtin.key.BuiltinPresetKey
import top.fifthlight.touchcontroller.common.control.builtin.serialization.JBuiltInWidget
import top.fifthlight.touchcontroller.common.serialization.enumByName
import top.fifthlight.touchcontroller.common.serialization.objectConverter
import top.fifthlight.touchcontroller.common.serialization.sealedByName

val JTouchGestureControlStyle = objectConverter(BuiltinPresetKey.ControlStyle.TouchGesture)

object JSplitControlsControlStyle : JObj<BuiltinPresetKey.ControlStyle.SplitControls>() {
    val buttonInteraction by JFieldMaybe(BuiltinPresetKey.ControlStyle.SplitControls::buttonInteraction, JBoolean)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) =
        BuiltinPresetKey.ControlStyle.SplitControls(buttonInteraction = (+buttonInteraction) ?: true)
}

val JControlStyle = sealedByName {
    "touch_gesture" encodes subtype<BuiltinPresetKey.ControlStyle.TouchGesture>(JTouchGestureControlStyle)
    "split_controls" encodes subtype<BuiltinPresetKey.ControlStyle.SplitControls>(JSplitControlsControlStyle)
}

val JSprintButtonLocation = enumByName<BuiltinPresetKey.SprintButtonLocation> {
    "none" encodes BuiltinPresetKey.SprintButtonLocation.NONE
    "right_top" encodes BuiltinPresetKey.SprintButtonLocation.RIGHT_TOP
    "right" encodes BuiltinPresetKey.SprintButtonLocation.RIGHT
}

object JDpadMoveMethod : JObj<BuiltinPresetKey.MoveMethod.Dpad>() {
    val swapJumpAndSneak by JFieldMaybe(BuiltinPresetKey.MoveMethod.Dpad::swapJumpAndSneak, JBoolean)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) =
        BuiltinPresetKey.MoveMethod.Dpad(swapJumpAndSneak = (+swapJumpAndSneak) ?: false)
}

object JJoystickMoveMethod : JObj<BuiltinPresetKey.MoveMethod.Joystick>() {
    val triggerSprint by JFieldMaybe(BuiltinPresetKey.MoveMethod.Joystick::triggerSprint, JBoolean)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) =
        BuiltinPresetKey.MoveMethod.Joystick(triggerSprint = (+triggerSprint) ?: false)
}

val JMoveMethod = sealedByName {
    "dpad" encodes subtype<BuiltinPresetKey.MoveMethod.Dpad>(JDpadMoveMethod)
    "joystick" encodes subtype<BuiltinPresetKey.MoveMethod.Joystick>(JJoystickMoveMethod)
}

object JTopBarConfig : JObj<BuiltinPresetKey.TopBarConfig>() {
    val widgets by JFieldMaybe(
        { config: BuiltinPresetKey.TopBarConfig -> config.widgets },
        JList(JBuiltInWidget),
    )

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = BuiltinPresetKey.TopBarConfig(
        widgets = (+widgets)?.toPersistentList(),
    )
}

object JBuiltinPresetKey : JObj<BuiltinPresetKey>() {
    val texture_set by JFieldMaybe(BuiltinPresetKey::textureSet, JTextureSet)
    val control_style by JFieldMaybe(BuiltinPresetKey::controlStyle, JControlStyle)
    val move_method by JFieldMaybe(BuiltinPresetKey::moveMethod, JMoveMethod)
    val sprint_button_location by JFieldMaybe(BuiltinPresetKey::sprintButtonLocation, JSprintButtonLocation)
    val opacity by JFieldMaybe(BuiltinPresetKey::opacity, JFloat)
    val scale by JFieldMaybe(BuiltinPresetKey::scale, JFloat)
    val top_bar by JFieldMaybe(BuiltinPresetKey::topBar, JTopBarConfig)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = BuiltinPresetKey(
        textureSet = (+texture_set) ?: BuiltInTextureSets.classic,
        controlStyle = (+control_style) ?: BuiltinPresetKey.ControlStyle.TouchGesture,
        moveMethod = (+move_method) ?: BuiltinPresetKey.MoveMethod.Dpad(),
        sprintButtonLocation = (+sprint_button_location) ?: BuiltinPresetKey.SprintButtonLocation.NONE,
        opacity = (+opacity) ?: .6f,
        scale = (+scale) ?: 1f,
        topBar = (+top_bar) ?: BuiltinPresetKey.TopBarConfig(),
    )
}
