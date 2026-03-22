package top.fifthlight.blazerod.render.version_1_21_8.animation.context

import net.minecraft.core.Direction
import net.minecraft.world.entity.Entity
import top.fifthlight.blazerod.model.animation.AnimationContext
import top.fifthlight.blazerod.model.animation.AnimationContext.Property.*
import top.fifthlight.blazerod.model.animation.AnimationContext.RenderingTargetType
import top.fifthlight.blazerod.render.version_1_21_8.util.math.set
import top.fifthlight.blazerod.render.version_1_21_8.util.math.sub

open class EntityAnimationContext(
    open val entity: Entity,
) : BaseAnimationContext() {
    companion object {
        @JvmStatic
        protected val propertyTypes = BaseAnimationContext.propertyTypes + setOf(
            RenderTarget,
            EntityPosition,
            EntityPositionDelta,
            EntityHorizontalFacing,
            EntityGroundSpeed,
            EntityVerticalSpeed,
            EntityHasRider,
            EntityIsRiding,
            EntityIsInWater,
            EntityIsInWaterOrRain,
            EntityIsInFire,
            EntityIsOnGround,
        )
            @JvmName("getEntityPropertyTypes")
            get
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getProperty(type: AnimationContext.Property<T>): T? = when (type) {
        RenderTarget -> RenderingTargetType.ENTITY

        EntityPosition -> vector3dBuffer.set(entity.position())

        EntityPositionDelta -> entity.position().sub(entity.oldPosition(), vector3dBuffer)

        EntityHorizontalFacing -> when (entity.direction) {
            Direction.NORTH -> 2
            Direction.SOUTH -> 3
            Direction.WEST -> 4
            Direction.EAST -> 5
            Direction.UP, Direction.DOWN -> throw AssertionError("Invalid cardinal facing")
        }.let { intBuffer.apply { value = it } }

        EntityGroundSpeed -> doubleBuffer.apply {
            value = entity.deltaMovement.horizontalDistance()
        }

        EntityVerticalSpeed -> doubleBuffer.apply { value = entity.deltaMovement.y }

        EntityHasRider -> booleanBuffer.apply { value = entity.isVehicle }

        EntityIsRiding -> booleanBuffer.apply { value = entity.isPassenger }

        EntityIsInWater -> booleanBuffer.apply { value = entity.isInWater }

        EntityIsInWaterOrRain -> booleanBuffer.apply { value = entity.isInWaterOrRain }

        EntityIsInFire -> booleanBuffer.apply { value = entity.isOnFire }

        EntityIsOnGround -> booleanBuffer.apply { value = entity.onGround() }

        else -> super.getProperty(type)
    } as T?

    override fun getPropertyTypes() = propertyTypes
}
