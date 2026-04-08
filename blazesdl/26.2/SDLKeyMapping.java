/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.blazesdl;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.sdl.SDLKeycode;

public class SDLKeyMapping {
    private static final Int2IntOpenHashMap SDL_TO_GLFW_KEYS = new Int2IntOpenHashMap();
    private static final Int2IntOpenHashMap GLFW_TO_SDL_KEYS = new Int2IntOpenHashMap();

    static {
        // A - Z
        for (var i = 0; i < 26; i++) {
            mapKeyCode(SDLKeycode.SDLK_A + i, GLFW.GLFW_KEY_A + i);
        }

        // 0 - 9
        for (var i = 0; i < 10; i++) {
            mapKeyCode(SDLKeycode.SDLK_0 + i, GLFW.GLFW_KEY_0 + i);
        }

        // F1 - F12
        for (var i = 0; i < 12; i++) {
            mapKeyCode(SDLKeycode.SDLK_F1 + i, GLFW.GLFW_KEY_F1 + i);
        }
        // F13 - F24
        for (var i = 0; i < 13; i++) {
            mapKeyCode(SDLKeycode.SDLK_F13 + i, GLFW.GLFW_KEY_F13 + i);
        }

        // NumPad 0 - 9
        for (var i = 0; i < 10; i++) {
            mapKeyCode(SDLKeycode.SDLK_KP_0 + i, GLFW.GLFW_KEY_KP_0 + i);
        }

        // Symbol and special character
        mapKeyCode(SDLKeycode.SDLK_SPACE, GLFW.GLFW_KEY_SPACE);
        mapKeyCode(SDLKeycode.SDLK_APOSTROPHE, GLFW.GLFW_KEY_APOSTROPHE);
        mapKeyCode(SDLKeycode.SDLK_COMMA, GLFW.GLFW_KEY_COMMA);
        mapKeyCode(SDLKeycode.SDLK_MINUS, GLFW.GLFW_KEY_MINUS);
        mapKeyCode(SDLKeycode.SDLK_PERIOD, GLFW.GLFW_KEY_PERIOD);
        mapKeyCode(SDLKeycode.SDLK_SLASH, GLFW.GLFW_KEY_SLASH);
        mapKeyCode(SDLKeycode.SDLK_SEMICOLON, GLFW.GLFW_KEY_SEMICOLON);
        mapKeyCode(SDLKeycode.SDLK_EQUALS, GLFW.GLFW_KEY_EQUAL);
        mapKeyCode(SDLKeycode.SDLK_LEFTBRACKET, GLFW.GLFW_KEY_LEFT_BRACKET);
        mapKeyCode(SDLKeycode.SDLK_BACKSLASH, GLFW.GLFW_KEY_BACKSLASH);
        mapKeyCode(SDLKeycode.SDLK_RIGHTBRACKET, GLFW.GLFW_KEY_RIGHT_BRACKET);
        mapKeyCode(SDLKeycode.SDLK_GRAVE, GLFW.GLFW_KEY_GRAVE_ACCENT);

        // Control keys
        mapKeyCode(SDLKeycode.SDLK_ESCAPE, GLFW.GLFW_KEY_ESCAPE);
        mapKeyCode(SDLKeycode.SDLK_RETURN, GLFW.GLFW_KEY_ENTER);
        mapKeyCode(SDLKeycode.SDLK_TAB, GLFW.GLFW_KEY_TAB);
        mapKeyCode(SDLKeycode.SDLK_BACKSPACE, GLFW.GLFW_KEY_BACKSPACE);
        mapKeyCode(SDLKeycode.SDLK_INSERT, GLFW.GLFW_KEY_INSERT);
        mapKeyCode(SDLKeycode.SDLK_DELETE, GLFW.GLFW_KEY_DELETE);
        mapKeyCode(SDLKeycode.SDLK_RIGHT, GLFW.GLFW_KEY_RIGHT);
        mapKeyCode(SDLKeycode.SDLK_LEFT, GLFW.GLFW_KEY_LEFT);
        mapKeyCode(SDLKeycode.SDLK_DOWN, GLFW.GLFW_KEY_DOWN);
        mapKeyCode(SDLKeycode.SDLK_UP, GLFW.GLFW_KEY_UP);
        mapKeyCode(SDLKeycode.SDLK_PAGEUP, GLFW.GLFW_KEY_PAGE_UP);
        mapKeyCode(SDLKeycode.SDLK_PAGEDOWN, GLFW.GLFW_KEY_PAGE_DOWN);
        mapKeyCode(SDLKeycode.SDLK_HOME, GLFW.GLFW_KEY_HOME);
        mapKeyCode(SDLKeycode.SDLK_END, GLFW.GLFW_KEY_END);
        mapKeyCode(SDLKeycode.SDLK_CAPSLOCK, GLFW.GLFW_KEY_CAPS_LOCK);
        mapKeyCode(SDLKeycode.SDLK_SCROLLLOCK, GLFW.GLFW_KEY_SCROLL_LOCK);
        mapKeyCode(SDLKeycode.SDLK_NUMLOCKCLEAR, GLFW.GLFW_KEY_NUM_LOCK);
        mapKeyCode(SDLKeycode.SDLK_PRINTSCREEN, GLFW.GLFW_KEY_PRINT_SCREEN);
        mapKeyCode(SDLKeycode.SDLK_PAUSE, GLFW.GLFW_KEY_PAUSE);

        // NumPad keys
        mapKeyCode(SDLKeycode.SDLK_KP_DIVIDE, GLFW.GLFW_KEY_KP_DIVIDE);
        mapKeyCode(SDLKeycode.SDLK_KP_MULTIPLY, GLFW.GLFW_KEY_KP_MULTIPLY);
        mapKeyCode(SDLKeycode.SDLK_KP_MINUS, GLFW.GLFW_KEY_KP_SUBTRACT);
        mapKeyCode(SDLKeycode.SDLK_KP_PLUS, GLFW.GLFW_KEY_KP_ADD);
        mapKeyCode(SDLKeycode.SDLK_KP_ENTER, GLFW.GLFW_KEY_KP_ENTER);
        mapKeyCode(SDLKeycode.SDLK_KP_PERIOD, GLFW.GLFW_KEY_KP_DECIMAL);

        // Modifier keys
        mapKeyCode(SDLKeycode.SDLK_LSHIFT, GLFW.GLFW_KEY_LEFT_SHIFT);
        mapKeyCode(SDLKeycode.SDLK_LCTRL, GLFW.GLFW_KEY_LEFT_CONTROL);
        mapKeyCode(SDLKeycode.SDLK_LALT, GLFW.GLFW_KEY_LEFT_ALT);
        mapKeyCode(SDLKeycode.SDLK_LGUI, GLFW.GLFW_KEY_LEFT_SUPER);
        mapKeyCode(SDLKeycode.SDLK_RSHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT);
        mapKeyCode(SDLKeycode.SDLK_RCTRL, GLFW.GLFW_KEY_RIGHT_CONTROL);
        mapKeyCode(SDLKeycode.SDLK_RALT, GLFW.GLFW_KEY_RIGHT_ALT);
        mapKeyCode(SDLKeycode.SDLK_RGUI, GLFW.GLFW_KEY_RIGHT_SUPER);
    }

