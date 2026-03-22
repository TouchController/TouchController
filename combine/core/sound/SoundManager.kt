package top.fifthlight.combine.sound

import androidx.compose.runtime.staticCompositionLocalOf

val LocalSoundManager = staticCompositionLocalOf<SoundManager> { EmptySoundManager }

enum class SoundKind {
    BUTTON_PRESS,
}

interface SoundManager {
    fun play(kind: SoundKind, pitch: Float)
}

private object EmptySoundManager : SoundManager {
    override fun play(kind: SoundKind, pitch: Float) {}
}
