/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.mixin.v1_21_10;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.fifthlight.touchcontroller.common.config.data.StatusConfig;
import top.fifthlight.touchcontroller.common.config.holder.GlobalConfigHolder;

@Mixin(MouseHandler.class)
abstract class DisableMouseDirectionMixin {
    @Inject(
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/Minecraft;player:Lnet/minecraft/client/player/LocalPlayer;",
                    ordinal = 1
            ),
            method = "turnPlayer",
            cancellable = true
    )
    private void turnPlayer(CallbackInfo ci) {
        var configHolder = GlobalConfigHolder.INSTANCE;
        var config = configHolder.getConfig().getValue();
        if (config.getStatus().getStatus() == StatusConfig.Status.DISABLED) {
            return;
        }
        if (config.getRegular().getDisableMouseMove()) {
            ci.cancel();
        }
    }
}
