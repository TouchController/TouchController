package top.fifthlight.blazerod.model.bedrock.animation

import it.unimi.dsi.fastutil.floats.FloatList
import org.joml.Vector3f
import team.unnamed.mocha.parser.ast.Expression
import top.fifthlight.blazerod.model.animation.AnimationContext
import top.fifthlight.blazerod.model.animation.AnimationKeyFrameData
import top.fifthlight.blazerod.model.animation.AnimationState

class BedrockKeyFrameData(
    private val values: FloatList,
    private val molangs: List<List<Expression>?>?,
) : AnimationKeyFrameData<Vector3f> {
    override val frames = values.size / 6

    override val elements: Int
        get() = 1

    override fun get(
        context: AnimationContext,
        state: AnimationState,
        index: Int,
        data: List<Vector3f>,
        post: Boolean,
    ) {
        val baseOffset = index * 6 + if (post) 3 else 0
        val bedrockState = state as BedrockAnimationState

        fun eval(offset: Int) = molangs?.getOrNull(baseOffset + offset)
            ?.let { bedrockState.evalExpressions(context, it).toFloat() }
            ?: values.getFloat(baseOffset + offset)

        data[0].set(eval(0), eval(1), eval(2))
    }
}
