package top.fifthlight.blazesdl.mixin;

import com.mojang.blaze3d.vulkan.VulkanPhysicalDevice;
import org.lwjgl.sdl.SDLVulkan;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(VulkanPhysicalDevice.class)
public class VulkanPhysicalDeviceMixin {
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFWVulkan;glfwGetPhysicalDevicePresentationSupport(Lorg/lwjgl/vulkan/VkInstance;Lorg/lwjgl/vulkan/VkPhysicalDevice;I)Z"))
    private boolean getPhysicalDevicePresentationSupport(VkInstance instance, VkPhysicalDevice device, int queuefamily) {
        return SDLVulkan.SDL_Vulkan_GetPresentationSupport(instance, device, queuefamily);
    }
}
