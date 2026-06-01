/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.blazesdl.mixin;

import com.mojang.blaze3d.opengl.GlDevice;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.sdl.SDLVideo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GlDevice.class)
public abstract class GlDeviceMixin {
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwMakeContextCurrent(J)V"))
    private void makeContextCurrent(long window) {
        GL.create(SDLVideo::SDL_GL_GetProcAddress);
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetWindowSizeLimits(JIIII)V"))
    private void setWindowSizeLimits(long window, int minw, int minh, int maxw, int maxh) {
        var sdlMinW = (minw == GLFW.GLFW_DONT_CARE) ? 0 : minw;
        var sdlMinH = (minh == GLFW.GLFW_DONT_CARE) ? 0 : minh;
        var sdlMaxW = (maxw == GLFW.GLFW_DONT_CARE) ? 0 : maxw;
        var sdlMaxH = (maxh == GLFW.GLFW_DONT_CARE) ? 0 : maxh;
        SDLVideo.SDL_SetWindowMinimumSize(window, sdlMinW, sdlMinH);
        SDLVideo.SDL_SetWindowMaximumSize(window, sdlMaxW, sdlMaxH);
    }

    @Redirect(method = "setVsync", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSwapInterval(I)V"))
    private void swapInterval(int interval) {
        SDLVideo.SDL_GL_SetSwapInterval(interval);
    }

    @Redirect(method = "presentFrame", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSwapBuffers(J)V"))
    private void swapBuffers(long window) {
        SDLVideo.SDL_GL_SwapWindow(window);
    }

    @Redirect(method = "getImplementationInformation", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwGetCurrentContext()J"))
    private long getCurrentContext() {
        return SDLVideo.SDL_GL_GetCurrentContext();
    }
}
