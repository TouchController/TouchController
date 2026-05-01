package top.fifthlight.combine.example.widgetfactory.common

import androidx.compose.runtime.*
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toPersistentList
import top.fifthlight.combine.data.TextFactoryFactory
import top.fifthlight.combine.data.TextStyle
import top.fifthlight.combine.layout.Alignment
import top.fifthlight.combine.layout.Arrangement
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.drawing.background
import top.fifthlight.combine.modifier.placement.*
import top.fifthlight.combine.modifier.scroll.verticalScroll
import top.fifthlight.combine.paint.Color
import top.fifthlight.combine.theme.LocalTheme
import top.fifthlight.combine.theme.blackstone.BlackstoneTheme
import top.fifthlight.combine.theme.invoke
import top.fifthlight.combine.theme.oreui.OreUITheme
import top.fifthlight.combine.theme.vanilla.VanillaTheme
import top.fifthlight.combine.widget.layout.Box
import top.fifthlight.combine.widget.layout.Column
import top.fifthlight.combine.widget.layout.Row
import top.fifthlight.combine.widget.layout.Spacer
import top.fifthlight.combine.widget.ui.*

private val themes =
    persistentListOf(
        "Blackstone" to BlackstoneTheme,
        "Ore UI" to OreUITheme,
        "Vanilla" to VanillaTheme,
    )

