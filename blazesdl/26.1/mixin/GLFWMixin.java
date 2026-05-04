/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.blazesdl.mixin;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.sdl.*;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import top.fifthlight.blazesdl.SDLKeyMapping;
import top.fifthlight.blazesdl.SDLUtil;

import java.nio.ByteBuffer;

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

    @Unique
    private static long blazesdl$glfwErrorCallback;

    @Overwrite
    public static int glfwGetMouseButton(long window, int button) {
        var state = SDLMouse.SDL_GetMouseState(null, null);
        var buttonMask = switch (button) {
            case GLFW.GLFW_MOUSE_BUTTON_LEFT -> SDLMouse.SDL_BUTTON_LMASK;
            case GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> SDLMouse.SDL_BUTTON_MMASK;
            case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> SDLMouse.SDL_BUTTON_RMASK;
            case GLFW.GLFW_MOUSE_BUTTON_4 -> SDLMouse.SDL_BUTTON_X1MASK;
            case GLFW.GLFW_MOUSE_BUTTON_5 -> SDLMouse.SDL_BUTTON_X2MASK;
            default -> 1 << button;
        };
        return (state & buttonMask) != 0 ? GLFW.GLFW_PRESS : GLFW.GLFW_RELEASE;
    }

    @Overwrite
    public static void glfwSetCursorPos(long window, double xpos, double ypos) {
        SDLMouse.SDL_WarpMouseInWindow(window, (float) xpos, (float) ypos);
    }

    @Overwrite
    public static void nglfwGetCursorPos(long window, long xpos, long ypos) {
        if (SDLUtil.isMouseGrabbed) {
            if (xpos != 0L) {
                MemoryUtil.memPutDouble(xpos, SDLUtil.virtualMouseX);
            }
            if (ypos != 0L) {
                MemoryUtil.memPutDouble(ypos, SDLUtil.virtualMouseY);
            }
        } else {
            if (xpos != 0L) {
                MemoryUtil.memPutDouble(xpos, SDLUtil.realMouseX);
            }
            if (ypos != 0L) {
                MemoryUtil.memPutDouble(ypos, SDLUtil.realMouseY);
            }
        }
    }

    @Overwrite
    public static long nglfwSetErrorCallback(long cbfun) {
        var previous = blazesdl$glfwErrorCallback;
        blazesdl$glfwErrorCallback = cbfun;
        return previous;
    }

    @Overwrite
    public static String glfwGetClipboardString(@NativeType("GLFWwindow *") long window) {
        return SDLClipboard.SDL_GetClipboardText();
    }

    @Overwrite
    public static void nglfwSetClipboardString(long window, long string) {
        SDLClipboard.nSDL_SetClipboardText(string);
    }

    @Overwrite
    public static void glfwSetClipboardString(long window, ByteBuffer string) {
        SDLClipboard.SDL_SetClipboardText(string);
    }

    @Overwrite
    public static void glfwSetClipboardString(@NativeType("GLFWwindow *") long window, CharSequence string) {
        SDLClipboard.SDL_SetClipboardText(string);
    }
}
