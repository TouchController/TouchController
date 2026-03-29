/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.mixin.v1_21_10;

import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.fifthlight.touchcontroller.event.v1_21_10.KeyboardInputEvents;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin {
    @Inject(
            at = @At("TAIL"),
            method = "tick"
    )
    private void tick(CallbackInfo info) {
        var moveVector = KeyboardInputEvents.INSTANCE.onEndTick((KeyboardInput) (Object) this);
        ((ClientInputAccessor) this).touchcontroller$setMoveVector(moveVector);
    }
}
