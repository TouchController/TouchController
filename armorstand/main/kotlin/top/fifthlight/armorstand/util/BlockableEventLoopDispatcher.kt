package top.fifthlight.armorstand.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import net.minecraft.util.thread.BlockableEventLoop
import kotlin.coroutines.CoroutineContext

class BlockableEventLoopDispatcher(private val executor: BlockableEventLoop<*>) : CoroutineDispatcher() {
    override fun isDispatchNeeded(context: CoroutineContext) = !this.executor.isSameThread

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (this.executor.isSameThread) {
            Dispatchers.Unconfined.dispatch(context, block)
        } else {
            this.executor.execute(block)
        }
    }
}
