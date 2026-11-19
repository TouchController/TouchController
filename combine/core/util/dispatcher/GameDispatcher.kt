package top.fifthlight.combine.core.util.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import top.fifthlight.mergetools.api.ExpectFactory

interface GameDispatcherProvider {
    val gameDispatcher: CoroutineDispatcher

    @ExpectFactory
    interface Factory {
        fun of(): GameDispatcherProvider
    }
}

val gameDispatcher by lazy {
    GameDispatcherProviderFactory.of().gameDispatcher
}

@Suppress("UnusedReceiverParameter")
val Dispatchers.Game
    get() = gameDispatcher
