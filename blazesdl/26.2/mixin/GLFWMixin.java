/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.blazesdl.mixin;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.sdl.SDLClipboard;
import org.lwjgl.sdl.SDLKeyboard;
import org.lwjgl.sdl.SDLKeycode;
import org.lwjgl.sdl.SDLScancode;
import org.lwjgl.system.NativeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import top.fifthlight.blazesdl.SDLKeyMapping;
import top.fifthlight.blazesdl.SDLUtil;

@SuppressWarnings("OverwriteAuthorRequired")
@Mixin(GLFW.class)
public abstract class GLFWMixin {
    @Overwrite
    public static int glfwGetKey(long window, int key) {
        var keyboardState = SDLUtil.keyboardState;
        if (keyboardState == null) {
            return GLFW.GLFW_RELEASE;
        }
        var sdlKeyCode = SDLKeyMapping.toSdlKey(key);
        var sdlScanCode = SDLKeyboard.SDL_GetScancodeFromKey(sdlKeyCode, null);
        if (sdlScanCode == SDLScancode.SDL_SCANCODE_UNKNOWN) {
            return GLFW.GLFW_RELEASE;
        }
        var state = keyboardState.get(sdlScanCode);
        return state != 0 ? GLFW.GLFW_PRESS : GLFW.GLFW_RELEASE;
    }

    @Overwrite
    public static long nglfwGetKeyName(int key, int scancode) {
        if (key != -1) {
            return SDLKeyboard.nSDL_GetKeyName(SDLKeyMapping.toSdlKey(key));
        } else if (scancode != -1) {
            return SDLKeyboard.nSDL_GetScancodeName(scancode);
        } else {
            return 0;
        }
    }

    @Overwrite
    public static int glfwGetKeyScancode(int key) {
        var sdlKey = SDLKeyMapping.toSdlKey(key);
        if (sdlKey == SDLKeycode.SDLK_UNKNOWN) {
            return -1;
        }
        var scan = SDLKeyboard.SDL_GetScancodeFromKey(sdlKey, null);
        if (scan == SDLScancode.SDL_SCANCODE_UNKNOWN) {
            return -1;
        }
        return scan;
    }

    @Overwrite
    public static String glfwGetClipboardString(@NativeType("GLFWwindow *") long window) {
        return SDLClipboard.SDL_GetClipboardText();
    }

    @Overwrite
    public static void glfwSetClipboardString(@NativeType("GLFWwindow *") long window, CharSequence string) {
        SDLClipboard.SDL_SetClipboardText(string);
    }
}
