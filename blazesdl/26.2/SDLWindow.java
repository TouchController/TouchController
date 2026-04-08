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
import org.lwjgl.sdl.SDLPixels;
import org.lwjgl.sdl.SDLSurface;
import org.lwjgl.sdl.SDLVideo;
import org.lwjgl.sdl.SDL_Surface;
import org.lwjgl.system.JNI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class SDLWindow extends Window {
    public static boolean useExclusiveFullscreen = false;
    public static long windowCreateHint = 0;

    public SDLWindow(WindowEventHandler eventHandler,
                     DisplayData displayData,
                     @Nullable String fullscreenVideoModeString,
                     String title,
                     GpuBackend backend) throws BackendCreationException {
        super(eventHandler, displayData, fullscreenVideoModeString, title, backend);
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
            mainImage = NativeImage.read(icons.getFirst().get());
            mainSurface = createSDLSurface(mainImage);

            for (var i = 1; i < icons.size(); i++) {
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

    private boolean setupFullscreenVideoMode() {
        if (useExclusiveFullscreen || SDLUtil.IS_WAYLAND) { // Wayland doesn't have real exclusive fullscreen
            var monitor = this.screenManager.findBestMonitor(this);
            if (monitor == null) {
                LOGGER.warn("Failed to find suitable monitor for fullscreen mode");
                return false;
            } else {
                var videomode = monitor.getPreferredVidMode(this.preferredFullscreenVideoMode);

                this.x = 0;
                this.y = 0;
                this.width = videomode.getWidth();
                this.height = videomode.getHeight();

                // Call SDL_SetWindowFullscreenMode directly because NPE https://github.com/LWJGL/lwjgl3/issues/1110
                var displayMode = ((SDLVideoMode) videomode).displayMode;
                var SDL_SetWindowFullscreenMode = SDLVideo.Functions.SetWindowFullscreenMode;
                if (!JNI.invokePPZ(this.handle, displayMode.address(), SDL_SetWindowFullscreenMode)) {
                    throw SDLError.handleError("SDL_SetWindowFullscreenMode");
                }

                return true;
            }
        } else {
            LOGGER.warn("BlazeSDL: Can't set resolution without exclusive fullscreen");
            return true;
        }
    }

    @Override
    protected void setMode() {
        var wasFullscreen = (SDLVideo.SDL_GetWindowFlags(this.handle) & SDLVideo.SDL_WINDOW_FULLSCREEN) != 0;

        if (this.fullscreen) {
            if (!setupFullscreenVideoMode()) {
                return;
            }

            if (!wasFullscreen) {
                this.windowedX = this.x;
                this.windowedY = this.y;
                this.windowedWidth = this.width;
                this.windowedHeight = this.height;
            }

            if (!SDLVideo.SDL_SetWindowFullscreen(this.handle, true)) {
                throw SDLError.handleError("SDL_SetWindowFullscreen");
            }
        } else {
            this.x = this.windowedX;
            this.y = this.windowedY;
            this.width = this.windowedWidth;
            this.height = this.windowedHeight;

            if (!SDLVideo.SDL_SetWindowFullscreen(this.handle, false)) {
                throw SDLError.handleError("SDL_SetWindowFullscreen");
            }
            if (!SDLVideo.SDL_SetWindowSize(this.handle, this.width, this.height)) {
                throw SDLError.handleError("SDL_SetWindowSize");
            }
            if (!SDLUtil.IS_WAYLAND) {
                if (!SDLVideo.SDL_SetWindowPosition(this.handle, this.x, this.y)) {
                    throw SDLError.handleError("SDL_SetWindowPosition");
                }
            }
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

    public SDLScreenManager getScreenManager() {
        return (SDLScreenManager) screenManager;
    }
}
