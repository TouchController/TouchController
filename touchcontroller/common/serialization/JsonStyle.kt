/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.serialization

import com.ubertob.kondor.json.JsonStyle

val jsonStyle = JsonStyle(
    appendFieldSeparator = JsonStyle.Companion::comma,
    appendValueSeparator = JsonStyle.Companion::colonSpace,
    appendNewline = JsonStyle.Companion::appendNewLineIndent,
    sortedObjectFields = false,
    explicitNulls = false,
)
