package top.fifthlight.combine.example.widgetfactory.v26_4

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import net.minecraft.client.gui.screens.Screen
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.screen.ScreenFactoryFactory
import top.fifthlight.combine.example.widgetfactory.common.WidgetFactory

class WidgetFactoryModMenuImpl : ModMenuApi {
    override fun getModConfigScreenFactory() = ConfigScreenFactory { parent ->
        ScreenFactoryFactory.of().getScreen(
            parent = parent,
            renderBackground = true,
            title = Text.literal("Widget Factory"),
        ) {
            WidgetFactory()
        } as Screen
    }
}