private val categories =
    persistentListOf(
        "Buttons" to
                @Composable {
                    Column(verticalArrangement = Arrangement.spacedBy(4)) {
                        Text(text = "Basic Buttons")

                        Row(horizontalArrangement = Arrangement.spacedBy(4)) {
                            Button(onClick = {}) { Text("Normal Button") }
                            Button(onClick = {}, enabled = false) { Text("Disabled Button") }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4)) {
                            GuideButton(onClick = {}) { Text("Guide Button") }
                            WarningButton(onClick = {}) { Text("Warning Button") }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4)) {
                            TextButton(onClick = {}) { Text("Text Button") }
                            IconButton(onClick = {}) { Text("Icon") }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4)) {
                            CheckBoxButton(checked = true, onClick = {}) { Text("Checked Button") }
                            CheckBoxButton(checked = false, onClick = {}) { Text("Unchecked Button") }
                        }
                    }
                },
        "Text Inputs" to
                @Composable {
                    Column(verticalArrangement = Arrangement.spacedBy(4)) {
                        Text(text = "Text Input Fields")

                        Row(horizontalArrangement = Arrangement.spacedBy(4)) {
                            var text by remember { mutableStateOf("") }
                            EditText(
                                modifier = Modifier.width(150),
                                value = text,
                                onValueChanged = { text = it },
                                placeholder = TextFactoryFactory.of().literal("Enter text..."),
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4)) {
                            var text by remember { mutableStateOf("Sample text") }
                            EditText(
                                modifier = Modifier.width(150),
                                value = text,
                                onValueChanged = { text = it },
                            )
                        }
                    }
                },
        "Selection" to
                @Composable {
                    Column(verticalArrangement = Arrangement.spacedBy(4)) {
                        Text(text = "Selection Widgets")

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            var checked by remember { mutableStateOf(true) }
                            CheckBox(value = checked, onValueChanged = { checked = it })
                            Text("Checked")
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            var checked by remember { mutableStateOf(false) }
                            CheckBox(value = checked, onValueChanged = { checked = it })
                            Text("Unchecked")
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            var switch by remember { mutableStateOf(true) }
                            Switch(value = switch, onValueChanged = { switch = it })
                            Text("Switch On")
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            var switch by remember { mutableStateOf(false) }
                            Switch(value = switch, onValueChanged = { switch = it })
                            Text("Switch Off")
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            var radio by remember { mutableStateOf(true) }
                            Radio(value = radio, onValueChanged = { radio = it })
                            Text("Radio Selected")
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            var radio by remember { mutableStateOf(false) }
                            Radio(value = radio, onValueChanged = { radio = it })
                            Text("Radio Unselected")
                        }
                    }
                },
        "Sliders" to
                @Composable {
                    Column(verticalArrangement = Arrangement.spacedBy(4)) {
                        Text(text = "Slider Widgets")

                        Column(verticalArrangement = Arrangement.spacedBy(4)) {
                            Text("Float Slider (0.0 - 1.0)")

                            var slider by remember { mutableStateOf(0.0f) }
                            Slider(
                                modifier = Modifier.width(200),
                                range = 0.0f..1.0f,
                                value = slider,
                                onValueChanged = { slider = it },
                            )
                            Text("Value: ${slider.toString().take(4)}")
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4)) {
                            Text("Float Slider (0.5)")

                            var slider by remember { mutableStateOf(0.5f) }
                            Slider(
                                modifier = Modifier.width(200),
                                range = 0.0f..1.0f,
                                value = slider,
                                onValueChanged = { slider = it },
                            )
                            Text("Value: ${slider.toString().take(4)}")
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4)) {
                            Text("Int Slider (0 - 100)")

                            var slider by remember { mutableStateOf(50) }
                            IntSlider(
                                modifier = Modifier.width(200),
                                range = 0..100,
                                value = slider,
                                onValueChanged = { slider = it },
                            )
                            Text("Value: $slider")
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4)) {
                            Text("Int Slider (25)")

                            var slider by remember { mutableStateOf(25) }
                            IntSlider(
                                modifier = Modifier.width(200),
                                range = 0..100,
                                value = slider,
                                onValueChanged = { slider = it },
                            )
                            Text("Value: $slider")
                        }
                    }
                },
        "Dropdowns" to
                @Composable {
                    Column(verticalArrangement = Arrangement.spacedBy(4)) {
                        Text(text = "Dropdown Selectors")

                        Row(horizontalArrangement = Arrangement.spacedBy(4)) {
                            var expanded by remember { mutableStateOf(false) }
                            var selected by remember { mutableStateOf("Option 1") }
                            Select(
                                expanded = expanded,
                                onExpandedChanged = { expanded = it },
                                dropDownContent = {
                                    DropdownItemList(
                                        items =
                                            listOf("Option 1", "Option 2", "Option 3")
                                                .map { option ->
                                                    TextFactoryFactory.of().literal(option) to
                                                            {
                                                                selected = option
                                                                expanded = false
                                                            }
                                                }
                                                .toPersistentList()
                                    )
                                },
                            ) {
                                Text(selected)
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4)) {
                            var expanded by remember { mutableStateOf(false) }
                            var selected by remember { mutableStateOf("Apple") }
                            Select(
                                expanded = expanded,
                                onExpandedChanged = { expanded = it },
                                dropDownContent = {
                                    DropdownItemList(
                                        items =
                                            listOf("Apple", "Banana", "Cherry", "Durian")
                                                .map { option ->
                                                    TextFactoryFactory.of().literal(option) to
                                                            {
                                                                selected = option
                                                                expanded = false
                                                            }
                                                }
                                                .toPersistentList()
                                    )
                                },
                            ) {
                                Text(selected)
                            }
                        }
                    }
                },
        "Color Picker" to
                @Composable {
                    Column(verticalArrangement = Arrangement.spacedBy(4)) {
                        Text(text = "Color Picker")

                        var color by remember { mutableStateOf(Color(255, 128, 64)) }
                        ColorPicker(value = color, onValueChanged = { color = it })

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(modifier = Modifier.size(50, 50), alignment = Alignment.Center) {
                                Box(modifier = Modifier.size(40, 40).background(color))
                            }
                            Column {
                                Text("R: ${color.r}")
                                Text("G: ${color.g}")
                                Text("B: ${color.b}")
                                Text("A: ${color.a}")
                            }
                        }
                    }
                },
        "Dialogs" to
                @Composable {
                    Column(verticalArrangement = Arrangement.spacedBy(4)) {
                        Text(text = "Dialog Examples")

                        var showDialog by remember { mutableStateOf(false) }
                        Button(onClick = { showDialog = true }) { Text("Show Alert Dialog") }

                        AlertDialog(
                            visible = showDialog,
                            onDismissRequest = { showDialog = false },
                            title = { Text("Alert Dialog") },
                            content = {
                                Column(verticalArrangement = Arrangement.spacedBy(4)) {
                                    Text("This is an example of an alert dialog.")
                                    Text("Dialogs can show important information")
                                    Text("or require user confirmation.")
                                }
                            },
                            action = {
                                Row(horizontalArrangement = Arrangement.spacedBy(4)) {
                                    Button(onClick = { showDialog = false }) { Text("Cancel") }
                                    GuideButton(onClick = { showDialog = false }) { Text("Confirm") }
                                }
                            },
                        )
                    }
                },
        "Layout Examples" to
                @Composable {
                    Column(verticalArrangement = Arrangement.spacedBy(4)) {
                        Text(text = "Layout Widgets")

                        Column(verticalArrangement = Arrangement.spacedBy(4)) {
                            Text("Box with Alignment")
                            Box(
                                modifier = Modifier.size(150, 80).background(Color(64, 64, 64)),
                                alignment = Alignment.Center,
                            ) {
                                Text("Centered")
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4)) {
                            Text("Box with TopLeft Alignment")
                            Box(
                                modifier = Modifier.size(150, 80).background(Color(64, 64, 64)),
                                alignment = Alignment.TopLeft,
                            ) {
                                Text("TopLeft")
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4)) {
                            Text("Row with Arrangement")
                            Row(
                                modifier = Modifier.width(300),
                                horizontalArrangement = Arrangement.spacedBy(4),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("Item 1")
                                Text("Item 2")
                                Text("Item 3")
                                Spacer(modifier = Modifier.weight(1f))
                                Text("Right")
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4)) {
                            Text("Column with Arrangement")
                            Column(
                                modifier = Modifier.height(100),
                                verticalArrangement = Arrangement.spacedBy(4),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text("Top")
                                Spacer(modifier = Modifier.weight(1f))
                                Text("Middle")
                                Spacer(modifier = Modifier.weight(1f))
                                Text("Bottom")
                            }
                        }
                    }
                },
        "Display Widgets" to
                @Composable {
                    Column(verticalArrangement = Arrangement.spacedBy(4)) {
                        Text(text = "Display Widgets")

                        Column(verticalArrangement = Arrangement.spacedBy(4)) {
                            Text("Text Styles")
                            Text(text = "Normal Text")
                            Text(text = "Bold Text", textStyle = TextStyle(bold = true))
                            Text(text = "Italic Text", textStyle = TextStyle(italic = true))
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4)) {
                            Text("Colored Text")
                            Row(horizontalArrangement = Arrangement.spacedBy(4)) {
                                Text(text = "Red", color = Color(255, 100, 100))
                                Text(text = "Green", color = Color(100, 255, 100))
                                Text(text = "Blue", color = Color(100, 100, 255))
                                Text(text = "Yellow", color = Color(255, 255, 100))
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4)) {
                            Text("Links")
                            Link(text = "Click here", onClick = {})
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4)) {
                            Text("Icons")
                            Row(horizontalArrangement = Arrangement.spacedBy(4)) {
                                Box(modifier = Modifier.size(24, 24).background(Color(200, 200, 200))) {
                                    Text("A")
                                }
                                Box(modifier = Modifier.size(24, 24).background(Color(200, 200, 200))) {
                                    Text("B")
                                }
                                Box(modifier = Modifier.size(24, 24).background(Color(200, 200, 200))) {
                                    Text("C")
                                }
                            }
                        }
                    }
                },
    )

