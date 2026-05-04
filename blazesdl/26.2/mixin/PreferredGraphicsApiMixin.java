package top.fifthlight.blazesdl.mixin;

import com.mojang.blaze3d.opengl.GlBackend;
import com.mojang.blaze3d.vulkan.VulkanBackend;
import net.minecraft.client.PreferredGraphicsApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import top.fifthlight.blazesdl.SDLGlBackend;
import top.fifthlight.blazesdl.SDLVulkanBackend;

@Mixin(PreferredGraphicsApi.class)
public class PreferredGraphicsApiMixin {
    @SuppressWarnings("LoggerInitializedWithForeignClass")
    @Unique
    private static final Logger blazesdl$LOGGER = LoggerFactory.getLogger(PreferredGraphicsApi.class);

    @Redirect(method = "getBackendsToTry", at = @At(value = "NEW", target = "Lcom/mojang/blaze3d/opengl/GlBackend;"))
    private GlBackend replaceGlBackend() {
        blazesdl$LOGGER.info("BlazeSDL: Replacing GlBackend to SDLGlBackend!");
        return new SDLGlBackend();
    }

    @Redirect(method = "getBackendsToTry", at = @At(value = "NEW", target = "Lcom/mojang/blaze3d/vulkan/VulkanBackend;"))
    private VulkanBackend replaceVulkanBackend() {
        blazesdl$LOGGER.info("BlazeSDL: Replacing VulkanBackend to SDLVulkanBackend!");
        return new SDLVulkanBackend();
    }
}
