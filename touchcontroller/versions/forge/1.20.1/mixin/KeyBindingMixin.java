/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.forge.v1_20_1.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraftforge.client.settings.KeyMappingLookup;
import org.spongepowered.asm.mixin.Final;
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
import top.fifthlight.touchcontroller.extension.v1_20_1.ClickableKeyBinding;
import top.fifthlight.touchcontroller.forge.v1_20_1.gal.key.KeyBindingHandlerImpl;

@Mixin(KeyMapping.class)
public abstract class KeyBindingMixin implements ClickableKeyBinding {
    @Shadow(aliases = {"f_90810_"})
    @Final
    private static KeyMappingLookup MAP;

    @Shadow
    private int clickCount;

    @Unique
    private static boolean touchController$doCancelKey(GlobalConfig config, InputConstants.Key key) {
        var client = Minecraft.getInstance();
        var keyBindings = MAP.getAll(key);

        if (keyBindings.contains(client.options.keyAttack) || keyBindings.contains(client.options.keyUse)) {
            return config.getRegular().getDisableMouseClick() || config.getDebug().getEnableTouchEmulation();
        }

        for (var i = 0; i < 9; i++) {
            if (keyBindings.contains(client.options.keyHotbarSlots[i])) {
                return config.getRegular().getDisableHotBarKey();
            }
        }

        return false;
    }

    @Inject(method = "click", at = @At("HEAD"), cancellable = true)
    private static void onKeyPressed(InputConstants.Key key, CallbackInfo info) {
        var configHolder = GlobalConfigHolder.INSTANCE;
        var config = configHolder.getConfig().getValue();
        if (config.getStatus().getStatus() == StatusConfig.Status.DISABLED) {
            return;
        }

        if (touchController$doCancelKey(config, key)) {
            info.cancel();
        }
    }

    @Inject(method = "set", at = @At("HEAD"), cancellable = true)
    private static void setKeyPressed(InputConstants.Key key, boolean pHeld, CallbackInfo info) {
        var configHolder = GlobalConfigHolder.INSTANCE;
        var config = configHolder.getConfig().getValue();
        if (config.getStatus().getStatus() == StatusConfig.Status.DISABLED) {
            return;
        }

        if (touchController$doCancelKey(config, key)) {
            info.cancel();
        }
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
}
