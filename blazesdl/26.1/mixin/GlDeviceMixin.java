/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.blazesdl.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.opengl.GlDevice;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.sdl.SDLVideo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import top.fifthlight.blazesdl.SDLGlDevice;

@Mixin(GlDevice.class)
public abstract class GlDeviceMixin {
    @Unique
    private SDLGlDevice blazesdl$getSdlDevice() {
        if ((Object) this instanceof SDLGlDevice device) {
            return device;
        } else {
            return null;
        }
    }

    @WrapOperation(
            method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwMakeContextCurrent(J)V"))
    private void wrapMakeContextCurrent(long window, Operation<Void> operation) {
        var sdlDevice = blazesdl$getSdlDevice();
        if (sdlDevice != null) {
            // Context is already set as current
            GL.create(SDLVideo::SDL_GL_GetProcAddress);
        } else {
            operation.call(window);
        }
    }

    @WrapOperation(
            method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetWindowSizeLimits(JIIII)V"))
    private void wrapSetWindowSizeLimits(long window, int minw, int minh, int maxw, int maxh, Operation<Void> operation) {
        var sdlDevice = blazesdl$getSdlDevice();
        if (sdlDevice != null) {
            var sdlMinW = (minw == GLFW.GLFW_DONT_CARE) ? 0 : minw;
            var sdlMinH = (minh == GLFW.GLFW_DONT_CARE) ? 0 : minh;
            var sdlMaxW = (maxw == GLFW.GLFW_DONT_CARE) ? 0 : maxw;
            var sdlMaxH = (maxh == GLFW.GLFW_DONT_CARE) ? 0 : maxh;
            SDLVideo.SDL_SetWindowMinimumSize(window, sdlMinW, sdlMinH);
            SDLVideo.SDL_SetWindowMaximumSize(window, sdlMaxW, sdlMaxH);
        } else {
            operation.call(window, minw, minh, maxw, maxh);
        }
    }

    @WrapOperation(
            method = "setVsync", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSwapInterval(I)V"))
    private void wrapSwapInterval(int interval, Operation<Void> operation) {
        if (blazesdl$getSdlDevice() != null) {
            SDLVideo.SDL_GL_SetSwapInterval(interval);
        } else {
            operation.call(interval);
        }
    }

    @WrapOperation(
            method = "presentFrame", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSwapBuffers(J)V"))
    private void wrapSwapBuffers(long window, Operation<Void> operation) {
        if (blazesdl$getSdlDevice() != null) {
            SDLVideo.SDL_GL_SwapWindow(window);
        } else {
            operation.call(window);
        }
    }

    @WrapOperation(method = "getImplementationInformation",
            at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwGetCurrentContext()J"))
    private long wrapGetCurrentContext(Operation<Long> operation) {
        if (blazesdl$getSdlDevice() != null) {
            return SDLVideo.SDL_GL_GetCurrentContext();
        } else {
            return operation.call();
        }
    }
}
