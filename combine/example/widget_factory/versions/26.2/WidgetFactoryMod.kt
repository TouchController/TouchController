package top.fifthlight.combine.example.widgetfactory.v26_2

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.gui.screens.Screen
import net.minecraft.resources.Identifier
import org.lwjgl.glfw.GLFW
import top.fifthlight.combine.data.TextFactoryFactory
import top.fifthlight.combine.example.widgetfactory.common.WidgetFactory
import top.fifthlight.combine.screen.ScreenFactoryFactory

class WidgetFactoryMod : ClientModInitializer, ModMenuApi {
    companion object {
        private val keyCategory =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("combine", "example"))
        private val keyMapping = KeyMapping("combine_widget_factory", GLFW.GLFW_KEY_H, keyCategory)
    }

    override fun getModConfigScreenFactory() = ConfigScreenFactory { parent ->
        ScreenFactoryFactory.of().getScreen(
            parent = parent,
            title = TextFactoryFactory.of().literal("Widget Factory"),
        ) {
            WidgetFactory()
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
                    title = TextFactoryFactory.of().literal("Widget Factory"),
                ) {
                    WidgetFactory()
                } as Screen
            )
        }
    }
}
