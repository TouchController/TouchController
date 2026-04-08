/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.blazesdl;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.shaders.GpuDebugOptions;
import com.mojang.blaze3d.shaders.ShaderSource;
import com.mojang.blaze3d.systems.GpuSurfaceBackend;
import org.lwjgl.sdl.SDLVideo;

public class SDLGlDevice extends GlDevice {
    public long context;

    public SDLGlDevice(long windowHandle, long context, ShaderSource defaultShaderSource, GpuDebugOptions debugOptions) {
        super(windowHandle, defaultShaderSource, debugOptions);
        this.context = context;
    }

    @Override
    public GpuSurfaceBackend createSurface(long windowHandle) {
        return new SDLGlSurface(windowHandle);
    }

    @Override
    public void close() {
        super.close();
        SDLVideo.SDL_GL_DestroyContext(context);
    }
}
