/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.common.util.name

import androidx.compose.runtime.Immutable
import top.fifthlight.combine.core.data.Identifier
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.data.TextFactory

@Immutable
sealed class TranslatableName {
    data class Translatable(val identifier: Identifier) : TranslatableName()

    data class TranslatableString(val identifier: String) : TranslatableName()

    data class Literal(val string: String) : TranslatableName()

    fun getText() = when (this) {
        is Translatable -> Text.translatable(identifier)
        is TranslatableString -> TextFactory.of(identifier)
        is Literal -> Text.literal(string)
    }

    fun asString() = getText().string
}
