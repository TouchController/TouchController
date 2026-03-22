package top.fifthlight.combine.input

import androidx.compose.runtime.Stable
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

interface Interaction

@Stable
interface InteractionSource {
    val interactions: Flow<Interaction>
}

interface MutableInteractionSource: InteractionSource {
    suspend fun emit(interaction: Interaction)

    fun tryEmit(interaction: Interaction): Boolean
}

fun MutableInteractionSource(): MutableInteractionSource = MutableInteractionSourceImpl()

@Stable
private class MutableInteractionSourceImpl : MutableInteractionSource {
    override val interactions = MutableSharedFlow<Interaction>(
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override suspend fun emit(interaction: Interaction) {
        interactions.emit(interaction)
    }

    override fun tryEmit(interaction: Interaction): Boolean {
        return interactions.tryEmit(interaction)
    }
}
