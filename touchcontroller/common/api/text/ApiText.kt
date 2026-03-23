/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.api.text

import top.fifthlight.touchcontroller.api.v1.text.Text
import top.fifthlight.combine.data.Text as CombineText

data class ApiText(val text: CombineText) : Text

val Text.text
    get() = (this as ApiText).text
