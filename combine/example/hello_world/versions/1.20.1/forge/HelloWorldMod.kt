package top.fifthlight.combine.example.helloworld.v1_21_1

import net.minecraft.client.gui.screens.Screen
import net.minecraftforge.client.ConfigScreenHandler
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.screen.ScreenFactoryFactory
import top.fifthlight.combine.example.helloworld.common.HelloWorld

@Mod("combine_hello_world")
class HelloWorldMod(private val fmlContext: FMLJavaModLoadingContext) {
    init {
        fmlContext.modEventBus.addListener(::onClientSetup)
    }

    private fun onClientSetup(event: FMLClientSetupEvent) {
        fmlContext.registerExtensionPoint(
            ConfigScreenHandler.ConfigScreenFactory::class.java
        ) {
            ConfigScreenHandler.ConfigScreenFactory { _, parent ->
                ScreenFactoryFactory.of().getScreen(
                    parent = parent,
                    renderBackground = true,
                    title = Text.literal("Hello world"),
                ) {
                    HelloWorld()
                } as Screen
            }
        }
    }
}
