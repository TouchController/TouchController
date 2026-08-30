package top.fifthlight.combine.example.widgetfactory.common.category

import androidx.compose.runtime.*
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.placement.fillMaxWidth
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.core.widget.layout.FlowRow
import top.fifthlight.combine.widget.Switch
import top.fifthlight.combine.widget.Text

object SwitchCategory : WidgetCategory() {
    override val name: String
        get() = "Switches"

    @Composable
    override fun Interface() {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4),
        ) {
            Text("Switch")

            FlowRow(horizontalSpacing = 4) {
                run {
                    var value by remember { mutableStateOf(false) }
                    Switch(
                        value = value,
                        onValueChanged = { value = it },
                    )
                }

                run {
                    var value by remember { mutableStateOf(true) }
                    Switch(
                        value = value,
                        onValueChanged = { value = it },
                    )
                }

                Switch(
                    value = false,
                    enabled = false,
                    onValueChanged = {},
                )

                Switch(
                    value = true,
                    enabled = false,
                    onValueChanged = {},
                )
            }
        }
    }
}
