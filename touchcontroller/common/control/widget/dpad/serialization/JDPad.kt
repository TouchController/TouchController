/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.control.widget.dpad.serialization

import com.ubertob.kondor.json.*
import com.ubertob.kondor.json.jsonnode.FieldsValues
import com.ubertob.kondor.json.jsonnode.NodePath
import top.fifthlight.data.IntOffset
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.assets.texture.set.BuiltInTextureSets
import top.fifthlight.touchcontroller.common.assets.serialization.JTextureSet
import top.fifthlight.touchcontroller.common.control.action.ButtonTrigger
import top.fifthlight.touchcontroller.common.control.action.serialization.JButtonTrigger
import top.fifthlight.touchcontroller.common.control.name.ControllerWidgetName
import top.fifthlight.touchcontroller.common.control.name.serialization.JControllerWidgetName
import top.fifthlight.touchcontroller.common.control.texture.serialization.JTextureCoordinate
import top.fifthlight.touchcontroller.common.control.widget.dpad.DPad
import top.fifthlight.touchcontroller.common.control.widget.dpad.DPadExtraButton
import top.fifthlight.touchcontroller.common.layout.align.Align
import top.fifthlight.touchcontroller.common.layout.align.serialization.JAlign
import top.fifthlight.touchcontroller.common.serialization.JIntOffset
import top.fifthlight.touchcontroller.common.serialization.JUuid
import top.fifthlight.touchcontroller.common.serialization.objectConverter
import top.fifthlight.touchcontroller.common.serialization.sealedByName

val JSameDPadActiveTexture = objectConverter(DPadExtraButton.ActiveTexture.Same)

val JGrayDPadActiveTexture = objectConverter(DPadExtraButton.ActiveTexture.Gray)

object JTextureDPadActiveTexture : JObj<DPadExtraButton.ActiveTexture.Texture>() {
    val texture by JFieldMaybe(DPadExtraButton.ActiveTexture.Texture::texture, JTextureCoordinate)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = DPadExtraButton.ActiveTexture.Texture(
        texture = (+texture) ?: DPadExtraButton.ActiveTexture.Texture().texture,
    )
}

val JDPadActiveTexture = sealedByName {
    "same" encodes subtype<DPadExtraButton.ActiveTexture.Same>(JSameDPadActiveTexture)
    "gray" encodes subtype<DPadExtraButton.ActiveTexture.Gray>(JGrayDPadActiveTexture)
    "texture" encodes subtype<DPadExtraButton.ActiveTexture.Texture>(JTextureDPadActiveTexture)
}

object JDPadButtonInfo : JObj<DPadExtraButton.ButtonInfo>() {
    val size by JFieldMaybe(DPadExtraButton.ButtonInfo::size, JInt)
    val texture by JFieldMaybe(DPadExtraButton.ButtonInfo::texture, JTextureCoordinate)
    val activeTexture by JFieldMaybe(DPadExtraButton.ButtonInfo::activeTexture, JDPadActiveTexture)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = DPadExtraButton.ButtonInfo(
        size = (+size) ?: 22,
        texture = (+texture) ?: DPadExtraButton.ButtonInfo().texture,
        activeTexture = (+activeTexture) ?: DPadExtraButton.ActiveTexture.Gray,
    )
}

val JDPadNoneExtraButton = objectConverter(DPadExtraButton.None)

object JDPadNormalExtraButton : JObj<DPadExtraButton.Normal>() {
    val trigger by JFieldMaybe(DPadExtraButton.Normal::trigger, JButtonTrigger)
    val info by JFieldMaybe(DPadExtraButton.Normal::info, JDPadButtonInfo)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = DPadExtraButton.Normal(
        trigger = (+trigger) ?: ButtonTrigger(),
        info = (+info) ?: DPadExtraButton.ButtonInfo(),
    )
}

