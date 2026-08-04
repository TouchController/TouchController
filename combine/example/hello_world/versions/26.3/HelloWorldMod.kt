package top.fifthlight.combine.example.helloworld.v26_3

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.gui.screens.Screen
import net.minecraft.resources.Identifier
import org.lwjgl.sdl.SDLKeycode
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.screen.ScreenFactoryFactory
import top.fifthlight.combine.example.helloworld.common.HelloWorld

class HelloWorldMod : ClientModInitializer, ModMenuApi {
    companion object {
        private val keyCategory =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("combine", "example"))
        private val keyMapping = KeyMapping("combine_hello_world", SDLKeycode.SDLK_H, keyCategory)
    }

    override fun getModConfigScreenFactory() = ConfigScreenFactory { parent ->
        ScreenFactoryFactory.of().getScreen(
            parent = parent,
            renderBackground = true,
            title = Text.literal("Hello world"),
        ) {
            HelloWorld()
        } as Screen
    }

    override fun onInitializeClient() {
        KeyMappingHelper.registerKeyMapping(keyMapping)
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (!keyMapping.isDown) {
                return@register
            }
            if (client.gui.screen() != null) {
                return@register
            }
            client.gui.setScreen(
                ScreenFactoryFactory.of().getScreen(
                    parent = null,
                    title = Text.literal("Hello world"),
                ) {
                    HelloWorld()
                } as Screen
            )
        }
    }
}
