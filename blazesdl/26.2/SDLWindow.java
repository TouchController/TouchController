/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.blazesdl;

import com.mojang.blaze3d.platform.*;
import com.mojang.blaze3d.systems.BackendCreationException;
import com.mojang.blaze3d.systems.GpuBackend;
import net.minecraft.server.packs.PackResources;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.sdl.*;
import org.lwjgl.system.JNI;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class SDLWindow extends Window {
    public static long windowCreateFlags = 0;

    public SDLWindow(WindowEventHandler windowEventHandler,
                     DisplayData displayData,
                     @Nullable String fullscreenVideoModeString,
                     boolean exclusiveFullscreen,
                     String title,
                     MonitorManager monitorManager,
                     GpuBackend backend) throws BackendCreationException {
        super(windowEventHandler, displayData, fullscreenVideoModeString, exclusiveFullscreen, title, monitorManager, backend);
    }

    private SDL_Surface createSDLSurface(NativeImage image) {
        if (image.format() != NativeImage.Format.RGBA) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "createSDLSurface only works on RGBA images; have %s", image.format()));
        }
        var pixelsPtr = image.getPointer();
        if (pixelsPtr == 0) {
            throw new IllegalStateException("NativeImage pointer is null");
        }
        var surface = SDLSurface.nSDL_CreateSurfaceFrom(
                image.getWidth(),
                image.getHeight(),
                SDLPixels.SDL_PIXELFORMAT_RGBA32,
                pixelsPtr,
                image.getWidth() * 4
        );
        if (surface == 0L) {
            throw SDLError.handleError("SDL_CreateSurfaceFrom");
        }
        return SDL_Surface.createSafe(surface);
    }

    @Override
    public void setIcon(@NonNull PackResources resources, @NonNull IconSet iconSet) throws IOException {
        var icons = iconSet.getStandardIcons(resources);
        if (icons.isEmpty()) {
            return;
        }

        NativeImage mainImage = null;
        SDL_Surface mainSurface = null;
        var altImages = new ArrayList<NativeImage>(icons.size() - 1);
        var altSurfaces = new ArrayList<SDL_Surface>(icons.size() - 1);
        try {
            mainImage = NativeImage.read(icons.getLast().get());
            mainSurface = createSDLSurface(mainImage);

            for (var i = icons.size() - 1; i >= 0; i--) {
                var altImage = NativeImage.read(icons.get(i).get());
                altImages.add(altImage);
                var altSurface = createSDLSurface(altImage);
                altSurfaces.add(altSurface);
                SDLSurface.SDL_AddSurfaceAlternateImage(mainSurface, altSurface);
            }

            SDLVideo.SDL_SetWindowIcon(handle, mainSurface);
        } finally {
            for (var altSurface : altSurfaces) {
                SDLSurface.SDL_DestroySurface(altSurface);
            }
            for (var altImage : altImages) {
                altImage.close();
            }
            if (mainSurface != null) {
                SDLSurface.SDL_DestroySurface(mainSurface);
            }
            if (mainImage != null) {
                mainImage.close();
            }
        }
    }

    @Override
    public void setTitle(@NonNull String title) {
        SDLVideo.SDL_SetWindowTitle(handle, title);
    }

    @Override
    public int getRefreshRate() {
        var monitor = SDLVideo.SDL_GetDisplayForWindow(this.handle);
        if (monitor == 0L) {
            throw SDLError.handleError("SDL_GetDisplayForWindow");
        }
        var videoMode = SDLVideo.SDL_GetCurrentDisplayMode(monitor);
        if (videoMode == null) {
            throw SDLError.handleError("SDL_GetCurrentDisplayMode");
        }
        return Math.round(videoMode.refresh_rate());
    }

    @Override
    protected void setMode() {
        var wasFullscreen = (SDLVideo.SDL_GetWindowFlags(this.handle) & SDLVideo.SDL_WINDOW_FULLSCREEN) != 0;

        if (this.fullscreen) {
            var monitor = monitorManager.findBestMonitor(this);

            if (monitor == null) {
                LOGGER.warn("Failed to find suitable monitor for fullscreen mode");
                this.fullscreen = false;
            } else {
                long displayModeAddress = 0;
                if (exclusiveFullscreen) {
                    var mode = monitor.getPreferredVidMode(this.preferredFullscreenVideoMode);
                    var displayMode = ((SDLVideoMode) mode).displayMode;
                    displayModeAddress = displayMode.address();
                }
                if (!wasFullscreen) {
                    this.windowedX = this.x;
                    this.windowedY = this.y;
                    this.windowedWidth = allowedWindowMinSize(this.width);
                    this.windowedHeight = allowedWindowMinSize(this.height);
                }

                var SDL_SetWindowFullscreenMode = SDLVideo.Functions.SetWindowFullscreenMode;
                if (!JNI.invokePPZ(this.handle, displayModeAddress, SDL_SetWindowFullscreenMode)) {
                    throw SDLError.handleError("SDL_SetWindowFullscreenMode");
                }
                if (!SDLVideo.SDL_SetWindowFullscreen(this.handle, true)) {
                    throw SDLError.handleError("SDL_SetWindowFullscreen");
                }

                this.x = 0;
                this.y = 0;
                try (var stack = MemoryStack.stackPush()) {
                    var width = stack.mallocInt(1);
                    var height = stack.mallocInt(1);
                    if (!SDLVideo.SDL_GetWindowSize(this.handle, width, height)) {
                        throw SDLError.handleError("SDL_GetWindowSize");
                    }
                    this.width = allowedWindowMinSize(width.get(0));
                    this.height = allowedWindowMinSize(height.get(0));
                }
                SDLVideo.SDL_SyncWindow(this.handle);
            }
        } else {
            this.x = this.windowedX;
            this.y = this.windowedY;
            this.width = this.windowedWidth;
            this.height = this.windowedHeight;

            if (!SDLVideo.SDL_SetWindowFullscreen(this.handle, false)) {
                throw SDLError.handleError("SDL_SetWindowFullscreen");
            }
            if (!SDLUtil.IS_WAYLAND) {
                if (!SDLVideo.SDL_SetWindowPosition(this.handle, this.x, this.y)) {
                    throw SDLError.handleError("SDL_SetWindowPosition");
                }
            }
            if (!SDLVideo.SDL_SetWindowSize(this.handle, this.width, this.height)) {
                throw SDLError.handleError("SDL_SetWindowSize");
            }
            SDLVideo.SDL_SyncWindow(this.handle);
        }
    }

    @Override
    protected void setBootErrorCallback() {
        // no-op
    }

    @Override
    public void setDefaultErrorCallback() {
        // no-op
    }

    public boolean shouldClose = false;

    @Override
    public boolean shouldClose() {
        return shouldClose;
    }

    public Runnable closeCallback;

    @Override
    public void setWindowCloseCallback(@NonNull Runnable task) {
        closeCallback = task;
    }

    public SDLMonitorManager getMonitorManager() {
        return (SDLMonitorManager) monitorManager;
    }
}
