package top.fifthlight.blazesdl;

import com.mojang.blaze3d.opengl.GlSurface;
import com.mojang.blaze3d.systems.GpuSurface;
import org.lwjgl.sdl.SDLVideo;

public class SDLGlSurface extends GlSurface {
    private final long windowHandle;

    public SDLGlSurface(long windowHandle) {
        super(windowHandle);
        this.windowHandle = windowHandle;
    }

    @Override
    public void configure(GpuSurface.Configuration config) {
        SDLVideo.SDL_GL_SetSwapInterval(config.presentMode() == GpuSurface.PresentMode.FIFO ? 1 : 0);
    }

    @Override
    public void present() {
        SDLVideo.SDL_GL_SwapWindow(this.windowHandle);
    }
}
