/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) 2026 fifth_light
 */

package top.fifthlight.touchcontroller.mixin.v1_21_10;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.prediction.PredictiveAction;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.fifthlight.combine.backend.minecraft.item.v1_21_10.ItemImpl;
import top.fifthlight.touchcontroller.common.config.GlobalConfig;
import top.fifthlight.touchcontroller.common.config.data.StatusConfig;
import top.fifthlight.touchcontroller.common.config.holder.GlobalConfigHolder;
import top.fifthlight.touchcontroller.common.util.crosshair.CrosshairTargetHelper;
import top.fifthlight.touchcontroller.extension.v1_21_10.GameModeWithBreakingProgress;
import top.fifthlight.touchcontroller.extension.v1_21_10.SyncableGameMode;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin implements GameModeWithBreakingProgress, SyncableGameMode {
    @Shadow
    private float destroyProgress;
    @Shadow
    @Final
    private Minecraft minecraft;
    @Unique
    private boolean touchcontroller$resetPlayerLookTarget;
    @Unique
    private float touchcontroller$prevYaw;
    @Unique
    private float touchcontroller$prevPitch;

    @Shadow
    protected abstract void ensureHasSentCarriedItem();

    @Override
    public float touchcontroller$getBreakingProgress() {
        return destroyProgress;
    }

    @Override
    public void touchcontroller$callSyncSelectedSlot() {
        ensureHasSentCarriedItem();
    }

    @Shadow
    protected abstract void startPrediction(ClientLevel world, PredictiveAction packetCreator);

    @Unique
    private boolean touchcontroller$crosshairAimingContainItem(GlobalConfig config, Item item) {
        return config.getItem().getCrosshairAimingItems().contains(new ItemImpl(item));
    }

    @Unique
    public void touchcontroller$interactBefore(Player player, InteractionHand hand) {
        var config = GlobalConfigHolder.INSTANCE.getConfig().getValue();
        if (config.getStatus().getStatus() == StatusConfig.Status.DISABLED) {
            return;
        }

        var itemStack = player.getItemInHand(hand);
        if (!touchcontroller$crosshairAimingContainItem(config, itemStack.getItem())) {
            return;
        }

        touchcontroller$prevYaw = player.getYRot();
        touchcontroller$prevPitch = player.getXRot();

        var rotation = CrosshairTargetHelper.calculatePlayerRotation(CrosshairTargetHelper.INSTANCE.getLastCrosshairDirection());
        float yaw = rotation.getFirst();
        float pitch = rotation.getSecond();
        this.startPrediction(this.minecraft.level, sequence -> new ServerboundMovePlayerPacket.Rot(yaw, pitch, player.onGround(), player.horizontalCollision));
        player.setYRot(yaw);
        player.setXRot(pitch);
        touchcontroller$resetPlayerLookTarget = true;
    }

    @Unique
    public void touchcontroller$interactAfter(Player player) {
        if (!touchcontroller$resetPlayerLookTarget) {
            return;
        }
        touchcontroller$resetPlayerLookTarget = false;
        player.setYRot(touchcontroller$prevYaw);
        player.setXRot(touchcontroller$prevPitch);
        this.startPrediction(this.minecraft.level, sequence -> new ServerboundMovePlayerPacket.Rot(player.getYRot(), player.getXRot(), player.onGround(), player.horizontalCollision));
    }

    @Inject(
            method = "useItemOn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;startPrediction(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/multiplayer/prediction/PredictiveAction;)V",
                    ordinal = 0
            )
    )
    public void interactBlockBefore(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        touchcontroller$interactBefore(player, hand);
    }

    @Inject(
            method = "useItemOn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;startPrediction(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/multiplayer/prediction/PredictiveAction;)V",
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    public void interactBlockAfter(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        touchcontroller$interactAfter(player);
    }

    @Inject(
            method = "useItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;startPrediction(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/multiplayer/prediction/PredictiveAction;)V",
                    ordinal = 0
            )
    )
    public void interactItemBefore(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        touchcontroller$interactBefore(player, hand);
    }

    @Inject(
            method = "useItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;startPrediction(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/multiplayer/prediction/PredictiveAction;)V",
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    public void interactItemAfter(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        touchcontroller$interactAfter(player);
    }
}
