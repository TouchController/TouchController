package top.fifthlight.blazesdl.mixin;

import org.lwjgl.glfw.GLFWVulkan;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@SuppressWarnings("OverwriteAuthorRequired")
@Mixin(GLFWVulkan.class)
public abstract class GLFWVulkanMixin {
    @Overwrite
    public static boolean glfwVulkanSupported() {
        return true;
    }
}
