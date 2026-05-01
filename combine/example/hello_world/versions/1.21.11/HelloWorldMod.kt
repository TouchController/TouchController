package top.fifthlight.combine.example.helloworld.v1_21_11

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.gui.screens.Screen
import net.minecraft.resources.Identifier
import org.lwjgl.glfw.GLFW
import top.fifthlight.combine.data.TextFactoryFactory
import top.fifthlight.combine.example.helloworld.common.HelloWorld
import top.fifthlight.combine.screen.ScreenFactoryFactory

class HelloWorldMod : ClientModInitializer, ModMenuApi {
    companion object {
        private val keyCategory =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("combine", "example"))
        private val keyMapping = KeyMapping("combine_hello_world", GLFW.GLFW_KEY_H, keyCategory)
    }

    override fun getModConfigScreenFactory() = ConfigScreenFactory { parent ->
        ScreenFactoryFactory.of().getScreen(
            parent = parent,
            title = TextFactoryFactory.of().literal("Hello world"),
        ) {
            HelloWorld()
        } as Screen
    }

    override fun onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(keyMapping)
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (!keyMapping.isDown) {
                return@register
            }
            if (client.screen != null) {
                return@register
            }
            client.setScreen(
                ScreenFactoryFactory.of().getScreen(
                    parent = null,
                    title = TextFactoryFactory.of().literal("Hello world"),
                ) {
                    HelloWorld()
                } as Screen
            )
        }
    }
}
