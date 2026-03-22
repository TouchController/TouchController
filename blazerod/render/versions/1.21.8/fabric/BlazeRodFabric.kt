package top.fifthlight.blazerod.render.version_1_21_8.fabric

import com.mojang.blaze3d.opengl.GlRenderPass
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.minecraft.client.Minecraft
import org.slf4j.LoggerFactory
import top.fifthlight.blazerod.physics.PhysicsInterface
import top.fifthlight.blazerod.render.common.BlazeRod
import top.fifthlight.blazerod.render.api.event.RenderEvents
import top.fifthlight.blazerod.render.common.runtime.uniform.UniformBuffer
import top.fifthlight.blazerod.render.common.debug.*
import top.fifthlight.blazerod.render.version_1_21_8.util.dispatchers.BlockableEventLoopDispatcher
import top.fifthlight.blazerod.render.version_1_21_8.runtime.resource.RenderTexture
import javax.swing.SwingUtilities

object BlazeRodFabric : ClientModInitializer {
    private val LOGGER = LoggerFactory.getLogger(BlazeRodFabric::class.java)

    override fun onInitializeClient() {
        BlazeRod.mainDispatcher = BlockableEventLoopDispatcher(Minecraft.getInstance())

        PhysicsInterface.load()

        if (System.getProperty("blazerod.debug") == "true") {
            BlazeRod.debug = true
            GlRenderPass.VALIDATION = true
            if (System.getProperty("blazerod.debug.gui") == "true") {
                ResourceCountTracker.initialize()
                UniformBufferTracker.initialize()
                System.setProperty("java.awt.headless", "false")
                SwingUtilities.invokeLater {
                    try {
                        ResourceCountTrackerFrame().isVisible = true
                        UniformBufferTrackerFrame().isVisible = true
                    } catch (ex: Exception) {
                        LOGGER.info("Failed to show debug windows", ex)
                    }
                }
            }
        }

        RenderEvents.INITIALIZE_DEVICE.register {
            // Trigger its loading in render thread
            RenderTexture.WHITE_RGBA_TEXTURE
        }

        RenderEvents.FLIP_FRAME.register {
            UniformBuffer.clear()
        }

        ClientLifecycleEvents.CLIENT_STOPPING.register { client ->
            UniformBuffer.close()
        }
    }
}
