package top.fifthlight.combine.animation

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import aurelienribon.tweenengine.Tween
import aurelienribon.tweenengine.TweenAccessor

class AnimateState<T>(
    initialState: T,
    private val converter: TwoWayConverter<T>,
) : State<T> {
    init {
        Tween.registerAccessor(AnimateState::class.java, Accessor)
    }

    override var value: T by mutableStateOf(initialState)
        internal set

    private fun getValues(
        tweenType: Int,
        returnValues: FloatArray
    ): Int = converter.getValues(value, tweenType, returnValues)

    private fun setValues(
        tweenType: Int,
        newValues: FloatArray
    ) {
        value = converter.setValues(tweenType, newValues)
    }

    companion object Accessor: TweenAccessor<AnimateState<*>> {
        override fun getValues(
            target: AnimateState<*>,
            tweenType: Int,
            returnValues: FloatArray,
        ): Int = target.getValues(tweenType, returnValues)

        override fun setValues(
            target: AnimateState<*>,
            tweenType: Int,
            newValues: FloatArray,
        ) = target.setValues(tweenType, newValues)
    }
}
