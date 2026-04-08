/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.blazesdl;

import com.mojang.blaze3d.platform.TextInputManager;
import org.lwjgl.sdl.SDLKeyboard;

public class SDLTextInputManager extends TextInputManager {
    private final SDLWindow window;

    public SDLTextInputManager(SDLWindow window) {
        super(window);
        this.window = window;
    }

    @Override
    public void setTextInputArea(int x0, int y0, int x1, int y1) {
        SDLUtil.updateTextInputAreaScaled(window, x0, y0, x1 - x0, y1 - y0, 0);
    }

    @Override
    protected void setIMEInputMode(boolean value) {
        if (value) {
            SDLKeyboard.SDL_StartTextInput(window.handle());
        } else {
            SDLKeyboard.SDL_StopTextInput(window.handle());
        }
    }

    @Override
    protected boolean getIMEStatus() {
        // SDL doesn't care whether IME is enabled
        return true;
    }
}
