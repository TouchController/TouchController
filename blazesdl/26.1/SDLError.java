/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.blazesdl;

public class SDLError {
    public static RuntimeException handleError(String func) {
        var error = org.lwjgl.sdl.SDLError.SDL_GetError();
        if (error != null) {
            return new RuntimeException("Function " + func + " failed with cause: " + error);
        } else {
            return new RuntimeException("Function " + func + " failed with no cause");
        }
    }
}
