/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.fabric.v1_21_10.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.fifthlight.touchcontroller.common.config.GlobalConfig;
import top.fifthlight.touchcontroller.common.config.data.StatusConfig;
import top.fifthlight.touchcontroller.common.config.holder.GlobalConfigHolder;
import top.fifthlight.touchcontroller.extension.v1_21_10.ClickableKeyBinding;
import top.fifthlight.touchcontroller.fabric.v1_21_10.TouchController;
import top.fifthlight.touchcontroller.fabric.v1_21_10.gal.key.KeyBindingHandlerImpl;

import java.util.function.Consumer;

@Mixin(KeyMapping.class)
public abstract class KeyBindingMixin implements ClickableKeyBinding {
    @Shadow
    private int clickCount;

    @Unique
    private static boolean touchController$doCancelKey(GlobalConfig config, KeyMapping keyMapping) {
        var client = Minecraft.getInstance();
        if (keyMapping == client.options.keyAttack || keyMapping == client.options.keyUse) {
            return config.getRegular().getDisableMouseClick() || config.getDebug().getEnableTouchEmulation();
        }

        for (var i = 0; i < 9; i++) {
            if (client.options.keyHotbarSlots[i] == keyMapping) {
                return config.getRegular().getDisableHotBarKey();
            }
        }

        return false;
    }

    @WrapOperation(method = "forAllKeyMappings", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"))
    private static <T> void forAllKeyMappings(Consumer<T> instance, T keyMapping, Operation<Void> original) {
        var configHolder = GlobalConfigHolder.INSTANCE;
        var config = configHolder.getConfig().getValue();
        if (config.getStatus().getStatus() == StatusConfig.Status.DISABLED) {
            original.call(instance, keyMapping);
            return;
        }

        if (!(keyMapping instanceof KeyMapping key)) {
            original.call(instance, keyMapping);
            return;
        }
        if (touchController$doCancelKey(config, key)) {
            return;
        }
        original.call(instance, keyMapping);
    }

    @Override
    public void touchController$click() {
        clickCount++;
    }

    @Override
    public int touchController$getClickCount() {
        return clickCount;
    }

    @Inject(
            method = "isDown()Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void overrideIsDown(CallbackInfoReturnable<Boolean> info) {
        if (KeyBindingHandlerImpl.INSTANCE.isDown((KeyMapping) (Object) this)) {
            info.setReturnValue(true);
        }
    }

    @Inject(
            method = "setDown(Z)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void blockEmulatedKeyDown(boolean value, CallbackInfo ci) {
        if (TouchController.isInEmulatedSetDown()) {
            ci.cancel();
        }
    }
}
