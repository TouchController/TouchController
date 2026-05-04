/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.blazesdl.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.*;
import org.lwjgl.sdl.SDLMouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.fifthlight.blazesdl.EventCallback;
import top.fifthlight.blazesdl.SDLUtil;
import top.fifthlight.blazesdl.SDLWindow;

@Mixin(InputConstants.class)
public abstract class InputConstantsMixin {
    @Inject(method = "setupKeyboardCallbacks", at = @At(value = "HEAD"), cancellable = true)
    private static void skipGlfwKeyboardCallbacks(Window window, GLFWKeyCallbackI keyPressCallback, GLFWCharCallbackI charTypedCallback, GLFWPreeditCallbackI preeditCallback, GLFWIMEStatusCallbackI imeStatusCallback, CallbackInfo ci) {
        if (window instanceof SDLWindow) {
            EventCallback.keyPressCallback = keyPressCallback;
            EventCallback.charTypedCallback = charTypedCallback;
            EventCallback.preeditCallback = preeditCallback;
            EventCallback.imeStatusCallback = imeStatusCallback;
            ci.cancel();
        }
    }

    @Inject(method = "setupMouseCallbacks", at = @At(value = "HEAD"), cancellable = true)
    private static void skipGlfwMouseCallbacks(Window window, GLFWCursorPosCallbackI onMoveCallback, GLFWMouseButtonCallbackI onPressCallback, GLFWScrollCallbackI onScrollCallback, GLFWDropCallbackI onDropCallback, CallbackInfo ci) {
        if (window instanceof SDLWindow) {
            EventCallback.onMoveCallback = onMoveCallback;
            EventCallback.onPressCallback = onPressCallback;
            EventCallback.onScrollCallback = onScrollCallback;
            EventCallback.onDropCallback = onDropCallback;
            ci.cancel();
        }
    }

    @Redirect(method = "grabOrReleaseMouse", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetCursorPos(JDD)V"))
    private static void redirectSetCursorPos(long handle, double xpos, double ypos) {
        SDLMouse.SDL_WarpMouseInWindow(handle, (float) xpos, (float) ypos);
    }

    @Redirect(method = "grabOrReleaseMouse", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetInputMode(JII)V"))
    private static void redirectSetInputMode(long handle, int mode, int value) {
        if (mode == GLFW.GLFW_CURSOR) {
            switch (value) {
                case GLFW.GLFW_CURSOR_DISABLED -> {
                    SDLUtil.isMouseGrabbed = true;
                    SDLUtil.virtualMouseX = SDLUtil.realMouseX;
                    SDLUtil.virtualMouseY = SDLUtil.realMouseY;
                    SDLMouse.SDL_SetWindowRelativeMouseMode(handle, true);
                    SDLMouse.SDL_HideCursor();
                }
                case GLFW.GLFW_CURSOR_HIDDEN -> {
                    SDLUtil.isMouseGrabbed = false;
                    SDLMouse.SDL_SetWindowRelativeMouseMode(handle, false);
                    SDLMouse.SDL_HideCursor();
                }
                case GLFW.GLFW_CURSOR_NORMAL -> {
                    SDLUtil.isMouseGrabbed = false;
                    SDLMouse.SDL_SetWindowRelativeMouseMode(handle, false);
                    SDLMouse.SDL_ShowCursor();
                }
            }
        }
    }

    @Inject(method = "isRawMouseInputSupported", at = @At(value = "HEAD"), cancellable = true)
    private static void overrideIsRawMouseInputSupported(CallbackInfoReturnable<Boolean> cir) {
        if (Minecraft.getInstance().getWindow() instanceof SDLWindow) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "updateRawMouseInput", at = @At(value = "HEAD"), cancellable = true)
    private static void overrideUpdateRawMouseInput(Window window, boolean value, CallbackInfo ci) {
        if (window instanceof SDLWindow) {
            // Always raw input
            ci.cancel();
        }
    }
}
