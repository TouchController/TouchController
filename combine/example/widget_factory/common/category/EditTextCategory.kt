package top.fifthlight.combine.example.widgetfactory.common.category

import androidx.compose.runtime.*
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.placement.fillMaxWidth
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.widget.EditText
import top.fifthlight.combine.widget.Text

object EditTextCategory : WidgetCategory() {
    override val name: String
        get() = "EditText"

    @Composable
    override fun Interface() {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4),
        ) {
            Text("EditText")

            run {
                var value by remember { mutableStateOf("Normal") }
                EditText(
                    value = value,
                    onValueChanged = { value = it },
                )
            }

            run {
                var value by remember { mutableStateOf("") }
                EditText(
                    value = value,
                    placeholder = Text.literal("Placeholder"),
                    onValueChanged = { value = it },
                )
            }
        }
    }
}
