package top.fifthlight.blazerod.render.version_1_21_8.animation.context

import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import top.fifthlight.blazerod.render.version_1_21_8.api.animation.AnimationContexts
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl

@ActualImpl(AnimationContexts::class)
object AnimationContextsImpl : AnimationContexts {
    @JvmStatic
    @ActualConstructor("create")
    fun create(): AnimationContexts = this

    override fun base() = BaseAnimationContext()

    override fun entity(entity: Entity) = EntityAnimationContext(entity)

    override fun livingEntity(entity: LivingEntity) = LivingEntityAnimationContext(entity)

    override fun player(player: Player) = PlayerEntityAnimationContext(player)
}
