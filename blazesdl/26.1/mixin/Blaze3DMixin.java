/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.blazesdl.mixin;

import com.mojang.blaze3d.Blaze3D;
import org.lwjgl.sdl.SDLTimer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = Blaze3D.class, remap = false)
public abstract class Blaze3DMixin {
    @Redirect(method = "getTime", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwGetTime()D"))
    private static double redirectGetTime() {
        return SDLTimer.SDL_GetTicks() / 1000.0;
    }
}
