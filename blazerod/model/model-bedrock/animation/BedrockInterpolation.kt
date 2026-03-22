package top.fifthlight.blazerod.model.bedrock.animation

import it.unimi.dsi.fastutil.bytes.ByteList
import org.joml.Quaternionf
import org.joml.Quaternionfc
import org.joml.Vector3f
import org.joml.Vector3fc
import top.fifthlight.blazerod.model.animation.*
import top.fifthlight.blazerod.model.util.FloatWrapper
import top.fifthlight.blazerod.model.util.MutableFloat

class BedrockInterpolation(
    val lerpModes: ByteList,
) : AnimationInterpolation(1),
    AnimationChannelComponent<BedrockInterpolation, BedrockInterpolation.ComponentType> {
    object ComponentType : AnimationChannelComponent.Type<BedrockInterpolation, ComponentType> {
        override val name: String
            get() = "bedrock_interpolation"
    }

    override val type: ComponentType
        get() = ComponentType

    private val catmullRom = CatmullRomInterpolation()

    // Set this interpolation as a component, to get the keyframe data
    override fun onAttachToChannel(channel: AnimationChannel<*, *>) {
        val channel = (channel as? KeyFrameAnimationChannel)
            ?: error("CatmullRomInterpolation must be attached to a keyframe animation channel")
        catmullRom.keyFrameData = channel.keyframeData
    }

    private fun getType(frame: Int) = BedrockLerpMode.entries[lerpModes.getByte(frame).toInt()]

    override fun interpolateVector3f(
        context: AnimationContext,
        state: AnimationState,
        delta: Float,
        startFrame: Int,
        endFrame: Int,
        startValue: List<Vector3fc>,
        endValue: List<Vector3fc>,
        result: Vector3f,
    ) = when (getType(startFrame)) {
        BedrockLerpMode.LINEAR -> linear.interpolateVector3f(
            context,
            state,
            delta,
            startFrame,
            endFrame,
            startValue,
            endValue,
            result
        )
        BedrockLerpMode.CATMULLROM -> catmullRom.interpolateVector3f(
            context,
            state,
            delta,
            startFrame,
            endFrame,
            startValue,
            endValue,
            result
        )
    }

    override fun interpolateQuaternionf(
        context: AnimationContext,
        state: AnimationState,
        delta: Float,
        startFrame: Int,
        endFrame: Int,
        startValue: List<Quaternionfc>,
        endValue: List<Quaternionfc>,
        result: Quaternionf,
    ) = when (getType(startFrame)) {
        BedrockLerpMode.LINEAR -> linear.interpolateQuaternionf(
            context,
            state,
            delta,
            startFrame,
            endFrame,
            startValue,
            endValue,
            result
        )

        BedrockLerpMode.CATMULLROM -> catmullRom.interpolateQuaternionf(
            context,
            state,
            delta,
            startFrame,
            endFrame,
            startValue,
            endValue,
            result
        )
    }

    override fun interpolateFloat(
        context: AnimationContext,
        state: AnimationState,
        delta: Float,
        startFrame: Int,
        endFrame: Int,
        startValue: List<FloatWrapper>,
        endValue: List<FloatWrapper>,
        result: MutableFloat,
    ) = when (getType(startFrame)) {
        BedrockLerpMode.LINEAR -> linear.interpolateFloat(
            context,
            state,
            delta,
            startFrame,
            endFrame,
            startValue,
            endValue,
            result
        )
        BedrockLerpMode.CATMULLROM -> catmullRom.interpolateFloat(
            context,
            state,
            delta,
            startFrame,
            endFrame,
            startValue,
            endValue,
            result
        )
    }
}
