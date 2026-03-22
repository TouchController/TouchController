package top.fifthlight.combine.animation

import androidx.compose.runtime.*
import aurelienribon.tweenengine.Tween
import top.fifthlight.combine.paint.Color
import top.fifthlight.data.IntOffset
import top.fifthlight.data.IntSize
import top.fifthlight.data.Offset
import top.fifthlight.data.Size

@Composable
fun <T> animateValueAsState(
    targetValue: T,
    tweenSpec: TweenSpec = TweenSpec.Base,
    converter: TwoWayConverter<T>,
): State<T> {
    val manager = LocalTweenManager.current
    var changed by remember { mutableStateOf(false) }
    val animateState = remember { AnimateState(targetValue, converter) }

    LaunchedEffect(targetValue, tweenSpec, manager) {
        if (!changed) {
            changed = true
            return@LaunchedEffect
        }
        val spec = tweenSpec.toFull()
        val array = FloatArray(4)
        val len = converter.getValues(targetValue, spec.tweenType, array)
        val actualArray = array.sliceArray(0 until len)
        Tween.to(animateState, spec.tweenType, spec.duration)
            .target(*actualArray)
            .ease(spec.equations)
            .also(spec::apply)
            .launch(manager)
    }

    return animateState
}

@Composable
fun animateFloatAsState(
    targetValue: Float,
    tweenSpec: TweenSpec = TweenSpec.Base,
) = animateValueAsState(
    targetValue = targetValue,
    tweenSpec = tweenSpec,
    converter = Float.converter,
)

@Composable
fun animateIntAsState(
    targetValue: Int,
    tweenSpec: TweenSpec = TweenSpec.Base,
) = animateValueAsState(
    targetValue = targetValue,
    tweenSpec = tweenSpec,
    converter = Int.converter,
)

@Composable
fun animateSizeAsState(
    targetValue: Size,
    tweenSpec: TweenSpec = TweenSpec.Base,
) = animateValueAsState(
    targetValue = targetValue,
    tweenSpec = tweenSpec,
    converter = Size.converter,
)

@Composable
fun animateOffsetAsState(
    targetValue: Offset,
    tweenSpec: TweenSpec = TweenSpec.Base,
) = animateValueAsState(
    targetValue = targetValue,
    tweenSpec = tweenSpec,
    converter = Offset.converter,
)

@Composable
fun animateIntSizeAsState(
    targetValue: IntSize,
    tweenSpec: TweenSpec = TweenSpec.Base,
) = animateValueAsState(
    targetValue = targetValue,
    tweenSpec = tweenSpec,
    converter = IntSize.converter,
)

@Composable
fun animateIntOffsetAsState(
    targetValue: IntOffset,
    tweenSpec: TweenSpec = TweenSpec.Base,
) = animateValueAsState(
    targetValue = targetValue,
    tweenSpec = tweenSpec,
    converter = IntOffset.converter,
)

@Composable
fun animateColorAsState(
    targetValue: Color,
    tweenSpec: TweenSpec = TweenSpec.Base,
) = animateValueAsState(
    targetValue = targetValue,
    tweenSpec = tweenSpec,
    converter = Color.converter,
)
