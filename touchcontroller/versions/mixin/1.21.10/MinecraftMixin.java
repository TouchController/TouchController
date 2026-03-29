/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.mixin.v1_21_10;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.fifthlight.touchcontroller.common.event.render.RenderEvents;
import top.fifthlight.touchcontroller.common.model.TouchControllerLoadStatus;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "runTick", at = @At("HEAD"))
    public void onRenderStart(boolean tick, CallbackInfo ci) {
        var instance = TouchControllerLoadStatus.INSTANCE;
        if (instance.isLoaded()) {
            RenderEvents.onRenderStart();
        }
    }
}
