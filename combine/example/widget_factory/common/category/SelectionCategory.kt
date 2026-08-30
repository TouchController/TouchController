package top.fifthlight.combine.example.widgetfactory.common.category

import androidx.compose.runtime.Composable
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.placement.fillMaxWidth
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.core.widget.layout.FlowRow
import top.fifthlight.combine.widget.CheckBoxItem
import top.fifthlight.combine.widget.RadioBoxItem
import top.fifthlight.combine.widget.Text

object SelectionCategory : WidgetCategory() {
    override val name: String
        get() = "Selection"

    @Composable
    override fun Interface() {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4),
        ) {
            Text("Checkbox")

            FlowRow(horizontalSpacing = 4) {
                CheckBoxItem(
                    value = false,
                    onValueChanged = {},
                ) {
                    Text("Normal Unchecked")
                }

                CheckBoxItem(
                    value = true,
                    onValueChanged = {},
                ) {
                    Text("Normal Checked")
                }

                CheckBoxItem(
                    value = false,
                    enabled = false,
                    onValueChanged = {},
                ) {
                    Text("Disabled Unchecked")
                }

                CheckBoxItem(
                    value = true,
                    enabled = false,
                    onValueChanged = {},
                ) {
                    Text("Disabled Checked")
                }
            }

            Text("Radio")

            FlowRow(horizontalSpacing = 4) {
                RadioBoxItem(
                    value = false,
                    onValueChanged = {},
                ) {
                    Text("Normal Unchecked")
                }

                RadioBoxItem(
                    value = true,
                    onValueChanged = {},
                ) {
                    Text("Normal Checked")
                }

                RadioBoxItem(
                    value = false,
                    enabled = false,
                    onValueChanged = {},
                ) {
                    Text("Disabled Unchecked")
                }

                RadioBoxItem(
                    value = true,
                    enabled = false,
                    onValueChanged = {},
                ) {
                    Text("Disabled Checked")
                }
            }
        }
    }
}
