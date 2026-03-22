package top.fifthlight.blazerod.model.vmd

import org.joml.*
import top.fifthlight.blazerod.model.animation.*
import top.fifthlight.blazerod.model.util.FloatWrapper
import top.fifthlight.blazerod.model.util.MutableFloat

abstract class VmdBezierInterpolation<C : VmdBezierInterpolation<C, T>, T : AnimationChannelComponent.Type<C, T>>(
    val expectChannels: Int,
) : AnimationInterpolation(1), AnimationChannelComponent<C, T> {
    protected lateinit var channelComponent: VmdBezierChannelComponent

    override fun onAttachToChannel(channel: AnimationChannel<*, *>) {
        channelComponent = channel.getComponent(VmdBezierChannelComponent.Type)
            ?: error("No VmdBezierChannelComponent found for VmdBezierInterpolation")
        require(expectChannels == channelComponent.channels) { "VmdBezierInterpolation must be attached with $expectChannels channels for this type." }
    }

    override fun interpolateVector3f(
        context: AnimationContext,
        state: AnimationState,
        delta: Float,
        startFrame: Int,
        endFrame: Int,
        startValue: List<Vector3fc>,
        endValue: List<Vector3fc>,
        result: Vector3f,
    ): Unit = error("Unimplemented")

    override fun interpolateQuaternionf(
        context: AnimationContext,
        state: AnimationState,
        delta: Float,
        startFrame: Int,
        endFrame: Int,
        startValue: List<Quaternionfc>,
        endValue: List<Quaternionfc>,
        result: Quaternionf,
    ): Unit = error("Unimplemented")

    override fun interpolateFloat(
        context: AnimationContext,
        state: AnimationState,
        delta: Float,
        startFrame: Int,
        endFrame: Int,
        startValue: List<FloatWrapper>,
        endValue: List<FloatWrapper>,
        result: MutableFloat,
    ): Unit = error("Unimplemented")
}

class VmdBezierVector3fInterpolation :
    VmdBezierInterpolation<VmdBezierVector3fInterpolation, VmdBezierVector3fInterpolation.Type>(3) {
    object Type : AnimationChannelComponent.Type<VmdBezierVector3fInterpolation, Type> {
        override val name: String
            get() = "vmd_beizer_interpolation_vector3f"
    }

    override val type: Type
        get() = Type

    override fun interpolateVector3f(
        context: AnimationContext,
        state: AnimationState,
        delta: Float,
        startFrame: Int,
        endFrame: Int,
        startValue: List<Vector3fc>,
        endValue: List<Vector3fc>,
        result: Vector3f,
    ) {
        val xDelta = channelComponent.getDelta(startFrame, 0, delta)
        val yDelta = channelComponent.getDelta(startFrame, 1, delta)
        val zDelta = channelComponent.getDelta(startFrame, 2, delta)
        val start = startValue[0]
        val end = endValue[0]
        result.set(
            Math.lerp(start.x(), end.x(), xDelta),
            Math.lerp(start.y(), end.y(), yDelta),
            Math.lerp(start.z(), end.z(), zDelta),
        )
    }
}

class VmdBezierSimpleVector3fInterpolation :
    VmdBezierInterpolation<VmdBezierSimpleVector3fInterpolation, VmdBezierSimpleVector3fInterpolation.Type>(1) {
    object Type : AnimationChannelComponent.Type<VmdBezierSimpleVector3fInterpolation, Type> {
        override val name: String
            get() = "vmd_beizer_interpolation_simple_vector3f"
    }

    override val type: Type
        get() = Type

    override fun interpolateVector3f(
        context: AnimationContext,
        state: AnimationState,
        delta: Float,
        startFrame: Int,
        endFrame: Int,
        startValue: List<Vector3fc>,
        endValue: List<Vector3fc>,
        result: Vector3f,
    ) {
        val delta = channelComponent.getDelta(startFrame, 0, delta)
        val start = startValue[0]
        val end = endValue[0]
        start.lerp(end, delta, result)
    }
}

class VmdBezierQuaternionfInterpolation :
    VmdBezierInterpolation<VmdBezierQuaternionfInterpolation, VmdBezierQuaternionfInterpolation.Type>(1) {
    object Type : AnimationChannelComponent.Type<VmdBezierQuaternionfInterpolation, Type> {
        override val name: String
            get() = "vmd_beizer_interpolation_quaternionf"
    }

    override val type: Type
        get() = Type

    override fun interpolateQuaternionf(
        context: AnimationContext,
        state: AnimationState,
        delta: Float,
        startFrame: Int,
        endFrame: Int,
        startValue: List<Quaternionfc>,
        endValue: List<Quaternionfc>,
        result: Quaternionf,
    ) {
        val bezierDelta = channelComponent.getDelta(startFrame, 0, delta)
        result.set(startValue[0]).slerp(endValue[0], bezierDelta)
    }
}

class VmdBezierFloatInterpolation :
    VmdBezierInterpolation<VmdBezierFloatInterpolation, VmdBezierFloatInterpolation.Type>(1) {
    object Type : AnimationChannelComponent.Type<VmdBezierFloatInterpolation, Type> {
        override val name: String
            get() = "vmd_beizer_interpolation_float"
    }

    override val type: Type
        get() = Type

    override fun interpolateFloat(
        context: AnimationContext,
        state: AnimationState,
        delta: Float,
        startFrame: Int,
        endFrame: Int,
        startValue: List<FloatWrapper>,
        endValue: List<FloatWrapper>,
        result: MutableFloat,
    ) {
        val bezierDelta = channelComponent.getDelta(startFrame, 0, delta)
        result.value = Math.lerp(startValue[0].value, endValue[0].value, bezierDelta)
    }
}