private val categoryInterfaces = categories.toMap().toImmutableMap()

@Composable
fun WidgetFactory() {
    var themeIndex by remember { mutableStateOf(0) }
    var selectedCategory by remember { mutableStateOf(categories.first().first) }

    themes[themeIndex].second {
        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(4)) {
            Column(
                modifier = Modifier.padding(4).fillMaxHeight().width(100),
                verticalArrangement = Arrangement.spacedBy(4),
            ) {
                Text("Category")

                categories.forEach { (name, _) ->
                    val isSelected = selectedCategory == name
                    val buttonModifier = Modifier.fillMaxWidth()
                    Button(
                        modifier = buttonModifier,
                        drawableSet =
                            if (isSelected) LocalTheme.current.drawables.guideButton
                            else LocalTheme.current.drawables.button,
                        onClick = { selectedCategory = name },
                    ) {
                        Text(name)
                    }
                }
            }

            Column(
                modifier = Modifier.padding(4).fillMaxHeight().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8),
                ) {
                    Text("Theme:")
                    var expanded by remember { mutableStateOf(false) }
                    Select(
                        expanded = expanded,
                        onExpandedChanged = { expanded = it },
                        dropDownContent = {
                            DropdownItemList(
                                items =
                                    themes
                                        .mapIndexed { index, (name, _) ->
                                            TextFactoryFactory.of().literal(name) to
                                                    {
                                                        themeIndex = index
                                                        expanded = false
                                                    }
                                        }
                                        .toPersistentList()
                            )
                        },
                    ) {
                        Text(themes[themeIndex].first)
                    }
                }

                Column(modifier = Modifier.verticalScroll().fillMaxWidth().weight(1f)) {
                    categoryInterfaces[selectedCategory]?.invoke()
                }
            }
        }
    }
}
