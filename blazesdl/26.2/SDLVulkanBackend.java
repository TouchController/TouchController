package top.fifthlight.blazesdl;

import com.mojang.blaze3d.vulkan.VulkanBackend;
import org.lwjgl.sdl.SDLVideo;

public class SDLVulkanBackend extends VulkanBackend {
    @Override
    public void setWindowHints() {
        SDLWindow.windowCreateHint = SDLVideo.SDL_WINDOW_VULKAN;
    }
}
