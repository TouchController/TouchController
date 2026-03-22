package top.fifthlight.blazerod.model.animation

import org.joml.*
import top.fifthlight.blazerod.model.util.FloatWrapper
import top.fifthlight.blazerod.model.util.MutableFloat

abstract class AnimationInterpolation(val elements: Int) {
    init {
        require(elements in 1..MAX_ELEMENTS) { "Bad elements count: should be in [1, $MAX_ELEMENTS]" }
    }

    abstract fun interpolateVector3f(
        context: AnimationContext,
        state: AnimationState,
        delta: Float,
        startFrame: Int,
        endFrame: Int,
        startValue: List<Vector3fc>,
        endValue: List<Vector3fc>,
        result: Vector3f,
    )

    abstract fun interpolateQuaternionf(
        context: AnimationContext,
        state: AnimationState,
        delta: Float,
        startFrame: Int,
        endFrame: Int,
        startValue: List<Quaternionfc>,
        endValue: List<Quaternionfc>,
        result: Quaternionf,
    )

    abstract fun interpolateFloat(
        context: AnimationContext,
        state: AnimationState,
        delta: Float,
        startFrame: Int,
        endFrame: Int,
        startValue: List<FloatWrapper>,
        endValue: List<FloatWrapper>,
        result: MutableFloat,
    )

    companion object {
        const val MAX_ELEMENTS = 4

        val linear = object : AnimationInterpolation(1) {
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
                result.set(startValue[0]).lerp(endValue[0], delta)
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
            ) {
                result.set(startValue[0]).slerp(endValue[0], delta)
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
            ) {
                result.value = Math.lerp(startValue[0].value, endValue[0].value, delta)
            }
        }

        val step = object : AnimationInterpolation(1) {
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
                if (delta >= 0.5f) {
                    result.set(endValue[0])
                } else {
                    result.set(startValue[0])
                }
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
            ) {
                if (delta >= 0.5f) {
                    result.set(endValue[0])
                } else {
                    result.set(startValue[0])
                }
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
            ) {
                if (delta >= 0.5f) {
                    result.value = endValue[0].value
                } else {
                    result.value = startValue[0].value
                }
            }
        }

        val cubicSpline = object : AnimationInterpolation(3) {
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
                val t = delta
                val t2 = t * t
                val t3 = t2 * t

                // Hermite spline formula
                val h1 = 2f * t3 - 3f * t2 + 1f
                val h2 = t3 - 2f * t2 + t
                val h3 = -2f * t3 + 3f * t2
                val h4 = t3 - t2

                startValue[1].mul(h1, result)
                    .fma(h2, startValue[2], result)
                    .fma(h3, endValue[1], result)
                    .fma(h4, endValue[0], result)
            }

            private val tempQuaternion = Quaternionf()
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
                val t = delta
                val t2 = t * t
                val t3 = t2 * t

                val h1 = 2f * t3 - 3f * t2 + 1f
                val h2 = t3 - 2f * t2 + t
                val h3 = -2f * t3 + 3f * t2
                val h4 = t3 - t2

                startValue[1].mul(h1, result)
                startValue[2].mul(h2, tempQuaternion)
                result.add(tempQuaternion)
                endValue[1].mul(h3, tempQuaternion)
                result.add(tempQuaternion)
                endValue[0].mul(h4, tempQuaternion)
                result.add(tempQuaternion)
                result.normalize()
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
            ) {
                val t = delta
                val t2 = t * t
                val t3 = t2 * t

                // Hermite spline formula
                val h1 = 2f * t3 - 3f * t2 + 1f
                val h2 = t3 - 2f * t2 + t
                val h3 = -2f * t3 + 3f * t2
                val h4 = t3 - t2

                val p0 = startValue[1].value
                val m0 = startValue[2].value
                val p1 = endValue[1].value
                val m1 = endValue[0].value

                result.value = Math.fma(h1, p0, Math.fma(h2, m0, Math.fma(h3, p1, h4 * m1)))
            }
        }
    }
}

interface AnimationInterpolator<T> {
    fun interpolate(
        context: AnimationContext,
        state: AnimationState,
        delta: Float,
        startFrame: Int,
        endFrame: Int,
        startValue: List<T>,
        endValue: List<T>,
        result: T,
    )
}

class Vector3AnimationInterpolator(val type: AnimationInterpolation) : AnimationInterpolator<Vector3f> {
    override fun interpolate(
        context: AnimationContext,
        state: AnimationState,
        delta: Float,
        startFrame: Int,
        endFrame: Int,
        startValue: List<Vector3f>,
        endValue: List<Vector3f>,
        result: Vector3f,
    ) = type.interpolateVector3f(
        context = context,
        state = state,
        delta = delta,
        startFrame = startFrame,
        endFrame = endFrame,
        startValue = startValue,
        endValue = endValue,
        result = result,
    )
}

class QuaternionAnimationInterpolator(val type: AnimationInterpolation) : AnimationInterpolator<Quaternionf> {
    override fun interpolate(
        context: AnimationContext,
        state: AnimationState,
        delta: Float,
        startFrame: Int,
        endFrame: Int,
        startValue: List<Quaternionf>,
        endValue: List<Quaternionf>,
        result: Quaternionf,
    ) = type.interpolateQuaternionf(
        context = context,
        state = state,
        delta = delta,
        startFrame = startFrame,
        endFrame = endFrame,
        startValue = startValue,
        endValue = endValue,
        result = result,
    )
}

class FloatAnimationInterpolator(val type: AnimationInterpolation) : AnimationInterpolator<MutableFloat> {
    override fun interpolate(
        context: AnimationContext,
        state: AnimationState,
        delta: Float,
        startFrame: Int,
        endFrame: Int,
        startValue: List<MutableFloat>,
        endValue: List<MutableFloat>,
        result: MutableFloat,
    ) {
        type.interpolateFloat(
            context = context,
            state = state,
            delta = delta,
            startFrame = startFrame,
            endFrame = endFrame,
            startValue = startValue,
            endValue = endValue,
            result = result,
        )
    }
}
