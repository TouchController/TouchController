package top.fifthlight.blazesdl.mixin;

import com.mojang.blaze3d.vulkan.VulkanInstance;
import org.lwjgl.PointerBuffer;
import org.lwjgl.sdl.SDLVulkan;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(VulkanInstance.class)
public class VulkanInstanceMixin {
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFWVulkan;glfwGetRequiredInstanceExtensions()Lorg/lwjgl/PointerBuffer;"))
    private PointerBuffer getRequiredInstanceExtensions() {
        return SDLVulkan.SDL_Vulkan_GetInstanceExtensions();
    }
}
