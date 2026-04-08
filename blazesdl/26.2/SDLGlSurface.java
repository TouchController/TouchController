package top.fifthlight.blazesdl;

import com.mojang.blaze3d.opengl.GlSurface;
import com.mojang.blaze3d.systems.GpuSurface;
import com.mojang.blaze3d.systems.SurfaceException;
import org.lwjgl.sdl.SDLVideo;

public class SDLGlSurface extends GlSurface {
    private final long windowHandle;

    public SDLGlSurface(long windowHandle) {
        super(windowHandle);
        this.windowHandle = windowHandle;
    }

    @Override
    public void configure(GpuSurface.Configuration config) throws SurfaceException {
        SDLVideo.SDL_GL_SetSwapInterval(config.vsync() ? 1 : 0);
    }

    @Override
    public void present() {
        SDLVideo.SDL_GL_SwapWindow(this.windowHandle);
    }
}
