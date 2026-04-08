package top.fifthlight.blazesdl.mixin;

import com.mojang.blaze3d.vulkan.VulkanBackend;
import org.lwjgl.sdl.SDLVulkan;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(VulkanBackend.class)
public class VulkanBackendMixin {
    @Redirect(method = "createDevice(JLcom/mojang/blaze3d/shaders/ShaderSource;Lcom/mojang/blaze3d/shaders/GpuDebugOptions;)Lcom/mojang/blaze3d/systems/GpuDevice;", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFWVulkan;glfwVulkanSupported()Z"))
    private boolean vulkanSupported() {
        return SDLVulkan.SDL_Vulkan_LoadLibrary((String) null);
    }
}
