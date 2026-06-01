/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.blazesdl.mixin;

import com.mojang.blaze3d.platform.MonitorCreator;
import com.mojang.blaze3d.platform.ScreenManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.BackendCreationException;
import com.mojang.blaze3d.systems.GpuBackend;
import org.lwjgl.glfw.*;
import org.lwjgl.sdl.SDLInit;
import org.lwjgl.sdl.SDLPlatform;
import org.lwjgl.sdl.SDLVideo;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.fifthlight.blazesdl.*;

@Mixin(Window.class)
public abstract class WindowMixin {
    @Inject(method = "createGlfwWindow", at = @At("HEAD"), cancellable = true)
    private static void createWindow(int width, int height, String title, long monitor, GpuBackend backend, CallbackInfoReturnable<Long> cir) throws BackendCreationException {
        if (!SDLVideo.SDL_GL_LoadLibrary((String) null)) {
            throw SDLGlBackend.handleError("SDL_GL_LoadLibrary");
        }

        backend.setWindowHints();

        var window = SDLVideo.SDL_CreateWindow(title, width, height, SDLVideo.SDL_WINDOW_OPENGL | SDLVideo.SDL_WINDOW_RESIZABLE | SDLVideo.SDL_WINDOW_HIGH_PIXEL_DENSITY);
        if (window == 0L) {
            throw SDLGlBackend.handleError("SDL_CreateWindow");
        }

        var context = SDLVideo.SDL_GL_CreateContext(window);
        if (context == 0L) {
            throw SDLGlBackend.handleError("SDL_GL_CreateContext");
        }

        cir.setReturnValue(window);
    }

    @Redirect(method = "<init>", at = @At(value = "NEW", target = "Lcom/mojang/blaze3d/platform/ScreenManager;"))
    private ScreenManager wrapScreenManager(MonitorCreator monitorCreator) {
        return new SDLScreenManager(SDLMonitor::new);
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwGetPrimaryMonitor()J"))
    private long getPrimaryMonitor() {
        return SDLVideo.SDL_GetPrimaryDisplay();
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwGetWindowPos(J[I[I)V"))
    private void glfwGetWindowPos(long window, int[] xpos, int[] ypos) {
        try (var stack = MemoryStack.stackPush()) {
            var x = stack.ints(-1);
            var y = stack.ints(-1);
            if (!SDLVideo.SDL_GetWindowPosition(window, x, y)) {
                throw SDLError.handleError("SDL_GetWindowPosition");
            }
            xpos[0] = x.get();
            ypos[0] = y.get();
        }
    }


    @Redirect(method = "refreshFramebufferSize", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwGetFramebufferSize(J[I[I)V"))
    private void glfwGetFramebufferSize(long window, int[] width, int[] height) {
        try (var stack = MemoryStack.stackPush()) {
            var w = stack.ints(-1);
            var h = stack.ints(-1);
            if (!SDLVideo.SDL_GetWindowSizeInPixels(window, w, h)) {
                throw SDLError.handleError("SDL_GetWindowSizeInPixels");
            }
            width[0] = w.get();
            height[0] = h.get();
        }
    }

    @Redirect(method = "close", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwDestroyWindow(J)V"))
    private void glfwDestroyWindow(long window) {
        SDLVideo.SDL_DestroyWindow(window);
    }

    @Redirect(method = "close", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwTerminate()V"))
    private void glfwTerminate() {
        SDLInit.SDL_Quit();
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetFramebufferSizeCallback(JLorg/lwjgl/glfw/GLFWFramebufferSizeCallbackI;)Lorg/lwjgl/glfw/GLFWFramebufferSizeCallback;"))
    private GLFWFramebufferSizeCallback cancelSetFramebufferSizeCallback(long window, GLFWFramebufferSizeCallbackI cbfun) {
        EventCallback.onFramebufferResize = cbfun;
        return null;
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetWindowPosCallback(JLorg/lwjgl/glfw/GLFWWindowPosCallbackI;)Lorg/lwjgl/glfw/GLFWWindowPosCallback;"))
    private GLFWWindowPosCallback cancelSetWindowPosCallback(long window, GLFWWindowPosCallbackI cbfun) {
        EventCallback.onWindowMove = cbfun;
        return null;
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetWindowSizeCallback(JLorg/lwjgl/glfw/GLFWWindowSizeCallbackI;)Lorg/lwjgl/glfw/GLFWWindowSizeCallback;"))
    private GLFWWindowSizeCallback cancelSetWindowSizeCallback(long window, GLFWWindowSizeCallbackI cbfun) {
        EventCallback.onWindowResize = cbfun;
        return null;
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetWindowFocusCallback(JLorg/lwjgl/glfw/GLFWWindowFocusCallbackI;)Lorg/lwjgl/glfw/GLFWWindowFocusCallback;"))
    private GLFWWindowFocusCallback cancelSetWindowFocusCallback(long window, GLFWWindowFocusCallbackI cbfun) {
        EventCallback.onWindowFocus = cbfun;
        return null;
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetCursorEnterCallback(JLorg/lwjgl/glfw/GLFWCursorEnterCallbackI;)Lorg/lwjgl/glfw/GLFWCursorEnterCallback;"))
    private GLFWCursorEnterCallback cancelSetCursorEnterCallback(long window, GLFWCursorEnterCallbackI cbfun) {
        EventCallback.onWindowCursorEnter = cbfun;
        return null;
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetWindowIconifyCallback(JLorg/lwjgl/glfw/GLFWWindowIconifyCallbackI;)Lorg/lwjgl/glfw/GLFWWindowIconifyCallback;"))
    private GLFWWindowIconifyCallback cancelSetWindowIconifyCallback(long window, GLFWWindowIconifyCallbackI cbfun) {
        EventCallback.onWindowIconify = cbfun;
        return null;
    }

    @Inject(method = "getPlatform", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwGetPlatform()I"), cancellable = true)
    private static void overrideGetPlatform(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(SDLPlatform.SDL_GetPlatform());
    }
}
