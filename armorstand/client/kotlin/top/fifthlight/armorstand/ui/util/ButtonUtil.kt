package top.fifthlight.armorstand.ui.util

import net.minecraft.client.gui.components.Button
import net.minecraft.network.chat.Component
import top.fifthlight.armorstand.ui.screen.BaseArmorStandScreen

fun <T : BaseArmorStandScreen<T>> BaseArmorStandScreen<T>.autoWidthButton(
    text: Component,
    padding: Int = 8,
    onPress: Button.OnPress,
): Button = Button.builder(text, onPress)
    .width(currentMinecraft.font.width(text) + padding * 2)
    .build()
