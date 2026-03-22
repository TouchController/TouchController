package top.fifthlight.blazerod.render.version_1_21_8.animation.context

import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import top.fifthlight.blazerod.model.animation.AnimationContext
import top.fifthlight.blazerod.model.animation.AnimationContext.Property.*

open class LivingEntityAnimationContext(
    override val entity: LivingEntity,
) : EntityAnimationContext(entity) {
    companion object {
        @JvmStatic
        protected val propertyTypes = EntityAnimationContext.propertyTypes + setOf(
            LivingEntityHealth,
            LivingEntityMaxHealth,
            LivingEntityHurtTime,
            LivingEntityIsDead,
            LivingEntityEquipmentCount,
        )
            @JvmName("getLivingEntityPropertyTypes")
            get
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getProperty(type: AnimationContext.Property<T>): T? = when (type) {
        LivingEntityHealth -> floatBuffer.apply { value = entity.health }

        LivingEntityMaxHealth -> floatBuffer.apply { value = entity.maxHealth }

        LivingEntityHurtTime -> intBuffer.apply { value = entity.hurtTime }

        LivingEntityIsDead -> booleanBuffer.apply { value = !entity.isAlive }

        LivingEntityEquipmentCount -> intBuffer.apply {
            value = EquipmentSlot.entries.count { entity.hasItemInSlot(it) }
        }

        else -> super.getProperty(type)
    } as T?

    override fun getPropertyTypes() = propertyTypes
}
