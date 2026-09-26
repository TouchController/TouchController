/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.control.widget.joystick.serialization

import com.ubertob.kondor.json.*
import com.ubertob.kondor.json.jsonnode.FieldsValues
import com.ubertob.kondor.json.jsonnode.NodePath
import top.fifthlight.data.IntOffset
import top.fifthlight.touchcontroller.assets.texture.set.BuiltInTextureSets
import top.fifthlight.touchcontroller.common.assets.serialization.JTextureSet
import top.fifthlight.touchcontroller.common.control.name.serialization.JControllerWidgetName
import top.fifthlight.touchcontroller.common.control.widget.joystick.Joystick
import top.fifthlight.touchcontroller.common.layout.align.Align
import top.fifthlight.touchcontroller.common.layout.align.serialization.JAlign
import top.fifthlight.touchcontroller.common.serialization.JIntOffset
import top.fifthlight.touchcontroller.common.serialization.JUuid

object JJoystick : JObj<Joystick>() {
    val textureSet by JFieldMaybe(Joystick::textureSet, JTextureSet)
    val size by JFieldMaybe(Joystick::size, JFloat)
    val stickSize by JFieldMaybe(Joystick::stickSize, JFloat)
    val triggerSprint by JFieldMaybe(Joystick::triggerSprint, JBoolean)
    val increaseOpacityWhenActive by JFieldMaybe(Joystick::increaseOpacityWhenActive, JBoolean)
    val id by str(JUuid, Joystick::id)
    val name by JFieldMaybe(Joystick::name, JControllerWidgetName)
    val align by JFieldMaybe(Joystick::align, JAlign)
    val autoAlign by JFieldMaybe(Joystick::autoAlign, JBoolean)
    val offset by JFieldMaybe(Joystick::offset, JIntOffset)
    val opacity by JFieldMaybe(Joystick::opacity, JFloat)
    val lockMoving by JFieldMaybe(Joystick::lockMoving, JBoolean)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = Joystick(
        textureSet = (+textureSet) ?: BuiltInTextureSets.new,
        size = (+size) ?: 1.5f,
        stickSize = (+stickSize) ?: 1.15f,
        triggerSprint = (+triggerSprint) ?: false,
        increaseOpacityWhenActive = (+increaseOpacityWhenActive) ?: true,
        id = +id,
        name = (+name) ?: Joystick().name,
        align = (+align) ?: Align.CENTER_CENTER,
        autoAlign = (+autoAlign) ?: true,
        offset = (+offset) ?: IntOffset.ZERO,
        opacity = (+opacity) ?: 1f,
        lockMoving = (+lockMoving) ?: false,
    )
}
