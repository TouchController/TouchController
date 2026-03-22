package top.fifthlight.touchcontroller.mixin.v1_21_11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import top.fifthlight.touchcontroller.common.model.ControllerHudModel;
import top.fifthlight.touchcontroller.common.util.crosshair.CrosshairTargetHelper;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
    @Shadow
    @Final
    protected Minecraft minecraft;

    @Unique
    private static Vec3 touchcontroller$currentDirection;

    @Redirect(
            method = "pick(Lnet/minecraft/world/entity/Entity;DDF)Lnet/minecraft/world/phys/HitResult;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;pick(DFZ)Lnet/minecraft/world/phys/HitResult;",
                    ordinal = 0
            )
    )
    private static HitResult cameraRaycast(Entity instance, double maxDistance, float tickDelta, boolean includeFluids) {
        var gameRenderer = Minecraft.getInstance().gameRenderer;
        var fov = ((GameRendererInvoker) gameRenderer).callGetFov(gameRenderer.getMainCamera(), tickDelta, true);
        var cameraPitch = Math.toRadians(instance.getViewXRot(tickDelta));
        var cameraYaw = Math.toRadians(instance.getViewYRot(tickDelta));

        var position = instance.getEyePosition(tickDelta);
        var projectionMatrix = Minecraft.getInstance().gameRenderer.getProjectionMatrix(fov);
        var direction = CrosshairTargetHelper.getCrosshairDirection(projectionMatrix, cameraPitch, cameraYaw);
        CrosshairTargetHelper.INSTANCE.setLastCrosshairDirection(direction);

        touchcontroller$currentDirection = new Vec3(direction.x, direction.y, direction.z);
        var interactionTarget = position.add(direction.x * maxDistance, direction.y * maxDistance, direction.z * maxDistance);
        var fluidHandling = includeFluids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE;
        return instance.level().clip(new ClipContext(position, interactionTarget, ClipContext.Block.OUTLINE, fluidHandling, instance));
    }

    @Redirect(
            method = "pick(Lnet/minecraft/world/entity/Entity;DDF)Lnet/minecraft/world/phys/HitResult;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getViewVector(F)Lnet/minecraft/world/phys/Vec3;",
                    ordinal = 0
            )
    )
    private static Vec3 getRotationVec(Entity instance, float tickDelta) {
        return touchcontroller$currentDirection;
    }

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
