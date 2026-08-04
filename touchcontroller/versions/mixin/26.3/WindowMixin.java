/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.mixin.v26_3;

import com.mojang.blaze3d.platform.Window;
import org.lwjgl.sdl.SDL_Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.fifthlight.touchcontroller.gal.window.v26_3.PlatformWindowProviderImpl;

@Mixin(Window.class)
public class WindowMixin {
    @Inject(method = "handleEvent", at = @At("HEAD"), cancellable = true)
    public void handleWindowEvent(SDL_Event event, CallbackInfo info) {
        var handler = PlatformWindowProviderImpl.getEventHandler();
        if (handler == null) {
            return;
        }
        if (handler.invoke(event)) {
            info.cancel();
        }
    }
}
