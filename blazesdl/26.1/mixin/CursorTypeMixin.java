/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.blazesdl.mixin;

import com.mojang.blaze3d.platform.cursor.CursorType;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.sdl.SDLMouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CursorType.class)
public abstract class CursorTypeMixin {
    @Redirect(method = "createStandardCursor", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwCreateStandardCursor(I)J"))
    private static long redirectCreateStandardCursor(int glfwShape) {
        var sdlSystemCursor = switch (glfwShape) {
            case GLFW.GLFW_ARROW_CURSOR -> SDLMouse.SDL_SYSTEM_CURSOR_DEFAULT;
            case GLFW.GLFW_IBEAM_CURSOR -> SDLMouse.SDL_SYSTEM_CURSOR_TEXT;
            case GLFW.GLFW_CROSSHAIR_CURSOR -> SDLMouse.SDL_SYSTEM_CURSOR_CROSSHAIR;
            case GLFW.GLFW_POINTING_HAND_CURSOR -> SDLMouse.SDL_SYSTEM_CURSOR_POINTER;
            case GLFW.GLFW_RESIZE_EW_CURSOR -> SDLMouse.SDL_SYSTEM_CURSOR_EW_RESIZE;
            case GLFW.GLFW_RESIZE_NS_CURSOR -> SDLMouse.SDL_SYSTEM_CURSOR_NS_RESIZE;
            case GLFW.GLFW_RESIZE_ALL_CURSOR -> SDLMouse.SDL_SYSTEM_CURSOR_MOVE;
            case GLFW.GLFW_NOT_ALLOWED_CURSOR -> SDLMouse.SDL_SYSTEM_CURSOR_NOT_ALLOWED;
            default -> 0;
        };

        return SDLMouse.SDL_CreateSystemCursor(sdlSystemCursor);
    }

    @Unique
    private final long blazesdl$defaultCursor = SDLMouse.SDL_CreateSystemCursor(SDLMouse.SDL_SYSTEM_CURSOR_DEFAULT);

    @Redirect(method = "select", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetCursor(JJ)V"))
    public void redirectSelect(long windowHandle, long cursorHandle) {
        if (cursorHandle == 0L) {
            SDLMouse.SDL_SetCursor(blazesdl$defaultCursor);
        } else {
            SDLMouse.SDL_SetCursor(cursorHandle);
        }
    }
}
