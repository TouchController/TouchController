package top.fifthlight.blazerod.render.version_1_21_8.animation.context

import net.minecraft.client.CameraType
import net.minecraft.core.component.DataComponents
import net.minecraft.util.Mth
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import top.fifthlight.blazerod.model.animation.AnimationContext
import top.fifthlight.blazerod.model.animation.AnimationContext.Property.*
import top.fifthlight.blazerod.model.animation.AnimationContext.RenderingTargetType
import kotlin.math.abs

open class PlayerEntityAnimationContext(
    override val entity: Player,
) : LivingEntityAnimationContext(entity) {
    companion object {
        @JvmStatic
        protected val propertyTypes = EntityAnimationContext.propertyTypes + listOf(
            RenderTarget,
            PlayerHeadXRotation,
            PlayerHeadYRotation,
            PlayerIsFirstPerson,
            PlayerPersonView,
            PlayerIsSpectator,
            PlayerIsSneaking,
            PlayerIsSprinting,
            PlayerIsSwimming,
            PlayerBodyXRotation,
            PlayerBodyYRotation,
            PlayerIsEating,
            PlayerIsUsingItem,
            PlayerLevel,
            PlayerIsJumping,
            PlayerIsSleeping,
        )
            @JvmName("getPlayerEntityPropertyTypes")
            get
    }

    private fun clampBodyYaw(entity: LivingEntity, degrees: Float, tickProgress: Float): Float {
        if (entity.vehicle is LivingEntity) {
            var f = Mth.rotLerp(tickProgress, entity.yBodyRotO, entity.yBodyRot)
            val g = 85.0f
            val h = Mth.clamp(Mth.wrapDegrees(degrees - f), -g, g)
            f = degrees - h
            if (abs(h) > 50.0f) {
                f += h * 0.2f
            }

            return f
        } else {
            return Mth.rotLerp(tickProgress, entity.yBodyRotO, entity.yBodyRot)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getProperty(type: AnimationContext.Property<T>): T? = when (type) {
        RenderTarget -> RenderingTargetType.PLAYER

        PlayerHeadXRotation -> floatBuffer.apply {
            val rawBodyYaw = Mth.rotLerp(getDeltaTick(), entity.yHeadRotO, entity.yHeadRot)
            val bodyYaw = clampBodyYaw(entity, rawBodyYaw, getDeltaTick())
            value = -Mth.wrapDegrees(rawBodyYaw - bodyYaw)
        }

        PlayerHeadYRotation -> floatBuffer.apply {
            value = entity.getXRot(getDeltaTick())
        }

        PlayerIsFirstPerson -> booleanBuffer.apply {
            val isSelf = entity == client.player
            val isFirstPerson = client.options.cameraType == CameraType.FIRST_PERSON
            value = isSelf && isFirstPerson
        }

        PlayerPersonView -> intBuffer.apply {
            val isSelf = entity == client.player
            val perspective = client.options.cameraType
            value = when {
                !isSelf -> 1
                perspective == CameraType.FIRST_PERSON -> 0
                perspective == CameraType.THIRD_PERSON_BACK -> 1
                perspective == CameraType.THIRD_PERSON_FRONT -> 2
                else -> 0
            }
        }

        PlayerIsSpectator -> booleanBuffer.apply { value = entity.isSpectator }

        PlayerIsSneaking -> booleanBuffer.apply { value = entity.isShiftKeyDown }

        PlayerIsSprinting -> booleanBuffer.apply { value = entity.isSprinting }

        PlayerIsSwimming -> booleanBuffer.apply { value = entity.isSwimming }

        PlayerBodyXRotation -> floatBuffer.apply {
            val rawBodyYaw = Mth.rotLerp(getDeltaTick(), entity.yHeadRotO, entity.yHeadRot)
            val bodyYaw = clampBodyYaw(entity, rawBodyYaw, getDeltaTick())
            value = -bodyYaw
        }

        PlayerBodyYRotation -> floatBuffer.apply { value = 0f }

        PlayerIsEating -> booleanBuffer.apply {
            val isUsingItem = entity.isUsingItem
            val usingItemHasConsumingComponent = entity.mainHandItem.components.get(DataComponents.CONSUMABLE) != null
            value = isUsingItem && usingItemHasConsumingComponent
        }

        PlayerIsUsingItem -> booleanBuffer.apply { value = entity.isUsingItem }

        PlayerIsJumping -> booleanBuffer.apply { value = entity.isJumping }

        PlayerIsSleeping -> booleanBuffer.apply { value = entity.isSleeping }

        PlayerLevel -> intBuffer.apply { value = entity.experienceLevel }

        PlayerFoodLevel -> intBuffer.apply {
            value = entity.getFoodData().foodLevel
        }

        else -> super.getProperty(type)
    } as T?

    override fun getPropertyTypes() = propertyTypes
}
