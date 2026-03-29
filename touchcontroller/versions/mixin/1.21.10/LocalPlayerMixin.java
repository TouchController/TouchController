/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.mixin.v1_21_10;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import top.fifthlight.touchcontroller.common.model.ControllerHudModel;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @Shadow
    @Final
    protected Minecraft minecraft;

    /// Because Minecraft Java version requires you to stand on ground to trigger sprint on double-clicking forward key,
    /// this method change the on ground logic to relax this requirement when using touch input.
    @Redirect(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;onGround()Z",
                    ordinal = 0
            )
    )
    public boolean redirectIsOnGround(LocalPlayer instance) {
        var controllerHudModel = ControllerHudModel.Global;
        var contextResult = controllerHudModel.getResult();
        if (contextResult.getForward() != 0) {
            return true;
        } else {
            return instance.onGround();
        }
    }
}
