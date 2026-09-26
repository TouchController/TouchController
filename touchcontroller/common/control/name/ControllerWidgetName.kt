package top.fifthlight.touchcontroller.common.control.name

import androidx.compose.runtime.Immutable
import top.fifthlight.combine.core.data.Identifier
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.data.TextFactory

@Immutable
sealed class ControllerWidgetName {
    data class Translatable(val identifier: Identifier) : ControllerWidgetName()

    data class TranslatableString(val identifier: String) : ControllerWidgetName()

    data class Literal(val string: String) : ControllerWidgetName()

    fun getText() = when (this) {
        is Translatable -> Text.translatable(identifier)
        is TranslatableString -> TextFactory.of(identifier)
        is Literal -> Text.literal(string)
    }

    fun asString() = getText().string
}
