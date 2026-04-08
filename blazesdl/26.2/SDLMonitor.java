/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.blazesdl;

import com.mojang.blaze3d.platform.Monitor;
import org.lwjgl.sdl.SDLStdinc;
import org.lwjgl.sdl.SDLVideo;
import org.lwjgl.sdl.SDL_Rect;
import org.lwjgl.system.MemoryStack;

public class SDLMonitor extends Monitor {
    public SDLMonitor(long monitor) {
        super((int) monitor);
    }

    @Override
    public void refreshVideoModes() {
        this.videoModes.clear();
        var modes = SDLVideo.SDL_GetFullscreenDisplayModes((int) monitor);
        if (modes == null) {
            throw SDLError.handleError("SDL_GetFullscreenDisplayModes");
        }
        try {
            for (var i = modes.limit() - 1; i >= 0; i--) {
                var mode = SDLVideoMode.fromSDLDisplayMode(modes.get(i));
                if (mode.getRedBits() >= 8 && mode.getGreenBits() >= 8 && mode.getBlueBits() >= 8) {
                    this.videoModes.add(mode);
                }
            }
        } finally {
            SDLStdinc.SDL_free(modes);
        }

        try (var stack = MemoryStack.stackPush()) {
            var rect = SDL_Rect.malloc(stack);
            if (!SDLVideo.SDL_GetDisplayBounds((int) this.monitor, rect)) {
                throw SDLError.handleError("SDL_GetDisplayBounds");
            }
            this.x = rect.x();
            this.y = rect.y();

            var mode = SDLVideo.SDL_GetCurrentDisplayMode((int) this.monitor);
            if (mode == null) {
                throw SDLError.handleError("SDL_GetCurrentDisplayMode");
            }
            this.currentMode = SDLVideoMode.fromSDLDisplayMode(mode.address());
        }
    }
}
