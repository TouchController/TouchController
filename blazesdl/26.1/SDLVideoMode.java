/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.blazesdl;

import com.mojang.blaze3d.platform.VideoMode;
import org.jspecify.annotations.NonNull;
import org.lwjgl.sdl.SDLPixels;
import org.lwjgl.sdl.SDL_DisplayMode;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.Objects;

public class SDLVideoMode extends VideoMode {
    public final SDL_DisplayMode displayMode;
    protected float refreshRateFloat;
    protected float refreshRateNumerator;
    protected float refreshRateDenominator;

    public SDLVideoMode(int width, int height, int redBits, int greenBits, int blueBits, int refreshRate, float refreshRateFloat, float refreshRateNumerator, float refreshRateDenominator, SDL_DisplayMode sdlDisplayMode) {
        super(width, height, redBits, greenBits, blueBits, refreshRate);
        this.refreshRateFloat = refreshRateFloat;
        this.refreshRateNumerator = refreshRateNumerator;
        this.refreshRateDenominator = refreshRateDenominator;
        this.displayMode = sdlDisplayMode;
    }

    public static SDLVideoMode fromSDLDisplayMode(long displayModePointer) {
        var buffer = ByteBuffer.allocateDirect(SDL_DisplayMode.SIZEOF);
        MemoryUtil.memCopy(displayModePointer, MemoryUtil.memAddress(buffer), SDL_DisplayMode.SIZEOF);
        var displayMode = new SDL_DisplayMode(buffer);
        var pixelFormat = displayMode.format();
        var pixelFormatDetails = SDLPixels.SDL_GetPixelFormatDetails(pixelFormat);
        if (pixelFormatDetails == null) {
            throw SDLError.handleError("SDL_GetPixelFormatDetails");
        }
        var redBits = pixelFormatDetails.Rbits();
        var greenBits = pixelFormatDetails.Gbits();
        var blueBits = pixelFormatDetails.Bbits();

        return new SDLVideoMode(displayMode.w(),
                displayMode.h(),
                redBits,
                greenBits,
                blueBits,
                Math.round(displayMode.refresh_rate()),
                displayMode.refresh_rate(),
                displayMode.refresh_rate_numerator(),
                displayMode.refresh_rate_denominator(),
                displayMode);
    }

    public float getRefreshRateFloat() {
        return refreshRateFloat;
    }

    public float getRefreshRateNumerator() {
        return refreshRateNumerator;
    }

    public float getRefreshRateDenominator() {
        return refreshRateDenominator;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        var that = (SDLVideoMode) o;
        return Float.compare(refreshRateFloat, that.refreshRateFloat) == 0 && Float.compare(refreshRateNumerator, that.refreshRateNumerator) == 0 && Float.compare(refreshRateDenominator, that.refreshRateDenominator) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), refreshRateFloat, refreshRateNumerator, refreshRateDenominator);
    }

    public @NonNull String toString() {
        return String.format(Locale.ROOT, "%sx%s@%s (%sbit)", this.getWidth(), this.getHeight(), this.getRefreshRateFloat(), this.getRedBits() + this.getGreenBits() + this.getBlueBits());
    }

    // TODO: Override read() and write()
}
