package top.fifthlight.blazesdl.mixin;

import com.mojang.blaze3d.platform.NativeLibrariesBootstrap;
import org.lwjgl.sdl.SDL;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Objects;
import java.util.function.Supplier;

@Mixin(NativeLibrariesBootstrap.class)
public abstract class NativeLibrariesBootstrapMixin {
    @Shadow
    private static void loadLibrary(Supplier<String> debugCapture, String name, Runnable loader) {
    }

    @Redirect(method = "loadLibraries", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/NativeLibrariesBootstrap;loadLibrary(Ljava/util/function/Supplier;Ljava/lang/String;Ljava/lang/Runnable;)V", ordinal = 1))
    private static void redirectLoadGlfw(Supplier<String> debugCapture, String name, Runnable loader) {
        loadLibrary(debugCapture, "GLFW", () -> Objects.requireNonNull(SDL.getLibrary(), "SDl"));
    }
}
