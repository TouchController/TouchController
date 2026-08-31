package top.fifthlight.combine.example.widgetfactory.common.category

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.drawing.background
import top.fifthlight.combine.core.modifier.drawing.border
import top.fifthlight.combine.core.modifier.placement.fillMaxWidth
import top.fifthlight.combine.core.modifier.scroll.horizontalScroll
import top.fifthlight.combine.core.modifier.scroll.rememberScrollState
import top.fifthlight.combine.core.paint.Colors
import top.fifthlight.combine.core.widget.layout.Box
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.core.widget.layout.Row
import top.fifthlight.combine.widget.Button
import top.fifthlight.combine.widget.Text

object HorizontalScrollCategory : WidgetCategory() {
    override val name: String
        get() = "Horizontal scroll"

    @Composable
    override fun Interface() {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4),
        ) {
            Text("Horizontal scroll")

            val scrollState = rememberScrollState()

            val progress by scrollState.progress.collectAsState()
            val overscroll by scrollState.overscroll.collectAsState()
            Text("Progress: $progress, overscroll: $overscroll")

            Box(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(scrollState = scrollState)
                    .fillMaxWidth()
                    .background(Colors.BLACK)
                    .border(size = 1, color = Colors.WHITE),
            ) {
                Row {
                    repeat(30) {
                        Button(onClick = {}) {
                            Text(text = "$it")
                        }
                    }
                }
            }
        }
    }
}
