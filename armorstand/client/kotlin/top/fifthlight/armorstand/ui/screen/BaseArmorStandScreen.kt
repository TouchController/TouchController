package top.fifthlight.armorstand.ui.screen

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

abstract class BaseArmorStandScreen<T: BaseArmorStandScreen<T>>(
    protected val parent: Screen? = null,
    title: Component,
): Screen(title) {
    val currentMinecraft: Minecraft
        get() = super.minecraft ?: Minecraft.getInstance()

    override fun onClose() {
        currentMinecraft.setScreen(parent)
    }
}
