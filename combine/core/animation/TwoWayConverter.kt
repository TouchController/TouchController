package top.fifthlight.combine.animation

import top.fifthlight.combine.paint.Color
import top.fifthlight.data.IntOffset
import top.fifthlight.data.IntSize
import top.fifthlight.data.Offset
import top.fifthlight.data.Size

interface TwoWayConverter<T> {
    fun getValues(target: T, tweenType: Int, returnValues: FloatArray): Int

    fun setValues(tweenType: Int, newValues: FloatArray): T
}

val Float.Companion.converter: TwoWayConverter<Float>
    get() = floatConverter

private val floatConverter = object : TwoWayConverter<Float> {
    override fun getValues(target: Float, tweenType: Int, returnValues: FloatArray): Int {
        returnValues[0] = target
        return 1
    }

    override fun setValues(tweenType: Int, newValues: FloatArray): Float = newValues[0]
}

val Int.Companion.converter: TwoWayConverter<Int>
    get() = intConverter

private val intConverter = object : TwoWayConverter<Int> {
    override fun getValues(target: Int, tweenType: Int, returnValues: FloatArray): Int {
        returnValues[0] = target.toFloat()
        return 1
    }

    override fun setValues(tweenType: Int, newValues: FloatArray): Int = newValues[0].toInt()
}

val Offset.Companion.converter: TwoWayConverter<Offset>
    get() = offsetConverter

private val offsetConverter =  object : TwoWayConverter<Offset> {
    override fun getValues(target: Offset, tweenType: Int, returnValues: FloatArray): Int {
        returnValues[0] = target.x
        returnValues[1] = target.y
        return 2
    }

    override fun setValues(tweenType: Int, newValues: FloatArray): Offset = Offset(
        x = newValues[0],
        y = newValues[1],
    )
}

val Size.Companion.converter: TwoWayConverter<Size>
    get() = sizeConverter

private val sizeConverter =  object : TwoWayConverter<Size> {
    override fun getValues(target: Size, tweenType: Int, returnValues: FloatArray): Int {
        returnValues[0] = target.width
        returnValues[1] = target.height
        return 2
    }

    override fun setValues(tweenType: Int, newValues: FloatArray): Size = Size(
        width = newValues[0],
        height = newValues[1],
    )
}

val IntOffset.Companion.converter: TwoWayConverter<IntOffset>
    get() = intOffsetConverter

private val intOffsetConverter =  object : TwoWayConverter<IntOffset> {
    override fun getValues(target: IntOffset, tweenType: Int, returnValues: FloatArray): Int {
        returnValues[0] = target.x.toFloat()
        returnValues[1] = target.y.toFloat()
        return 2
    }

    override fun setValues(tweenType: Int, newValues: FloatArray): IntOffset = IntOffset(
        x = newValues[0].toInt(),
        y = newValues[1].toInt(),
    )
}

val IntSize.Companion.converter: TwoWayConverter<IntSize>
    get() = intSizeConverter

private val intSizeConverter =  object : TwoWayConverter<IntSize> {
    override fun getValues(target: IntSize, tweenType: Int, returnValues: FloatArray): Int {
        returnValues[0] = target.width.toFloat()
        returnValues[1] = target.height.toFloat()
        return 2
    }

    override fun setValues(tweenType: Int, newValues: FloatArray): IntSize = IntSize(
        width = newValues[0].toInt(),
        height = newValues[1].toInt(),
    )
}

val Color.Companion.converter: TwoWayConverter<Color>
    get() = colorConverter

private val colorConverter =  object : TwoWayConverter<Color> {
    override fun getValues(target: Color, tweenType: Int, returnValues: FloatArray): Int {
        returnValues[0] = target.aFloat
        returnValues[1] = target.rFloat
        returnValues[2] = target.gFloat
        returnValues[3] = target.bFloat
        return 4
    }

    override fun setValues(tweenType: Int, newValues: FloatArray): Color = Color(
        a = newValues[0],
        r = newValues[1],
        g = newValues[2],
        b = newValues[3],
    )
}
