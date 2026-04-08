/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.blazesdl;

import com.mojang.blaze3d.platform.Monitor;
import com.mojang.blaze3d.platform.MonitorCreator;
import com.mojang.blaze3d.platform.ScreenManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import org.jspecify.annotations.Nullable;
import org.lwjgl.sdl.SDLEvents;
import org.lwjgl.sdl.SDLStdinc;
import org.lwjgl.sdl.SDLVideo;
import org.lwjgl.sdl.SDL_Rect;
import org.lwjgl.system.MemoryStack;

public class SDLScreenManager extends ScreenManager {
    public SDLScreenManager(MonitorCreator monitorCreator) {
        super(monitorCreator);
        var displays = SDLVideo.SDL_GetDisplays();
        if (displays == null) {
            throw SDLError.handleError("SDL_GetDisplays");
        }
        try {
            for (var i = 0; i < displays.limit(); i++) {
                var monitor = displays.get(i);
                this.monitors.put(monitor, monitorCreator.createMonitor(Integer.toUnsignedLong(monitor)));
            }
        } finally {
            SDLStdinc.SDL_free(displays);
        }
    }

    @Override
    public void onMonitorChange(long monitor, int event) {
        RenderSystem.assertOnRenderThread();
        switch (event) {
            case SDLEvents.SDL_EVENT_DISPLAY_ADDED -> {
                this.monitors.put(monitor, this.monitorCreator.createMonitor(monitor));
                ScreenManager.LOGGER.debug("Monitor {} connected. Current monitors: {}", monitor, this.monitors);
            }
            case SDLEvents.SDL_EVENT_DISPLAY_REMOVED -> {
                this.monitors.remove(monitor);
                ScreenManager.LOGGER.debug("Monitor {} disconnected. Current monitors: {}", monitor, this.monitors);
            }
        }
    }

    @Override
    public @Nullable Monitor findBestMonitor(Window window) {
        var windowDisplay = SDLVideo.SDL_GetDisplayForWindow(window.handle());
        if (windowDisplay != 0) {
            return this.getMonitor(windowDisplay);
        }

        var displays = SDLVideo.SDL_GetDisplays();
        if (displays == null) {
            throw SDLError.handleError("SDL_GetDisplays");
        }

        var winMinX = window.getX();
        var winMaxX = winMinX + window.getScreenWidth();
        var winMinY = window.getY();
        var winMaxY = winMinY + window.getScreenHeight();
        var maxArea = -1;

        Monitor result = null;
        var primaryDisplay = SDLVideo.SDL_GetPrimaryDisplay();
        LOGGER.debug("Selecting monitor - primary: {}, current monitors: {}", primaryDisplay, this.monitors);

        try (var stack = MemoryStack.stackPush()) {
            var rect = SDL_Rect.malloc(stack);

            for (var monitor : this.monitors.values()) {
                var display = (int) monitor.getMonitor();
                if (!SDLVideo.SDL_GetDisplayBounds(display, rect)) {
                    throw SDLError.handleError("SDL_GetDisplayBounds");
                }

                var monMinX = rect.x();
                var monMaxX = monMinX + monitor.getCurrentMode().getWidth();
                var monMinY = rect.y();
                var monMaxY = monMinY + monitor.getCurrentMode().getHeight();

                var minX = clamp(winMinX, monMinX, monMaxX);
                var maxX = clamp(winMaxX, monMinX, monMaxX);
                var minY = clamp(winMinY, monMinY, monMaxY);
                var maxY = clamp(winMaxY, monMinY, monMaxY);

                var sx = Math.max(0, maxX - minX);
                var sy = Math.max(0, maxY - minY);
                var area = sx * sy;

                if (area > maxArea) {
                    result = monitor;
                    maxArea = area;
                } else if (area == maxArea && primaryDisplay == display) {
                    LOGGER.debug("Primary monitor {} is preferred to monitor {}", monitor, result);
                    result = monitor;
                }
            }
        }

        LOGGER.debug("Selected monitor: {}", result);
        return result;
    }

    @Override
    public void shutdown() {
        // no-op
    }
}
