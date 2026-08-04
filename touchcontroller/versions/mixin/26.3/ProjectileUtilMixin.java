package top.fifthlight.touchcontroller.mixin.v26_3;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import top.fifthlight.touchcontroller.common.util.crosshair.CrosshairTargetHelper;

@Mixin(ProjectileUtil.class)
public class ProjectileUtilMixin {
    @Redirect(method = "getHitEntitiesAlong(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/component/AttackRange;Ljava/util/function/Predicate;Lnet/minecraft/world/level/ClipContext$Block;)Lcom/mojang/datafixers/util/Either;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getHeadLookAngle()Lnet/minecraft/world/phys/Vec3;"))
    private static @NonNull Vec3 overrideClientHitTarget(Entity instance) {
        if (!(instance instanceof LocalPlayer localPlayer)) {
            return instance.getHeadLookAngle();
        }
        var client = Minecraft.getInstance();
        if (localPlayer != client.player) {
            return instance.getHeadLookAngle();
        }

        var gameRenderer = Minecraft.getInstance().gameRenderer;
        var cameraPitch = Math.toRadians(localPlayer.getXRot());
        var cameraYaw = Math.toRadians(localPlayer.getYHeadRot());

        var projection = ((CameraAccessor) gameRenderer.mainCamera()).getProjection();
        var projectionMatrix = projection.getMatrix(new Matrix4f());
        var direction = CrosshairTargetHelper.getCrosshairDirection(projectionMatrix, cameraPitch, cameraYaw);
        return new Vec3(direction.x, direction.y, direction.z);
    }
}
