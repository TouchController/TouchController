package top.fifthlight.blazesdl.mixin;

import com.mojang.blaze3d.vulkan.VulkanGpuSurface;
import org.lwjgl.sdl.SDLVulkan;
import org.lwjgl.vulkan.VkAllocationCallbacks;
import org.lwjgl.vulkan.VkInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.LongBuffer;

@Mixin(VulkanGpuSurface.class)
public class VulkanSurfaceMixin {
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFWVulkan;glfwCreateWindowSurface(Lorg/lwjgl/vulkan/VkInstance;JLorg/lwjgl/vulkan/VkAllocationCallbacks;Ljava/nio/LongBuffer;)I"))
    private int createWindowSurface(VkInstance instance, long window, VkAllocationCallbacks allocator, LongBuffer surface) {
        if (SDLVulkan.SDL_Vulkan_CreateSurface(window, instance, allocator, surface)) {
            return 0;
        } else {
            return -1;
        }
    }
}
