package top.fifthlight.armorstand.ui.state

import java.nio.file.Path

data class AnimationScreenState(
    val playState: PlayState = PlayState.None,
    val selectedAnimation: AnimationItem? = null,
    val embedAnimations: List<AnimationItem> = listOf(),
    val externalAnimations: List<AnimationItem> = listOf(),
    val ikList: List<Pair<String?, Boolean>> = listOf(),
) {
    val animations: List<AnimationItem> by lazy {
        embedAnimations + externalAnimations
    }

    data class AnimationItem(
        val name: String? = null,
        val duration: Float? = null,
        val source: Source,
    ) {
        sealed class Source {
            data class Embed(val index: Int) : Source()
            data class External(val path: Path) : Source()
        }
    }

    sealed class PlayState {
        data object None : PlayState()

        data class Paused(
            val progress: Float,
            val length: Float,
            val speed: Float,
            val readonly: Boolean,
        ) : PlayState()

        data class Playing(
            val progress: Float,
            val length: Float,
            val speed: Float,
            val readonly: Boolean,
        ) : PlayState()
    }
}