    private static void mapKeyCode(int sdlKey, int glfwKey) {
        SDL_TO_GLFW_KEYS.put(sdlKey, glfwKey);
        GLFW_TO_SDL_KEYS.put(glfwKey, sdlKey);
    }

    public static int toGlfwKey(int sdlKeycode) {
        return SDL_TO_GLFW_KEYS.getOrDefault(sdlKeycode, GLFW.GLFW_KEY_UNKNOWN);
    }

    public static int toSdlKey(int gltfKeycode) {
        return GLFW_TO_SDL_KEYS.getOrDefault(gltfKeycode, SDLKeycode.SDLK_UNKNOWN);
    }

    public static int getGlfwModifiers(int sdlMod) {
        var glfwMods = 0;
        if ((sdlMod & SDLKeycode.SDL_KMOD_SHIFT) != 0) glfwMods |= GLFW.GLFW_MOD_SHIFT;
        if ((sdlMod & SDLKeycode.SDL_KMOD_CTRL) != 0) glfwMods |= GLFW.GLFW_MOD_CONTROL;
        if ((sdlMod & SDLKeycode.SDL_KMOD_ALT) != 0) glfwMods |= GLFW.GLFW_MOD_ALT;
        if ((sdlMod & SDLKeycode.SDL_KMOD_GUI) != 0) glfwMods |= GLFW.GLFW_MOD_SUPER;
        if ((sdlMod & SDLKeycode.SDL_KMOD_CAPS) != 0) glfwMods |= GLFW.GLFW_MOD_CAPS_LOCK;
        if ((sdlMod & SDLKeycode.SDL_KMOD_NUM) != 0) glfwMods |= GLFW.GLFW_MOD_NUM_LOCK;
        return glfwMods;
    }
}
