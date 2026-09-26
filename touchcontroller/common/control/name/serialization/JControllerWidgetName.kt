/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.control.name.serialization

import com.ubertob.kondor.json.JObj
import com.ubertob.kondor.json.jsonnode.FieldsValues
import com.ubertob.kondor.json.jsonnode.NodePath
import com.ubertob.kondor.json.str
import top.fifthlight.touchcontroller.common.control.name.ControllerWidgetName
import top.fifthlight.touchcontroller.common.serialization.JIdentifier
import top.fifthlight.touchcontroller.common.serialization.sealedByName

object JTranslatableName : JObj<ControllerWidgetName.Translatable>() {
    val identifier by str(JIdentifier, ControllerWidgetName.Translatable::identifier)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = ControllerWidgetName.Translatable(+identifier)
}

object JTranslatableStringName : JObj<ControllerWidgetName.TranslatableString>() {
    val identifier by str(ControllerWidgetName.TranslatableString::identifier)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = ControllerWidgetName.TranslatableString(+identifier)
}

object JLiteralName : JObj<ControllerWidgetName.Literal>() {
    val string by str(ControllerWidgetName.Literal::string)

    override fun FieldsValues.deserializeOrThrow(path: NodePath) = ControllerWidgetName.Literal(+string)
}

val JControllerWidgetName = sealedByName {
    "translatable" encodes subtype<ControllerWidgetName.Translatable>(JTranslatableName)
    "translatableString" encodes subtype<ControllerWidgetName.TranslatableString>(JTranslatableStringName)
    "literal" encodes subtype<ControllerWidgetName.Literal>(JLiteralName)
}
