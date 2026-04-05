/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.api.text

import top.fifthlight.touchcontroller.api.v1.text.Text
import top.fifthlight.combine.data.Text as CombineText
import top.fifthlight.combine.data.TextFactory as CombineTextFactory

sealed interface ApiText : Text {
    val text: CombineText

    data class Literal(val string: String) : ApiText {
        override val text = CombineText.literal(string)
    }

    data class Translatable(val id: String) : ApiText {
        override val text = CombineTextFactory.of(id)
    }

    data class Raw(val inner: CombineText): ApiText {
        override val text = inner
    }
}

val Text.text
    get() = (this as ApiText).text
