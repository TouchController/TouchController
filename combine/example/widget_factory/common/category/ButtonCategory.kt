package top.fifthlight.combine.example.widgetfactory.common.category

import androidx.compose.runtime.Composable
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.placement.fillMaxWidth
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.core.widget.layout.FlowRow
import top.fifthlight.combine.widget.Button
import top.fifthlight.combine.widget.GuideButton
import top.fifthlight.combine.widget.Text
import top.fifthlight.combine.widget.WarningButton

object ButtonCategory : WidgetCategory() {
    override val name: String
        get() = "Buttons"

    @Composable
    override fun Interface() {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4),
        ) {
            Text("Button")

            FlowRow(horizontalSpacing = 4) {
                Button(onClick = { }) {
                    Text("Normal")
                }
                Button(
                    onClick = { },
                    enabled = false,
                ) {
                    Text("Disabled")
                }
            }

            FlowRow(horizontalSpacing = 4) {
                GuideButton(onClick = { }) {
                    Text("Guide")
                }
                GuideButton(
                    onClick = { },
                    enabled = false,
                ) {
                    Text("Disabled")
                }
            }

            FlowRow(horizontalSpacing = 4) {
                WarningButton(onClick = { }) {
                    Text("Warning")
                }
                WarningButton(
                    onClick = { },
                    enabled = false,
                ) {
                    Text("Disabled")
                }
            }
        }
    }
}
