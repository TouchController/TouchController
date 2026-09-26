/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.serialization

import com.ubertob.kondor.json.JObj
import com.ubertob.kondor.json.jsonnode.FieldsValues
import com.ubertob.kondor.json.jsonnode.NodePath

fun <T : Any> objectConverter(value: T): JObj<T> = object : JObj<T>() {
    override fun FieldsValues.deserializeOrThrow(path: NodePath): T = value
}