object JDPadSwipeExtraButton : JObj<DPadExtraButton.Swipe>() {
    val trigger by JFieldMaybe(DPadExtraButton.Swipe::trigger, JButtonTrigger)
    val info by JFieldMaybe(DPadExtraButton.Swipe::info, JDPadButtonInfo)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = DPadExtraButton.Swipe(
        trigger = (+trigger) ?: ButtonTrigger(),
        info = (+info) ?: DPadExtraButton.ButtonInfo(),
    )
}

object JDPadSwipeLockingExtraButton : JObj<DPadExtraButton.SwipeLocking>() {
    val press by JFieldMaybe(DPadExtraButton.SwipeLocking::press, JString)
    val info by JFieldMaybe(DPadExtraButton.SwipeLocking::info, JDPadButtonInfo)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = DPadExtraButton.SwipeLocking(
        press = +press,
        info = (+info) ?: DPadExtraButton.ButtonInfo(),
    )
}

val JDPadExtraButton = sealedByName {
    "none" encodes subtype<DPadExtraButton.None>(JDPadNoneExtraButton)
    "normal" encodes subtype<DPadExtraButton.Normal>(JDPadNormalExtraButton)
    "swipe" encodes subtype<DPadExtraButton.Swipe>(JDPadSwipeExtraButton)
    "swipe_locking" encodes subtype<DPadExtraButton.SwipeLocking>(JDPadSwipeLockingExtraButton)
}

object JDPad : JObj<DPad>() {
    val textureSet by JFieldMaybe(DPad::textureSet, JTextureSet)
    val size by JFieldMaybe(DPad::size, JFloat)
    val padding by JFieldMaybe(DPad::padding, JInt)
    val extraButton by obj(JDPadExtraButton, DPad::extraButton)
    val showBackwardButton by JFieldMaybe(DPad::showBackwardButton, JBoolean)
    val idForward by str(JUuid, DPad::idForward)
    val idBackward by str(JUuid, DPad::idBackward)
    val idLeft by str(JUuid, DPad::idLeft)
    val idRight by str(JUuid, DPad::idRight)
    val idLeftForward by str(JUuid, DPad::idLeftForward)
    val idRightForward by str(JUuid, DPad::idRightForward)
    val idLeftBackward by str(JUuid, DPad::idLeftBackward)
    val idRightBackward by str(JUuid, DPad::idRightBackward)
    val idExtraButton by str(JUuid, DPad::idExtraButton)
    val id by str(JUuid, DPad::id)
    val name by JFieldMaybe(DPad::name, JControllerWidgetName)
    val align by JFieldMaybe(DPad::align, JAlign)
    val autoAlign by JFieldMaybe(DPad::autoAlign, JBoolean)
    val offset by JFieldMaybe(DPad::offset, JIntOffset)
    val opacity by JFieldMaybe(DPad::opacity, JFloat)
    val lockMoving by JFieldMaybe(DPad::lockMoving, JBoolean)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = DPad(
        textureSet = (+textureSet) ?: BuiltInTextureSets.classic,
        size = (+size) ?: 2f,
        padding = (+padding) ?: 4,
        extraButton = +extraButton,
        showBackwardButton = (+showBackwardButton) ?: false,
        idForward = +idForward,
        idBackward = +idBackward,
        idLeft = +idLeft,
        idRight = +idRight,
        idLeftForward = +idLeftForward,
        idRightForward = +idRightForward,
        idLeftBackward = +idLeftBackward,
        idRightBackward = +idRightBackward,
        idExtraButton = +idExtraButton,
        id = +id,
        name = (+name) ?: ControllerWidgetName.Translatable(Texts.WIDGET_DPAD_NAME),
        align = (+align) ?: Align.CENTER_CENTER,
        autoAlign = (+autoAlign) ?: true,
        offset = (+offset) ?: IntOffset.ZERO,
        opacity = (+opacity) ?: 1f,
        lockMoving = (+lockMoving) ?: false,
    )
}
