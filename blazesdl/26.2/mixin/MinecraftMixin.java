/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.blazesdl.mixin;

import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.TextInputManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;
import com.mojang.blaze3d.systems.BackendCreationException;
import com.mojang.blaze3d.systems.GpuBackend;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.sdl.SDLInit;
import org.lwjgl.sdl.SDLVideo;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.fifthlight.blazesdl.SDLError;
import top.fifthlight.blazesdl.SDLTextInputManager;
import top.fifthlight.blazesdl.SDLWindow;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "<init>", at = @At(value = "HEAD"))
    private static void useExplicitInit(GameConfig gameConfig, CallbackInfo ci) {
        Configuration.OPENGL_EXPLICIT_INIT.set(true);
    }

    @Redirect(method = "<init>", at = @At(value = "NEW", target = "Lcom/mojang/blaze3d/platform/Window;"))
    private Window createWindow(WindowEventHandler eventHandler, DisplayData displayData,
                                String fullscreenVideoModeString, String title, GpuBackend backend) throws BackendCreationException {
        return new SDLWindow(eventHandler, displayData, fullscreenVideoModeString, title, backend);
    }

    @Redirect(method = "<init>", at = @At(value = "NEW", target = "Lcom/mojang/blaze3d/platform/TextInputManager;"))
    private TextInputManager createTextInputManager(Window window) {
        return new SDLTextInputManager((SDLWindow) window);
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwShowWindow(J)V"))
    private void showWindow(long handle) {
        if (!SDLVideo.SDL_ShowWindow(handle)) {
            throw SDLError.handleError("SDL_ShowWindow");
        }
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetWindowSizeLimits(JIIII)V"))
    private void setWindowSizeLimit(long window, int minw, int minh, int maxw, int maxh) {
        var sdlMinW = (minw == GLFW.GLFW_DONT_CARE) ? 0 : minw;
        var sdlMinH = (minh == GLFW.GLFW_DONT_CARE) ? 0 : minh;
        var sdlMaxW = (maxw == GLFW.GLFW_DONT_CARE) ? 0 : maxw;
        var sdlMaxH = (maxh == GLFW.GLFW_DONT_CARE) ? 0 : maxh;
        SDLVideo.SDL_SetWindowMinimumSize(window, sdlMinW, sdlMinH);
        SDLVideo.SDL_SetWindowMaximumSize(window, sdlMaxW, sdlMaxH);
    }

    @Redirect(method = "renderFrame", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwGetFramebufferSize(J[I[I)V"))
    private void getFrameBufferSize(long handle, int[] width, int[] height) {
        try (var stack = MemoryStack.stackPush()) {
            var w = stack.ints(-1);
            var h = stack.ints(-1);
            if (!SDLVideo.SDL_GetWindowSizeInPixels(handle, w, h)) {
                throw SDLError.handleError("SDL_GetWindowSizeInPixels");
            }
            width[0] = w.get();
            height[0] = h.get();
        }
    }

    @Redirect(method = "close", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwTerminate()V"))
    private void onGlfwTerminate() {
        SDLInit.SDL_Quit();
    }
}
