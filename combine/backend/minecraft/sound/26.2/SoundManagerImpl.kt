package top.fifthlight.combine.backend.minecraft.sound.v26_2

import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.sounds.SoundManager
import net.minecraft.sounds.SoundEvents
import top.fifthlight.combine.sound.SoundKind

class SoundManagerImpl(
    private val soundManager: SoundManager,
) : top.fifthlight.combine.sound.SoundManager {
    override fun play(kind: SoundKind, pitch: Float) {
        val soundEvent = when (kind) {
            SoundKind.BUTTON_PRESS -> SoundEvents.UI_BUTTON_CLICK
        }
        soundManager.play(SimpleSoundInstance.forUI(soundEvent, pitch))
    }
}
