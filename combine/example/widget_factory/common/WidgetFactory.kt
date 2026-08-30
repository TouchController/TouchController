package top.fifthlight.combine.example.widgetfactory.common

import androidx.compose.runtime.*
import kotlinx.collections.immutable.toPersistentList
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.placement.fillMaxSize
import top.fifthlight.combine.core.modifier.placement.fillMaxWidth
import top.fifthlight.combine.core.modifier.placement.padding
import top.fifthlight.combine.core.modifier.scroll.verticalScroll
import top.fifthlight.combine.core.widget.layout.Box
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.core.widget.layout.Row
import top.fifthlight.combine.example.widgetfactory.common.category.*
import top.fifthlight.combine.example.widgetfactory.common.category.blackstone.BlackstoneAlertDialogCategory
import top.fifthlight.combine.theme.Theme
import top.fifthlight.combine.theme.blackstone.BlackstoneTheme
import top.fifthlight.combine.theme.invoke
import top.fifthlight.combine.theme.oreui.OreUITheme
import top.fifthlight.combine.theme.vanilla.VanillaTheme
import top.fifthlight.combine.widget.DropdownItemList
import top.fifthlight.combine.widget.Select
import top.fifthlight.combine.widget.Text

val categories = listOf(
    ButtonCategory,
    SelectionCategory,
    EditTextCategory,
    SliderCategory,
    SwitchCategory,
    BlackstoneAlertDialogCategory,
)

@Composable
private fun CategorySelect(
    modifier: Modifier = Modifier,
    categories: List<WidgetCategory>,
    value: WidgetCategory,
    onValueChanged: (WidgetCategory) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Select(
        modifier = modifier,
        expanded = expanded,
        onExpandedChanged = { expanded = it },
        dropDownContent = {
            DropdownItemList(
                modifier = Modifier.verticalScroll(),
                onItemSelected = { expanded = false },
                items = categories.map { category ->
                    Text.literal(category.name) to { onValueChanged(category) }
                }.toPersistentList()
            )
        },
    ) {
        Text(value.name)
    }
}

val themes = listOf(
    "Blackstone" to BlackstoneTheme,
    "OreUI" to OreUITheme,
    "Vanilla" to VanillaTheme,
    "Base" to Theme()
)

@Composable
private fun ThemeSelect(
    modifier: Modifier = Modifier,
    themes: List<Pair<String, Theme>>,
    value: Pair<String, Theme>,
    onValueChanged: (Pair<String, Theme>) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Select(
        modifier = modifier,
        expanded = expanded,
        onExpandedChanged = { expanded = it },
        dropDownContent = {
            DropdownItemList(
                modifier = Modifier.verticalScroll(),
                onItemSelected = { expanded = false },
                items = themes.map { (name, theme) ->
                    Text.literal(name) to { onValueChanged(name to theme) }
                }.toPersistentList()
            )
        },
    ) {
        Text(value.first)
    }
}

@Composable
fun WidgetFactory() {
    var themePair by remember { mutableStateOf(themes.first()) }
    val (_, theme) = themePair
    theme {
        Column(
            modifier = Modifier
                .padding(4)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4),
        ) {
            var category by remember { mutableStateOf(categories.first()) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4),
            ) {
                ThemeSelect(
                    themes = themes,
                    value = themePair,
                    onValueChanged = { themePair = it },
                )

                CategorySelect(
                    categories = categories.filter { it.themes?.contains(theme) ?: true },
                    value = category,
                    onValueChanged = { category = it },
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                category.Interface()
            }
        }
    }
}
