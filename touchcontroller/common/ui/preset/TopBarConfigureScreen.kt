package top.fifthlight.touchcontroller.common.ui.component

import androidx.compose.runtime.*
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.collections.immutable.PersistentList
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.layout.Alignment
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.drawing.background
import top.fifthlight.combine.core.modifier.drawing.border
import top.fifthlight.combine.core.modifier.placement.*
import top.fifthlight.combine.core.modifier.scroll.verticalScroll
import top.fifthlight.combine.core.widget.layout.Box
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.core.widget.layout.Row
import top.fifthlight.combine.widget.ui.Icon
import top.fifthlight.combine.widget.ui.IconButton
import top.fifthlight.combine.widget.ui.Text
import top.fifthlight.touchcontroller.assets.lang.Texts
import top.fifthlight.touchcontroller.assets.texture.Textures
import top.fifthlight.touchcontroller.common.assets.TextureSet
import top.fifthlight.touchcontroller.common.config.preset.topbar.TopBarWidgets
import top.fifthlight.touchcontroller.common.control.builtin.BuiltInWidget
import top.fifthlight.touchcontroller.common.ui.control.AutoScaleControllerWidget
import top.fifthlight.touchcontroller.common.ui.control.ControllerWidget
import top.fifthlight.touchcontroller.common.ui.theme.LocalTouchControllerTheme
import top.fifthlight.touchcontroller.common.ui.widget.ListButton
import top.fifthlight.touchcontroller.common.ui.widget.Scaffold
import top.fifthlight.touchcontroller.common.ui.widget.navigation.AppBar
import top.fifthlight.touchcontroller.common.ui.widget.navigation.BackButton

data class TopBarConfigureScreen(
    val textureSet: TextureSet,
    val value: PersistentList<BuiltInWidget>,
    val onValueChanged: (PersistentList<BuiltInWidget>) -> Unit,
) : Screen {
    @Composable
    override fun Content() {
        var selectedWidgets by remember { mutableStateOf(value) }

        val availableWidgets = remember(selectedWidgets, textureSet) {
            TopBarWidgets.registry.values().filter { widget ->
                widget !in selectedWidgets && (widget.hidden?.invoke(textureSet) != true)
            }
        }

        Scaffold(
            topBar = {
                AppBar(
                    modifier = Modifier.fillMaxWidth(),
                    leading = {
                        BackButton(screenName = Text.translatable(Texts.SCREEN_TOP_BAR_CUSTOMIZE_TITLE))
                    },
                )
            },
        ) { modifier ->
            Row(
                modifier = Modifier
                    .background(LocalTouchControllerTheme.current.background)
                    .then(modifier)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(3f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        for (widget in selectedWidgets) {
                            ControllerWidget(widget = widget[textureSet])
                        }
                    }
                    if (selectedWidgets.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .padding(4)
                                .verticalScroll()
                                .border(LocalTouchControllerTheme.current.borderBackgroundDark)
                                .fillMaxWidth()
                                .weight(7f),
                        ) {
                            for ((index, widget) in selectedWidgets.withIndex()) {
                                val widget = widget[textureSet]
                                Row(
                                    modifier = Modifier.height(IntrinsicSize.Min),
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .border(LocalTouchControllerTheme.current.listButtonDrawablesUnchecked.normal)
                                            .weight(1f)
                                            .fillMaxHeight(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4),
                                    ) {
                                        AutoScaleControllerWidget(
                                            modifier = Modifier.size(16),
                                            widget = widget,
                                        )
                                        Text(
                                            modifier = Modifier.weight(1f),
                                            text = widget.name.getText(),
                                        )
                                    }
                                    IconButton(
                                        modifier = Modifier
                                            .width(24)
                                            .fillMaxHeight(),
                                        onClick = {
                                            if (index == 0) return@IconButton
                                            val item = selectedWidgets[index]
                                            val newList = selectedWidgets.removeAt(index).add(index - 1, item)
                                            selectedWidgets = newList
                                            onValueChanged(newList)
                                        },
                                    ) {
                                        Icon(Textures.icon_up)
                                    }
                                    IconButton(
                                        modifier = Modifier
                                            .width(24)
                                            .fillMaxHeight(),
                                        onClick = {
                                            if (index >= selectedWidgets.size - 1) return@IconButton
                                            val item = selectedWidgets[index]
                                            val newList = selectedWidgets.removeAt(index).add(index + 1, item)
                                            selectedWidgets = newList
                                            onValueChanged(newList)
                                        },
                                    ) {
                                        Icon(Textures.icon_down)
                                    }
                                    IconButton(
                                        modifier = Modifier
                                            .width(24)
                                            .fillMaxHeight(),
                                        onClick = {
                                            val newList = selectedWidgets.removeAt(index)
                                            selectedWidgets = newList
                                            onValueChanged(newList)
                                        },
                                    ) {
                                        Icon(Textures.icon_delete)
                                    }
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .border(LocalTouchControllerTheme.current.borderBackgroundDark)
                                .fillMaxWidth()
                                .weight(7f),
                            alignment = Alignment.Center,
                        ) {
                            Text(Text.translatable(Texts.SCREEN_TOP_BAR_CUSTOMIZE_NO_WIDGET_SELECTED))
                        }
                    }
                }
                if (availableWidgets.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .padding(4)
                            .verticalScroll()
                            .border(LocalTouchControllerTheme.current.borderBackgroundDark)
                            .fillMaxHeight()
                            .weight(1f),
                    ) {
                        for (widgetItem in availableWidgets) {
                            val widget = widgetItem[textureSet]
                            ListButton(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    val newList = selectedWidgets.add(widgetItem)
                                    selectedWidgets = newList
                                    onValueChanged(newList)
                                },
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4),
                                ) {
                                    AutoScaleControllerWidget(
                                        modifier = Modifier.size(16),
                                        widget = widget,
                                    )
                                    Text(
                                        modifier = Modifier.weight(1f),
                                        text = widget.name.getText(),
                                    )
                                    Text(Text.translatable(Texts.SCREEN_TOP_BAR_CUSTOMIZE_LIST_ADD))
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .border(LocalTouchControllerTheme.current.borderBackgroundDark)
                            .fillMaxHeight()
                            .weight(1f),
                        alignment = Alignment.Center,
                    ) {
                        Text(Text.translatable(Texts.SCREEN_TOP_BAR_CUSTOMIZE_NO_MORE_WIDGET_TO_ADD))
                    }
                }
            }
        }
    }
}
