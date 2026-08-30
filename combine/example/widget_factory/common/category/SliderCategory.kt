package top.fifthlight.combine.example.widgetfactory.common.category

import androidx.compose.runtime.*
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.placement.fillMaxWidth
import top.fifthlight.combine.core.modifier.placement.width
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.widget.IntSlider
import top.fifthlight.combine.widget.Slider
import top.fifthlight.combine.widget.Text

object SliderCategory : WidgetCategory() {
    override val name: String
        get() = "Sliders"

    @Composable
    override fun Interface() {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4),
        ) {
            Text("Slider")

            run {
                var value by remember { mutableStateOf(0.5f) }
                Slider(
                    modifier = Modifier.width(200),
                    value = value,
                    range = 0f..1f,
                    onValueChanged = { value = it },
                )
            }

            Text("IntSlider")

            run {
                var value by remember { mutableStateOf(5) }
                IntSlider(
                    modifier = Modifier.width(200),
                    value = value,
                    range = 0..10,
                    onValueChanged = { value = it },
                )
            }
        }
    }
}
