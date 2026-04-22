package top.fifthlight.combine.backend.minecraft.dispatcher.v26_2

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import net.minecraft.client.Minecraft
import top.fifthlight.combine.util.dispatcher.GameDispatcherProvider
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl
import kotlin.coroutines.CoroutineContext

@ActualImpl(GameDispatcherProvider::class)
object GameDispatcher : CoroutineDispatcher(),
    GameDispatcherProvider {
    @JvmStatic
    @ActualConstructor
    fun of(): GameDispatcherProvider = this

    private val client = Minecraft.getInstance()

    override fun isDispatchNeeded(context: CoroutineContext) = !client.isSameThread

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (client.isSameThread) {
            Dispatchers.Unconfined.dispatch(context, block)
        } else {
            client.execute(block)
        }
    }

    override val gameDispatcher: CoroutineDispatcher
        get() = this
}
