package top.fifthlight.touchcontroller.gal.view.v26_1

import net.minecraft.client.Minecraft
import net.minecraft.world.phys.HitResult
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import top.fifthlight.touchcontroller.common.gal.view.CrosshairTarget
import top.fifthlight.touchcontroller.common.gal.view.ViewActionProvider
import top.fifthlight.touchcontroller.extension.v26_1.GameModeWithBreakingProgress
import top.fifthlight.touchcontroller.gal.entity.v26_1.EntityTypeImpl

@ActualImpl(ViewActionProvider::class)
object ViewActionProviderImpl : ViewActionProvider {
    @JvmStatic
    @ActualConstructor
    fun of(): ViewActionProvider = this

    private val client = Minecraft.getInstance()

    override fun getCrosshairTarget(): CrosshairTarget? {
        val target = client.hitResult ?: return null
        return when (target.type) {
            HitResult.Type.ENTITY -> client.crosshairPickEntity?.let {
                CrosshairTarget.Entity(EntityTypeImpl(it.type))
            } ?: CrosshairTarget.Miss

            HitResult.Type.BLOCK -> CrosshairTarget.Block
            HitResult.Type.MISS -> CrosshairTarget.Miss
            else -> null
        }
    }

    override fun getCurrentBreakingProgress(): Float {
        val manager = client.gameMode
        val accessor = manager as GameModeWithBreakingProgress
        return accessor.`touchcontroller$getBreakingProgress`()
    }
}
