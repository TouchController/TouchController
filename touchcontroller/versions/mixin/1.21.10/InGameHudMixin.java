/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.mixin.v1_21_10;

import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.fifthlight.touchcontroller.common.config.data.StatusConfig;
import top.fifthlight.touchcontroller.common.config.holder.GlobalConfigHolder;
import top.fifthlight.touchcontroller.common.event.render.RenderEvents;
import top.fifthlight.touchcontroller.common.model.ControllerHudModel;

@Mixin(Gui.class)
public abstract class InGameHudMixin {
    @Shadow
    @Final
    private static ResourceLocation CROSSHAIR_ATTACK_INDICATOR_FULL_SPRITE;
    @Shadow
    @Final
    private static ResourceLocation CROSSHAIR_ATTACK_INDICATOR_BACKGROUND_SPRITE;
    @Shadow
    @Final
    private static ResourceLocation CROSSHAIR_ATTACK_INDICATOR_PROGRESS_SPRITE;
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(
            method = "renderCrosshair",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIII)V",
                    ordinal = 0
            ),
            cancellable = true
    )
    public void renderCrosshair(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo callbackInfo) {
        var config = GlobalConfigHolder.INSTANCE.getConfig().getValue();
        if (config.getStatus().getStatus() == StatusConfig.Status.DISABLED) {
            return;
        }
        var shouldRender = RenderEvents.INSTANCE.shouldRenderCrosshair();
        if (!shouldRender) {
            if (this.minecraft.options.attackIndicator().get() == AttackIndicatorStatus.CROSSHAIR) {
                var attackCooldownProgress = this.minecraft.player.getAttackStrengthScale(0.0f);
                var renderFullTexture = false;
                if (this.minecraft.crosshairPickEntity != null && this.minecraft.crosshairPickEntity instanceof LivingEntity && attackCooldownProgress >= 1.0f) {
                    renderFullTexture = this.minecraft.player.getCurrentItemAttackStrengthDelay() > 5.0f && this.minecraft.crosshairPickEntity.isAlive();
                }
                var x = context.guiWidth() / 2;
                var y = context.guiHeight() / 2;
                if (renderFullTexture) {
                    context.blitSprite(RenderPipelines.CROSSHAIR, CROSSHAIR_ATTACK_INDICATOR_FULL_SPRITE, x - 8, y - 8, 16, 16);
                } else if (attackCooldownProgress < 1.0f) {
                    var progress = (int) (attackCooldownProgress * 17.0f);
                    context.blitSprite(RenderPipelines.CROSSHAIR, CROSSHAIR_ATTACK_INDICATOR_BACKGROUND_SPRITE, x - 8, y - 2, 16, 4);
                    context.blitSprite(RenderPipelines.CROSSHAIR, CROSSHAIR_ATTACK_INDICATOR_PROGRESS_SPRITE, 16, 4, 0, 0, x - 8, y - 2, progress, 4);
                }
            }
            callbackInfo.cancel();
        }
    }

    @Inject(
            method = "renderItemHotbar",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIII)V",
                    ordinal = 1,
                    shift = At.Shift.AFTER
            )
    )
    private void renderHotbar(GuiGraphics context, DeltaTracker deltaTracker, CallbackInfo ci) {
        var config = GlobalConfigHolder.INSTANCE.getConfig().getValue();
        if (config.getStatus().getStatus() == StatusConfig.Status.DISABLED) {
            return;
        }
        var player = minecraft.player;
        if (player != null) {
            var controllerHudModel = ControllerHudModel.Global;
            var inventory = controllerHudModel.getResult().getInventory();
            var slots = inventory.getSlots();
            var x = (context.guiWidth() - 182) / 2 + 1;
            var y = context.guiHeight() - 22 + 1;
            for (int i = 0; i < 9; i++) {
                var stack = player.getInventory().getItem(i);
                if (stack.isEmpty()) {
                    continue;
                }
                var slot = slots[i];
                var progress = slot.getProgress();
                var height = (int) (16 * progress);
                context.fill(x + 20 * i + 2, y + 18 - height, x + 20 * i + 18, y + 18, 0xFF00BB00);
            }
        }
    }
}
