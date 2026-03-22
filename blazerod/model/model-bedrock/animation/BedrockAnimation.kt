package top.fifthlight.blazerod.model.bedrock.animation

import top.fifthlight.blazerod.model.animation.Animation
import top.fifthlight.blazerod.model.animation.AnimationChannel
import top.fifthlight.blazerod.model.animation.AnimationContext
import top.fifthlight.blazerod.model.animation.KeyFrameAnimationChannel
import top.fifthlight.blazerod.model.bedrock.molang.value.MolangValue

class BedrockAnimation(
    override val name: String,
    override val channels: List<AnimationChannel<*, *>>,
    duration: Float?,
    val startDelay: MolangValue = MolangValue.ZERO,
    val loopDelay: MolangValue = MolangValue.ZERO,
    animTimeUpdate: MolangValue?,
    val loopMode: AnimationLoopMode,
) : Animation {
    override val duration = duration ?: channels.maxOfOrNull { (it as? KeyFrameAnimationChannel)?.duration ?: 0f } ?: 0f

    sealed class AnimationTimeUpdate {
        object Fixed : AnimationTimeUpdate()
        class Molang(val value: MolangValue) : AnimationTimeUpdate()
    }

    private val animTimeUpdate = when (animTimeUpdate) {
        null -> AnimationTimeUpdate.Fixed
        else -> AnimationTimeUpdate.Molang(animTimeUpdate)
    }

    override fun createState(context: AnimationContext) = BedrockAnimationState(
        context = context,
        duration = duration,
        startDelay = startDelay,
        loopDelay = loopDelay,
        loopMode = loopMode,
        animTimeUpdate = animTimeUpdate,
    )
}
