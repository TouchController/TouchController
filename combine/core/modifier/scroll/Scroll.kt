package top.fifthlight.combine.modifier.scroll

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import aurelienribon.tweenengine.Tween
import aurelienribon.tweenengine.TweenAccessor
import aurelienribon.tweenengine.TweenManager
import aurelienribon.tweenengine.equations.Quint
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import top.fifthlight.combine.animation.LocalTweenManager
import top.fifthlight.data.Offset

class ScrollState(private val tweenManager: TweenManager) {
    private val _actualProgress = MutableStateFlow(0)
    val actualProgress = _actualProgress.asStateFlow()
    private val _overscroll = MutableStateFlow(0)
    val overscroll = _overscroll.asStateFlow()
    private val _renderProgress = TweenFlow(MutableStateFlow(0))
    val progress = _renderProgress.asStateFlow()
    var contentHeight = 0
        internal set
    var viewportHeight = 0
        internal set
    internal var initialPointerPosition: Offset? = null
    internal var startProgress = 0
    internal var startPointerPosition: Offset? = null
    internal var scrolling: Boolean = false
    private var tween: Tween? = null

    // We use delegate here, so no problem
    @OptIn(ExperimentalForInheritanceCoroutinesApi::class)
    private class TweenFlow(
        private val stateFlow: MutableStateFlow<Int>,
    ): MutableStateFlow<Int> by stateFlow {
        init {
            Tween.registerAccessor(TweenFlow::class.java, Accessor)
        }

        companion object Accessor: TweenAccessor<TweenFlow> {
            override fun getValues(
                target: TweenFlow,
                tweenType: Int,
                returnValues: FloatArray,
            ): Int {
                returnValues[0] = target.stateFlow.value.toFloat()
                return 1
            }

            override fun setValues(
                target: TweenFlow,
                tweenType: Int,
                newValues: FloatArray,
            ) {
                target.stateFlow.value = newValues[0].toInt()
            }
        }
    }

    fun stopAnimation() {
        tween?.let {
            tweenManager.killTarget(_renderProgress)
            tween = null
        }
    }

    fun updateProgress(progress: Int, animateOverscroll: Boolean = false) {
        val maxProgress = (contentHeight - viewportHeight).takeIf { it > 0 }

        // calculate actualProgress and overscroll
        val needAnimation = if (progress < 0) {
            _actualProgress.value = 0
            _overscroll.value = progress
            _renderProgress.value = progress
            true
        } else if (maxProgress != null && progress > maxProgress) {
            _actualProgress.value = maxProgress
            _overscroll.value = progress - maxProgress
            _renderProgress.value = progress
            true
        } else {
            _actualProgress.value = progress
            _overscroll.value = 0
            _renderProgress.value = progress
            false
        }
        stopAnimation()
        if (needAnimation && animateOverscroll) {
            tween = Tween.to(_renderProgress, 0, .6f)
                .target(_actualProgress.value.toFloat())
                .ease(Quint.OUT)
                .start(tweenManager)
        }
    }
}

@Composable
fun rememberScrollState() = run {
    val tweenManager = LocalTweenManager.current
    val state = remember { ScrollState(tweenManager) }
    DisposableEffect(state) {
        onDispose {
            // ?
        }
    }
    state
}
