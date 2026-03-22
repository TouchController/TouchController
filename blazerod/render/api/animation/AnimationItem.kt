package top.fifthlight.blazerod.render.api.animation

import top.fifthlight.blazerod.render.api.resource.ModelInstance
import top.fifthlight.blazerod.render.api.resource.RenderScene
import top.fifthlight.blazerod.model.animation.Animation
import top.fifthlight.blazerod.model.animation.AnimationContext
import top.fifthlight.blazerod.model.animation.AnimationState
import top.fifthlight.mergetools.api.ExpectFactory

interface AnimationItem {
    val name: String?
    val duration: Float?

    @ExpectFactory
    interface Factory {
        fun load(
            scene: RenderScene,
            animation: Animation,
        ): AnimationItem
    }
}

interface AnimationItemPendingValues

interface AnimationItemInstance {
    fun createState(context: AnimationContext): AnimationState
    fun update(context: AnimationContext, state: AnimationState): AnimationItemPendingValues
    fun apply(instance: ModelInstance, pendingValues: AnimationItemPendingValues)

    @ExpectFactory
    interface Factory {
        fun of(animation: AnimationItem): AnimationItemInstance
    }
}

interface MaskableAnimationItemInstance {
    fun applyMasked(
        instance: ModelInstance,
        pendingValues: AnimationItemPendingValues,
        allowedNodeIndices: BooleanArray,
    )
}
