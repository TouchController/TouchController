package top.fifthlight.blazesdl.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vulkan.VulkanInstance;
import org.lwjgl.PointerBuffer;
import org.lwjgl.sdl.SDLVulkan;
import org.lwjgl.vulkan.KHRPortabilityEnumeration;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(VulkanInstance.class)
public class VulkanInstanceMixin {
    @Shadow
    @Final
    private Set<String> enabledExtensions;

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFWVulkan;glfwGetRequiredInstanceExtensions()Lorg/lwjgl/PointerBuffer;"))
    private PointerBuffer getRequiredInstanceExtensions() {
        return SDLVulkan.SDL_Vulkan_GetInstanceExtensions();
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/MemoryStack;callocPointer(I)Lorg/lwjgl/PointerBuffer;", ordinal = 1))
    private void removePortabilityIfNotExists(int debugVerbosity, boolean wantsDebugLabels, boolean validation, CallbackInfo ci, @Local(name = "availableExtensions") Set<String> availableExtensions) {
        // SDL will always put VK_KHR_portability_enumeration, so we remove it if it is not supported: https://github.com/libsdl-org/SDL/issues/15021
        if (!availableExtensions.contains(KHRPortabilityEnumeration.VK_KHR_PORTABILITY_ENUMERATION_EXTENSION_NAME)) {
            enabledExtensions.remove(KHRPortabilityEnumeration.VK_KHR_PORTABILITY_ENUMERATION_EXTENSION_NAME);
        }
    }

}
