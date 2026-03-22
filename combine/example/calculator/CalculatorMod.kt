package top.fifthlight.combine.example.calculator

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.gui.screens.Screen
import org.lwjgl.glfw.GLFW
import top.fifthlight.combine.data.TextFactoryFactory
import top.fifthlight.combine.layout.Alignment
import top.fifthlight.combine.layout.Arrangement
import top.fifthlight.combine.modifier.Modifier
import top.fifthlight.combine.modifier.drawing.background
import top.fifthlight.combine.modifier.drawing.innerLine
import top.fifthlight.combine.modifier.placement.*
import top.fifthlight.combine.paint.Colors
import top.fifthlight.combine.screen.ScreenFactoryFactory
import top.fifthlight.combine.theme.blackstone.BlackstoneTheme
import top.fifthlight.combine.theme.invoke
import top.fifthlight.combine.theme.oreui.OreUITheme
import top.fifthlight.combine.theme.vanilla.VanillaTheme
import top.fifthlight.combine.widget.layout.Box
import top.fifthlight.combine.widget.layout.Column
import top.fifthlight.combine.widget.layout.Row
import top.fifthlight.combine.widget.ui.Button
import top.fifthlight.combine.widget.ui.Text

class CalculatorMod: ClientModInitializer, ModMenuApi {
    private val keyMapping = KeyMapping("combine_calculator", GLFW.GLFW_KEY_H, "combine_example")

    private val themes = listOf(
        "Blackstone" to BlackstoneTheme,
        "Ore UI" to OreUITheme,
        "Vanilla" to VanillaTheme,
    )

    private fun createScreen(parent: Screen? = null) = ScreenFactoryFactory.of().getScreen(
        parent = parent,
        title = TextFactoryFactory.of().literal("Combine Calculator")
    ) {
        var themeIndex by remember { mutableStateOf(0) }
        themes[themeIndex].second {
            var display by remember { mutableStateOf("0") }
            var operand1 by remember { mutableStateOf<Double?>(null) }
            var operator by remember { mutableStateOf<String?>(null) }
            var shouldResetDisplay by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier.fillMaxSize(),
                alignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier.width(IntrinsicSize.Min),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4),
                ) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            themeIndex = (themeIndex + 1) % themes.size
                        },
                    ) {
                        Text("Theme: ${themes[themeIndex].first}")
                    }

                    Row(
                        modifier = Modifier
                            .padding(4)
                            .background(Colors.BLACK)
                            .innerLine(color = Colors.WHITE)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(display)
                        operator?.let { Text(it) }
                    }

                    val buttons = listOf(
                        listOf("7", "8", "9", "/"),
                        listOf("4", "5", "6", "*"),
                        listOf("1", "2", "3", "-"),
                        listOf("0", "C", "=", "+"),
                    )

                    buttons.forEach { rowSymbols ->
                        Row(horizontalArrangement = Arrangement.spacedBy(4)) {
                            rowSymbols.forEach { symbol ->
                                Button(
                                    modifier = Modifier.size(28, 20),
                                    onClick = {
                                        when (symbol) {
                                            in "0".."9" -> {
                                                if (display == "0" || shouldResetDisplay) {
                                                    display = symbol
                                                    shouldResetDisplay = false
                                                } else {
                                                    display += symbol
                                                }
                                            }
                                            "C" -> {
                                                display = "0"
                                                operand1 = null
                                                operator = null
                                            }
                                            "=" -> {
                                                val val1 = operand1
                                                val val2 = display.toDoubleOrNull()
                                                if (val1 != null && val2 != null && operator != null) {
                                                    val result = when (operator) {
                                                        "+" -> val1 + val2
                                                        "-" -> val1 - val2
                                                        "*" -> val1 * val2
                                                        "/" -> val1 / val2
                                                        else -> val2
                                                    }
                                                    display = result.toString()
                                                }
                                                operand1 = null
                                                operator = null
                                                shouldResetDisplay = true
                                            }
                                            in listOf("+", "-", "*", "/") -> {
                                                operand1 = display.toDoubleOrNull()
                                                operator = symbol
                                                shouldResetDisplay = true
                                            }
                                        }
                                    }
                                ) {
                                    Text(symbol)
                                }
                            }
                        }
                    }
                }
            }
        }
    } as Screen

    override fun getModConfigScreenFactory() = ConfigScreenFactory(::createScreen)

    override fun onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(keyMapping)
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (!keyMapping.isDown) {
                return@register
            }
            if (client.screen != null) {
                return@register
            }
            client.setScreen(createScreen(null))
        }
    }
}
