package top.fifthlight.blazesdl.mixin;

import com.mojang.blaze3d.opengl.GlDevice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GlDevice.class)
public class GLDeviceMixin {
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwMakeContextCurrent(J)V"))
    private void wrapMakeContextCurrent(long window) {
        // no-op, because is made current previously
    }
}
